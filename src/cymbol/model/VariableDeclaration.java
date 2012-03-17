package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class VariableDeclaration extends OutputModelObject {
    
    public Type type;
    public String name;
    public boolean isArray = false;
    public OutputModelObject expr;
    
    public VariableDeclaration(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public VariableDeclaration(VariableSymbol symbol) {
        this.type = symbol.type;
        this.name = symbol.getName();
        this.isArray = symbol.isArray;
    }
    
    public void add(OutputModelObject expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        String left = type.getName() + " " + name;
        if(isArray) { left += "[]"; }
        String right = expr != null ? " = " + expr : "";
        return left + right + ";";
    }
}
