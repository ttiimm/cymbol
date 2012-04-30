package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.Symbol;

public class Block extends OutputModelObject {
    
    @ModelElement public List<Struct> structs = new ArrayList<Struct>();
    @ModelElement public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    
    public List<OutputModelObject> statements = new ArrayList<OutputModelObject>();
    
    public void prepend(Statement statement) {
        statements.add(0, statement);
    }
    
    public void add(OutputModelObject omo) {
        if(omo instanceof Struct) {
            structs.add((Struct) omo);
        } else if(omo instanceof VariableDeclaration) {
            vars.add((VariableDeclaration) omo);
        } else {
            statements.add(omo);
        }
    }

    public void addAll(List<OutputModelObject> all) {
        for(OutputModelObject omo : all) {
            add(omo);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        if(structs.size() > 0) { sb.append("    " + Symbol.stripBrackets(structs.toString()) + "\n"); }
        if(vars.size() > 0) { 
            for(VariableDeclaration var : vars){ sb.append("    "); sb.append(var.toString() + "\n");} 
        }
        if(statements.size() > 0) { sb.append("    " + Symbol.stripBrackets(statements.toString()) + "\n"); } 
        sb.append("}\n");
            
        return sb.toString();
    }
}