package exp.org.aksw.sparqlify.core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.SparqlSqlStringRewrite;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.util.ExprRewriteSystem;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.ViewDefinitionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;



public class SparqlifyTestFacade {

	private ViewDefinitionFactory vdFactory;
	private CandidateViewSelector cvs;
	private SparqlSqlStringRewriter rewriter;


	public SparqlifyTestFacade(ViewDefinitionFactory vdFactory, CandidateViewSelector cvs, SparqlSqlStringRewriter rewriter) {
		this.cvs = cvs;
		this.vdFactory = vdFactory;
		this.rewriter = rewriter;
	}
	
	
	public void addView(String viewDefStr) {
		ViewDefinition viewDef = vdFactory.create(viewDefStr);

		cvs.addView(viewDef);
	}
	
	
	public SparqlSqlStringRewrite rewrite(String queryStr) {
		Query query = new Query();
		QueryFactory.parse(query, queryStr, "http://ex.org/", Syntax.syntaxSPARQL_11);

		SparqlSqlStringRewrite result = rewriter.rewrite(query);

		return result;
	}
	
	
	/**
	 * Create a simple facade initialized with an embedded test database.
	 * 
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static SparqlifyTestFacade createWithTestDb()
			throws SQLException, IOException
	{
		DataSource dataSource = SparqlifyUtils.createTestDatabase(); 
		Connection conn = dataSource.getConnection();

		Schema databaseSchema = Schema.create(conn);
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		
		
		ViewDefinitionFactory vdFactory = SparqlifyUtils.createViewDefinitionFactory(conn, typeAlias);
		
		//TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
		ExprRewriteSystem ers = SparqlifyUtils.createExprRewriteSystem();
		TypeSystem typeSystem = ers.getTypeSystem();

		
		//OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());

		SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(cvs, opMappingRewriter, vdFactory.getDatatypeSystem(), databaseSchema);
	
		SparqlifyTestFacade result = new SparqlifyTestFacade(vdFactory, cvs, rewriter);
			
		return result;
	}
}

