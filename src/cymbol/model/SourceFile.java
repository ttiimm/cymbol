package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.model.Statement.Block;
import cymbol.symtab.Symbol;

public class SourceFile extends OutputModelObject {

    public String name;
    
    public List<Struct> structs = new ArrayList<Struct>();
    public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    
    @ModelElement public List<FunctionDeclarations> functionDeclarations = new ArrayList<FunctionDeclarations>();
    @ModelElement public List<FunctionDefinitions> functionDefinitions = new ArrayList<FunctionDefinitions>();

    public SourceFile(String sourceName) {
       this.name = sourceName;
    }

    public void add(Struct struct) {
        structs.add(struct);
    }
    
    public void add(VariableDeclaration var) {
        vars.add(var);
    }

    public void add(MethodFunction func) {
        functionDeclarations.add(new FunctionDeclarations(func.symbol, func.block));
        functionDefinitions.add(new FunctionDefinitions(func.symbol, func.block));
    }
    
    public class FunctionDeclarations extends MethodFunction {

        public FunctionDeclarations(Symbol symbol, Block block) {
            super(symbol, block);
        } 

    }
    
    public class FunctionDefinitions extends MethodFunction {

        public FunctionDefinitions(Symbol symbol, Block block) {
            super(symbol, block);
        }
    }
}
