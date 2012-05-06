package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public class Statement extends OutputModelObject {

    public String theStatement;
    public boolean isReturn;

    public Statement(String theStatement, boolean isReturn) {
        this.theStatement = theStatement;
        this.isReturn = isReturn;
    }

    @Override
    public String toString() {
        return theStatement;
    }
    
}


