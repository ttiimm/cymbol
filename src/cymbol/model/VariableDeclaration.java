package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

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
        return capitalize(type.getName());
    }

    @Override
    public String toString() {
        String typeName = capitalize(type.getName());
        String left = typeName + " " + name;
        if(isArray) { left += "[]"; }
        String right = expr != null ? " = " + expr.getExpr() : "";
        return left + right + ";";
    }

    private String capitalize(String str) {
        String firstLetter = str.substring(0, 1);
        String rest = str.substring(1);
        return firstLetter.toUpperCase() + rest;
    }
}
