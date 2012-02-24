package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;

public class FunctionDeclaration extends OutputModelObject{
    
    public Type type; 
    public String name;
    public List<Argument> args = new ArrayList<Argument>();

    public FunctionDeclaration(Type type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public FunctionDeclaration(Symbol symbol) {
        this.type = symbol.type;
        String methodName = symbol.getName();
        int paranIndex = methodName.indexOf("(");
        this.name = methodName.substring(0, paranIndex);
        MethodSymbol m = (MethodSymbol) symbol;
        for(Symbol s : m.getMembers().values()) {
            args.add(new Argument(s));
        }
    }

    @Override
    public String toString() {
        return type.getName() + " " + name + args;
    }
    
    public class Argument {
        public Type type; 
        public String name;
        
        public Argument(Symbol s) {
            this.type = s.type;
            this.name = s.getName();
        }

        @Override
        public String toString() {
            return type.getName() + " " + name;
        }
    }

}
