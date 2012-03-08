package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.OutputModelObject;

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
}