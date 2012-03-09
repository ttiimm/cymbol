package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.Symbol;
import cymbol.symtab.Type;

public class VariableDeclaration extends OutputModelObject {
    
    public Type type;
    public String name;
    public OutputModelObject expr;
    
    public VariableDeclaration(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public VariableDeclaration(Symbol symbol) {
        this.type = symbol.type;
        this.name = symbol.getName();
    }
    
    public void add(OutputModelObject expr) {
        this.expr = expr;
    }

    @Override
    public String toString() {
        String left = type.getName() + " " + name;
        String right = expr != null ? " = " + expr : "";
        return left + right + ";";
    }
}
