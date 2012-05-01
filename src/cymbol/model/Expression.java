package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.model.Literals.Literal;

public class Expression extends OutputModelObject {
    
    public String theExpression;
    private String underlying;

    public Expression(OutputModelObject literal) {
        Literal theLiteral = (Literal) literal;
        this.theExpression = theLiteral.getLiteral();
        this.underlying = theLiteral.underlying();
    }
    
    public Expression(String theExpression) {
        this.theExpression = theExpression;
        this.underlying = theExpression;
    }
    
    public String getExpr() {
        return theExpression;
    }
    
    public String getLiteralExpr() {
        return underlying;
    }
}
