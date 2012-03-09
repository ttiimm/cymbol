package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public class Expression extends OutputModelObject {
    
    public String theExpression;

    public Expression(String theExpression) {
        this.theExpression = theExpression;
    }

    @Override
    public String toString() {
        return theExpression;
    }
    
}
