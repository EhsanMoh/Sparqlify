package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Template;

public class ViewTemplateDefinition {
	//private String name;
	
	private Template constructTemplate;
	
	// FIXME: Replace with Map<Var, Expr>
	private List<Expr> varBindings;

	/*
	public ViewTemplateDefinition()
	{
		constructTemplate = new Template(null);
		varBindings =  new ArrayList<Expr>();
	}*/
	
	public ViewTemplateDefinition() {
		this.constructTemplate = new Template(new BasicPattern());
		this.varBindings = new ArrayList<Expr>();
	}
	
	public ViewTemplateDefinition(Template constructTemplate, List<Expr> varBindings) {
		//this.name = name;
		this.constructTemplate = constructTemplate;
		this.varBindings = varBindings;
	}
	
	/*
	public String getName()
	{
		return name;
	}
	*/
	
	public Template getConstructTemplate() {
		return constructTemplate;
	}
	public void setConstructTemplate(Template constructTemplate) {
		this.constructTemplate = constructTemplate;
	}
	public List<Expr> getVarBindings() {
		return varBindings;
	}
	
	public VarExprList getVarExprList() {
		VarExprList result = new VarExprList();
		
		 for(Expr item : this.getVarBindings()) {
//			 if(!(item instanceof E_Equals)) {
//				 throw new RuntimeException("Expected E_Equals");
//			 }
			 
			 E_Equals e = (E_Equals)item;
			 Expr left = e.getArg1();
			 if(!left.isVariable()) {
				 throw new RuntimeException("Variable expected, instead got: " + left);
			 }
			 
			 Var var = left.asVar();
			 Expr expr = e.getArg2();
			 
			 result.add(var, expr);
		 }
		
		 return result;
	}
	
	
	public void setVarBindings(List<Expr> varBindings) {
		this.varBindings = varBindings;
	}
	@Override
	public String toString() {
		return "ViewTemplateDefinition [constructTemplate=" + constructTemplate
				+ ", varBindings=" + varBindings + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constructTemplate == null) ? 0 : constructTemplate
						.hashCode());
		result = prime * result
				+ ((varBindings == null) ? 0 : varBindings.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewTemplateDefinition other = (ViewTemplateDefinition) obj;
		if (constructTemplate == null) {
			if (other.constructTemplate != null)
				return false;
		} else if (!constructTemplate.equals(other.constructTemplate))
			return false;
		if (varBindings == null) {
			if (other.varBindings != null)
				return false;
		} else if (!varBindings.equals(other.varBindings))
			return false;
		return true;
	}
}