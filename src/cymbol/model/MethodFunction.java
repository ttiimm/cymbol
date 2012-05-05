package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;

public class MethodFunction extends OutputModelObject {
    
    public Symbol symbol;
    public Type type; 
    public String name;
    public List<Argument> args = new ArrayList<Argument>();
    @ModelElement public Block block;
  
    public MethodFunction(Type type, String name, Block block) {
        this.symbol = new MethodSymbol(name, null, null);
        this.symbol.type = type;
        this.type = type;
        this.name = name;
        this.block = block;
    }
    
    public MethodFunction(Symbol symbol, Block block) {
        this.symbol = symbol;
        this.type = symbol.type;
        this.block = block;
        String methodName = symbol.getName();
        int paranIndex = methodName.indexOf("(");
        this.name = methodName.substring(0, paranIndex);
        
        MethodSymbol m = (MethodSymbol) symbol;
        for(Symbol s : m.getMembers().values()) {
            args.add(new Argument(s));
        }
    }

    public String getReturnPointer() {
        return type == SymbolTable.VOID ? "" : "*";
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
