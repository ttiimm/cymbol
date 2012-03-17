package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.BuiltInTypeSymbol;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.VariableSymbol;

public class Struct extends OutputModelObject {

    public String name;
    
    public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    public List<Struct> nested = new ArrayList<Struct>();
    
    public Struct(Symbol struct) {
       this.name = struct.getName();
       
       for(Symbol s : ((StructSymbol) struct).getMembers().values()) {
           if(s instanceof VariableSymbol) { 
               vars.add(new VariableDeclaration((VariableSymbol) s)); 
           } else if (s instanceof StructSymbol) {
               nested.add(new Struct((StructSymbol) s));
           }
       }
    }

    @Override
    public String toString() {
        return "struct " + name + " {\n" +
               "    " + nested.toString() + "\n" +
               "    " + vars.toString() + "\n" +
               "}\n";
    }

    
}
