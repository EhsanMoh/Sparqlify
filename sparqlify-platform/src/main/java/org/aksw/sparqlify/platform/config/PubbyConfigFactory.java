package org.aksw.sparqlify.platform.config;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mapping.SparqlifyConstants;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.config.lang.Constraint;
import org.aksw.sparqlify.config.lang.PrefixConstraint;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Concat;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.vocab.CONF;


public class PubbyConfigFactory {
	
	
	//public static Resource pubbySparqlEndpoint = ResourceFactory.createResource("urn://sparqlify/platform/pubby/sparql-endpoint");
	
	private static final Logger logger = LoggerFactory.getLogger(PubbyConfigFactory.class);

	
	private String baseUrl;
	
	private String projectName = "";
	private String projectHomepage = "";
	private File baseConfigFile;
	private Config sparqlifyConfig;
	
	//"BaseServlet.serverConfiguration"
	
	public PubbyConfigFactory() {
	}
	
	public void setBaseConfigFile(File file) {
		this.baseConfigFile = file;
	}
	
	public File getBaseConfigFile() {
		return baseConfigFile;
	}
	
	
	
	/*
	public void setOverlayModel(Model model) {
		this.overlayModel = model;
	}
	
	public Model getOverlayModel() {
		return overlayModel;
	}
	*/
	
	
	public Config getSparqlifyConfig() {
		return sparqlifyConfig;
	}

	public void setSparqlifyConfig(Config sparqlifyConfig) {
		this.sparqlifyConfig = sparqlifyConfig;
	}

	
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectHomepage() {
		return projectHomepage;
	}

	public void setProjectHomepage(String projectHomepage) {
		this.projectHomepage = projectHomepage;
	}

	
	
	public String extractPrefix(Expr expr) {
		if(expr instanceof E_Concat || expr instanceof E_StrConcatPermissive || expr instanceof E_StrConcat) {
			ExprFunction fn = expr.getFunction();
			
			List<Expr> args = fn.getArgs();
			if(args.size() == 0) {
				logger.warn("Zero argument concat expression detected: " + expr);
			} else {
				
				Expr first = args.get(0);
				
				if(first.isConstant()) {
					String value = first.getConstant().asUnquotedString();
					return value;
				}
			}
		}
		return null;
	}
	
	
	public Set<String> getKnownPrefixes(Var var, ViewDefinition viewDef) {
		Set<String> result = new HashSet<String>();

		if(viewDef.getConstraints() != null) {
		
			for(Constraint constraint : viewDef.getConstraints()) {
				if(constraint instanceof PrefixConstraint) {
					PrefixConstraint c = (PrefixConstraint)constraint;
					
					if(var.equals(c.getVar())) {
						result.addAll(c.getPrefixes().getSet());
					}
					
				}
			}
		}
		
		VarExprList vel = viewDef.getViewTemplateDefinition().getVarExprList();
		Expr expr = vel.getExpr(var);
		
		// We expect the expression to be a term constructor
		if(expr.isFunction()) {
			ExprFunction termCtor = expr.getFunction();
			if(termCtor.getFunctionIRI().equals(SparqlifyConstants.uriLabel)) {
				List<Expr> args = termCtor.getArgs();
				if(args.size() == 0) {
					logger.warn("Zero length term constructor for var " + var + " in view definition" + viewDef.getName());
				} else {
					Expr arg = args.get(0);
					
					String prefix = extractPrefix(arg);
					if(prefix != null) {
						result.add(prefix);
					}
					
				}
				
			}
		}
		

			
			
		

		return result;
	}
	
	public Set<String> getKnownPrefixes(ViewDefinition viewDef) {
		Set<String> result = new HashSet<String>();
		
		Set<Node> nodes = new HashSet<Node>();
		for(Triple triple : viewDef.getConstructPattern()) {
			nodes.add(triple.getSubject());
		}

		for(Node node : nodes) {
			if(node.isVariable()) {
				Set<String> prefixes = getKnownPrefixes((Var)node, viewDef);
				result.addAll(prefixes);
			} else if(node.isURI()) {
				result.add(node.getURI());
			}
		}
		
		return result;
	}
	
	public Set<String> getKnownPrefixes(Config sparqlifyConfig) {
		Set<String> result = new HashSet<String>();
		
		for(ViewDefinition viewDef : sparqlifyConfig.getViewDefinitions()) {
			Set<String> prefixes = getKnownPrefixes(viewDef);
			
			result.addAll(prefixes);
		}
		
		return result;
	}
	
	public static Set<String> extractHostNames(Set<String> prefixes) {
		Set<String> result = new HashSet<String>();
		for(String prefix : prefixes) {
			try {
				URL url = new URL(prefix);
				String hostname = url.getProtocol() + "://" + url.getHost() + "/";
				result.add(hostname);
			} catch(Exception e) {
				logger.warn("Failed to extract hostname from: [" + prefix +"]");
			}
		}

		return result;
	}
	
	public void autoconfigure(Model model, Resource config) {
		Set<String> prefixes = getKnownPrefixes(sparqlifyConfig);
		Set<String> hostnames = PubbyConfigFactory.extractHostNames(prefixes);

		System.out.println("Prefixes: " + prefixes);
		System.out.println("Hostnames: " + hostnames);
		
		
		for(String hostname : hostnames) {
			writeDatasetDesc(model, config, hostname);
		}
	}
	
	
	public void writeDatasetDesc(Model model, Resource parentConfig, String prefix) {
		
		Resource dataset = model.createResource(new AnonId());
		Resource datasetBase = model.createResource(prefix);
		
		model.add(parentConfig, CONF.dataset, dataset);
		
		
		Resource pubbySparqlEndpoint = model.createResource(baseUrl + "sparql");
		//PubbyConfigFactory.
		
		model.add(dataset, CONF.sparqlEndpoint, pubbySparqlEndpoint);
		model.add(dataset, CONF.datasetBase, datasetBase);
		model.add(dataset, CONF.fixUnescapedCharacters, model.createLiteral("(),'!$&*+;=@"));
		
		/*
		#   conf:dataset [
		#        conf:sparqlEndpoint <http://localhost:7531/sparql>;
		#        conf:sparqlDefaultGraph <http://example.org>;
		#        conf:datasetBase <http://your-dataset-namespace.org/>;
		#        conf:fixUnescapedCharacters "(),'!$&*+;=@";
		#       ];
*/
		
		
		
	}
	

	public Configuration create() {
		//Model baseModel = FileManager.get().loadModel(baseConfigFile.getAbsoluteFile().toURI().toString()); 
		
		for(int i = 0; i < 15; ++i) {
			System.out.println("__________________________________________________________" + this.getClass().getName());
		}
		
		//Model model = baseModel;
			
		
		Model model = ModelFactory.createDefaultModel();
		
		model.setNsPrefixes(sparqlifyConfig.getPrefixMapping());

		Resource config = model.createResource("urn://sparqlify/platform/pubby/config");

		model.add(config, RDF.type, CONF.Configuration);
		model.add(config, CONF.webBase, model.createResource(baseUrl + "pubby/"));
		model.add(config, CONF.projectName, model.createLiteral(projectName));
		model.add(config, CONF.projectHomepage, model.createResource(projectHomepage));

		
		autoconfigure(model, config);
		
		model.write(System.out, "TURTLE");
		
		Configuration result = new Configuration(model);
		

		return result;
	}
}