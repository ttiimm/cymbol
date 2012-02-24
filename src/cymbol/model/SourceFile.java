package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

public class SourceFile extends OutputModelObject {

    public String name;
    
    @ModelElement public List<Struct> structs = new ArrayList<Struct>();
    @ModelElement public List<VariableDeclaration> vars = new ArrayList<VariableDeclaration>();
    @ModelElement public List<FunctionDeclaration> funcDefs = new ArrayList<FunctionDeclaration>();

    public SourceFile(String sourceName) {
       this.name = sourceName;
    }

    public void add(Struct struct) {
        structs.add(struct);
    }
    
    public void add(VariableDeclaration var) {
        vars.add(var);
    }

    public void add(FunctionDeclaration methodFunction) {
        funcDefs.add(methodFunction);
    }
    
}
