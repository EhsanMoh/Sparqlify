package org.aksw.sparqlify.core;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.op.Op0;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpQuadPattern2
    extends Op0
{
    private QuadPattern quadPattern;
    
    public OpQuadPattern2(QuadPattern quadPattern) {
        this.quadPattern = quadPattern;
    }
    
    public QuadPattern getPattern() {
        return quadPattern;
    }

    @Override
    public String getName()                 { return Tags.tagQuadPattern ; }
    @Override
    public Op apply(Transform transform)    { throw new RuntimeException("not supported"); } 
    @Override
    public void visit(OpVisitor opVisitor)  { throw new RuntimeException("not supported"); }
    @Override
    public Op0 copy()                        { return new OpQuadPattern2(quadPattern) ; }

    @Override
    public int hashCode()
    { return quadPattern.hashCode(); }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpQuadPattern2 ) ) return false ;
        OpQuadPattern2 opQuad = (OpQuadPattern2)other ;
        return quadPattern.equals(opQuad.quadPattern);
    }

}