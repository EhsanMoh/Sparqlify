package org.aksw.sparqlify.compile.sparql;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.nodes.SqlAlias;
import org.aksw.sparqlify.algebra.sql.nodes.SqlDistinct;
import org.aksw.sparqlify.algebra.sql.nodes.SqlGroup;
import org.aksw.sparqlify.algebra.sql.nodes.SqlJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlMyRestrict;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOrder;
import org.aksw.sparqlify.algebra.sql.nodes.SqlProjection;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlUnionN;


/**
 * Pitfalls:
 *     (distinct (project ...)) -> First project then make distinct
 *         -> Select Distinct [project]
 *     But
 *     (project (distinct ...)) -> First make distinct (with potentially some different projection),
 *     then project:
 *     Select [projection] FROM (Select Distinct * From ...)    
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlSelectBlockCollector {

	public static void copyProjection(SqlNodeOld target, SqlNodeOld source) {
		target.getSparqlVarToExprs().clear();
		target.getAliasToColumn().clear();
		
		
		target.getSparqlVarToExprs().putAll(source.getSparqlVarToExprs());
		target.getAliasToColumn().putAll(source.getAliasToColumn());		
	}
	
	public static SqlSelectBlock _makeSelect(SqlNodeOld node) {
		SqlSelectBlock result = MultiMethod.invokeStatic(SqlSelectBlockCollector.class, "makeSelect", node);
		
		copyProjection(result, node);
		
		return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlGroup node) {
		SqlSelectBlock result;
		
		if(node.getSubNode() instanceof SqlSlice) {
			
			SqlSelectBlock tmp = _makeSelect(node.getSubNode());

			result = new SqlSelectBlock(tmp.getAliasName(), tmp);
			copyProjection(result, result);

			
		}
		else {
			result = _makeSelect(node.getSubNode());
		}

		System.err.println("TODO Handle group by vars if present");
		
		return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlTable node) {
		return new SqlSelectBlock(node);
	}
	
	public static SqlSelectBlock makeSelect(SqlQuery node) {
		return new SqlSelectBlock(node);
	}
	
	public static SqlSelectBlock makeSelect(SqlMyRestrict node) {
		SqlSelectBlock result = _makeSelect(node.getSubNode());
		result.getConditions().addAll(node.getConditions());
		
		return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlSlice node) {
		SqlSelectBlock result = _makeSelect(node.getSubNode());
    	SqlSelectBlock.slice(null, result, node.getStart(), node.getLength());
		
		return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlDistinct node) {
		SqlSelectBlock result = _makeSelect(node.getSubNode());
		result.setDistinct(true);
		
		// Sort conditions must now become part of the projection
		
		
		return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlAlias node) {
		
		if(true) {
			SqlSelectBlock result = _makeSelect(node.getSubNode());
			//result.setAliasName(node.getAliasName());
			
			SqlSelectBlock wrap = new SqlSelectBlock(node.getAliasName(), result);
			copyProjection(wrap, result);
			
			return wrap;

			/*
			copyProjection(result, node);
			result.setAliasName(node.getAliasName());
			return result;
			*/
		}
		//throw new RuntimeException("Should not come here");
		

    	SqlNodeOld result = _makeSelectOrTable(node.getSubNode());    	
    	

		SqlSelectBlock wrap = new SqlSelectBlock(node.getAliasName(), result);
		copyProjection(wrap, result);
		
		return wrap;
		
		
		/*
		wrap.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
		wrap.getAliasToColumn().putAll(node.getAliasToColumn());

    	result.setAliasName(node.getAliasName());
    	
    	return result;
    	*/
	}
	
	public static SqlSelectBlock makeSelect(SqlNodeOrder node) {
    	SqlSelectBlock result = _makeSelect(node.getSubNode());    	
    	result.getSortConditions().addAll(node.getConditions());
    	
    	copyProjection(result, node);
    	
    	return result;
	}
	
	public static SqlSelectBlock makeSelect(SqlProjection node) {
		SqlSelectBlock result = _makeSelect(node.getSubNode());

		// If the node is distinct, or if it already has a projection set,
		// we must create a subselect
		if(result.isDistinct()) { // || result.isResultStar()
			//SqlSelectBlock wrapped = new 
			
		}
		
		SqlSelectBlock wrap = new SqlSelectBlock(node.getAliasName(), result);
		wrap.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
		wrap.getAliasToColumn().putAll(node.getAliasToColumn());
		
		
		return wrap;
	}
	
	public static SqlSelectBlock makeSelect(SqlJoin node) {
		SqlNodeOld join = makeSelectOrTable(node);
		
		return new SqlSelectBlock(join);
	}
	
	public static SqlSelectBlock makeSelect(SqlUnionN node) {
    	// Wrap all elements of the union
    	List<SqlNodeOld> wrapped = new ArrayList<SqlNodeOld>();
    	for(SqlNodeOld arg : node.getArgs()) {
    		SqlSelectBlock argSelect = _makeSelect(arg);
    		
    		/*
    		argSelect.getSparqlVarToExprs().clear();
    		argSelect.getAliasToColumn().clear();
    		
    		
    		argSelect.getSparqlVarToExprs().putAll(arg.getSparqlVarToExprs());
    		argSelect.getAliasToColumn().putAll(arg.getAliasToColumn());
    		*/
    		
    		
    		wrapped.add(argSelect);
    	}
    	    	
    	SqlUnionN union = new SqlUnionN(node.getAliasName(), wrapped);

    	union.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
    	union.getAliasToColumn().putAll(node.getAliasToColumn());

    	SqlSelectBlock result = new SqlSelectBlock(union);
    	copyProjection(result, union);
    	
    	return result;
	}
		
	public static SqlNodeOld _makeSelectOrTable(SqlNodeOld node) {
		return MultiMethod.invokeStatic(SqlSelectBlockCollector.class, "makeSelectOrTable", node);		
	}

	public static SqlNodeOld makeSelectOrTable(SqlNodeEmpty node) {
		// Should never come here
		return node;
	}

	public static SqlNodeOld makeSelectOrTable(SqlProjection node) {
		return _makeSelect(node);
	}

	public static SqlNodeOld makeSelectOrTable(SqlTable node) {
		return node;
	}
	
	public static SqlNodeOld makeSelectOrTable(SqlQuery node) {
		return node;
	}
	
	public static SqlNodeOld makeSelectOrTable(SqlAlias node) {
    	SqlNodeOld tmp = _makeSelectOrTable(node.getSubNode());    	
		
    	SqlAlias result = new SqlAlias(node.getAliasName(), tmp);
    	copyProjection(result, node);
    	
    	return result;
	}
	
	public static SqlNodeOld makeSelectOrTable(SqlMyRestrict node) {
		return _makeSelect(node);		
	}

	public static SqlNodeOld makeSelectOrTable(SqlSlice node) {		
		return _makeSelect(node);
	}

	public static SqlNodeOld makeSelectOrTable(SqlDistinct node) {		
		return _makeSelect(node);
	}

	
	public static SqlNodeOld makeSelectOrTable(SqlJoin node) {
		SqlNodeOld left = _makeSelectOrTable(node.getLeft());
		SqlNodeOld right = _makeSelectOrTable(node.getRight());
		
		SqlJoin join = SqlJoin.create(node.getJoinType(), left, right);
		join.getConditions().addAll(node.getConditions());
		copyProjection(join, node);

		return join;
	}
	
	public static SqlNodeOld makeSelectOrTable(SqlUnionN node) {
		return makeSelect(node);
	}
	
}

