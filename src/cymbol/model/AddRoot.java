package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public class AddRoot extends OutputModelObject {

    private String varName;

    public AddRoot(VariableDeclaration varDecl) {
        this.varName = varDecl.name;
    }

    @Override
    public String toString() {
        return "ADD_ROOT(" + varName + ");";
    }

}
