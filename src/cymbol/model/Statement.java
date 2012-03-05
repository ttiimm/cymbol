package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;

public interface Statement {

    public class Block extends OutputModelObject implements Statement {
        
        public List<Statement> statements = new ArrayList<Statement>();
        
        public void add(Statement statement) {
            statements.add(statement);
        }
    }
}
