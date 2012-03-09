package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.Symbol;

public class Block extends NonDeclarationStatement {
    
    @ModelElement public List<Struct> structs = new ArrayList<Struct>();
    @ModelElement public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    
    public List<Statement> statements = new ArrayList<Statement>();
    
    public void add(Statement statement) {
        if(statement.isStruct()) {
            structs.add((Struct) statement);
        } else if(statement.isVariableDeclaration()) {
            vars.add((VariableDeclaration) statement);
        } else {
            statements.add(statement);
        }
    }

    public void addAll(List<OutputModelObject> all) {
        for(OutputModelObject omo : all) {
            add((Statement) omo);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        if(structs.size() > 0) { sb.append("    " + Symbol.stripBrackets(structs.toString()) + "\n"); }
        if(vars.size() > 0) { sb.append("    " + Symbol.stripBrackets(vars.toString()) + "\n"); }
        if(statements.size() > 0) { sb.append("    " + Symbol.stripBrackets(statements.toString()) + "\n"); } 
        sb.append("}\n");
            
        return sb.toString();
    }
}