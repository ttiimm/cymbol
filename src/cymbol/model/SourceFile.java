package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.model.Primitives.Primitive;
import cymbol.symtab.Symbol;

public class SourceFile extends OutputModelObject {

    public String name;
    
    public List<Struct> structs = new ArrayList<Struct>();
    public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    
    public List<Primitive> stringLiterals = new ArrayList<Primitive>();
    public List<Primitive> intLiterals = new ArrayList<Primitive>();
    
    @ModelElement public List<FunctionDeclarations> functionDeclarations = new ArrayList<FunctionDeclarations>();
    @ModelElement public List<FunctionDefinitions> functionDefinitions = new ArrayList<FunctionDefinitions>();

    public SourceFile(String sourceName) {
       this.name = sourceName;
    }
    
    public void addAll(List<OutputModelObject> all) {
        for(OutputModelObject omo : all) {
            if(omo instanceof Struct) { add((Struct) omo); }
            else if(omo instanceof VariableDeclaration) { add((VariableDeclaration) omo); }
            else if(omo instanceof MethodFunction){ add((MethodFunction) omo); }
            else { throw new IllegalStateException("Tried to add " + omo + " to source file"); }
        }
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
    
    public int getStringLiteralSize() {
        return stringLiterals.size();
    }
    
    public int getIntLiteralSize() {
        return intLiterals.size();
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
