package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.model.Expressions.Expression;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class VariableDeclaration extends OutputModelObject {
    
    public Type type;
    public String name;
    public boolean isArray = false;
    public Expression expr;
    
    public VariableDeclaration(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public VariableDeclaration(VariableSymbol symbol) {
        this.type = symbol.type;
        this.name = symbol.getName();
        this.isArray = symbol.isArray;
    }
    
    public void add(Expression expr) {
        this.expr = expr;
    }
    
    public String getTypeName() {
        return Util.capitalize(type.getName());
    }

    @Override
    public String toString() {
        String typeName = Util.capitalize(type.getName());
        String left = typeName + " *" + name;
        if(isArray) { left += "[]"; }
        String right = expr != null ? " = " + expr.getExpr() : "";
        return left + right + ";";
    }

}
