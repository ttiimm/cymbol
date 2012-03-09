package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public class Statement extends OutputModelObject {

    public String theStatement;

    public Statement(String theStatement) {
        this.theStatement = theStatement;
    }

    @Override
    public String toString() {
        return theStatement;
    }
    
}


