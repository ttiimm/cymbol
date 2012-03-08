package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public interface Statement {

    public boolean isStruct();

    public boolean isVariableDeclaration();
    
}

abstract class NonDeclarationStatement extends OutputModelObject implements Statement{
    
    public boolean isStruct() { return false; }

    public boolean isVariableDeclaration() { return false; }
}

