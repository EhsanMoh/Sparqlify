package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/**
 * This transformer is mainly intended for "local" transforms
 * of functions.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RdfTermEliminatorImpl
	implements RdfTermEliminatorWriteable
{
	
	private static final Logger logger = LoggerFactory.getLogger(RdfTermEliminatorImpl.class);
	
	private Map<String, ExprTransformer> idToTransformer = new HashMap<String, ExprTransformer>();


	public RdfTermEliminatorImpl() {
	}
	
	public RdfTermEliminatorImpl(Map<String, ExprTransformer> idToTransformer) {
		this.idToTransformer = idToTransformer;
	}
	
	public Map<String, ExprTransformer> getTransformerMap() {
		return idToTransformer;
	}
	
	
	public ExprTransformer lookup(ExprFunction fn) {

		String id = ExprUtils.getFunctionId(fn);
		ExprTransformer result = idToTransformer.get(id);

		return result;
	}
	
	private static Set<Expr> seenErrors = new HashSet<Expr>();
	
	@Override
	public E_RdfTerm _transform(Expr expr) {
		
		//System.out.println("Transforming: " + expr);
		
		E_RdfTerm result;
		
		if(expr.isFunction()) {
			/*
			E_RdfTerm rdfTerm = SqlTranslationUtils.expandRdfTerm(expr);
			if(rdfTerm != null) {
				result = transform(rdfTerm);
			} else {
				ExprFunction e = expr.getFunction();
				result = transform(e);
			}*/
			
			ExprFunction e = expr.getFunction();
			result = transform(e);
			
		}
		else if(expr.isConstant()) {
			NodeValue e = expr.getConstant();
			result = transform(e); 			
		}
		else if(expr.isVariable()) {
			ExprVar e = (ExprVar)expr;
			result = transform(e);
		} else {
			throw new RuntimeException("Should not happen");
		}
		
		//System.out.println("Transformation Result: " + result);

		return result;
	}
	
	public E_RdfTerm transform(NodeValue expr) {
		E_RdfTerm result = SqlTranslationUtils.expandConstant(expr);
		return result;
	}
	
	public E_RdfTerm transform(ExprVar expr) {
		E_RdfTerm result = E_RdfTerm.createVar(expr);
		return result;
	}
	
	
	public E_RdfTerm transform(ExprFunction fn) {
		List<Expr> args = fn.getArgs();
		List<E_RdfTerm> newArgs = new ArrayList<E_RdfTerm>(args.size());

		for(Expr arg : args) {		
			E_RdfTerm newArg = _transform(arg);
			newArgs.add(newArg);			
		}
		
		//ExprTransformer transformer = lookup(fn);
	    String id = ExprUtils.getFunctionId(fn);
	    ExprTransformer transformer = idToTransformer.get(id);

		if(transformer == null) {
			throw new RuntimeException("No transformer registered for " + id + " in expression "+ fn);
		}

		E_RdfTerm result = transformer.transform(fn, newArgs);

		if(result == null) {
			throw new RuntimeException("Transformer " + fn + " returned null - must not happen.");
		}
		
		return result;
	}

	@Override
	public void register(String functionSymbol, ExprTransformer exprTransformer) {
		idToTransformer.put(functionSymbol, exprTransformer);
	}
}
