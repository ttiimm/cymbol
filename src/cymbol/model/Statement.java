package cymbol.model;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;

public interface Statement {

    public class Block extends OutputModelObject {
        
        public List<OutputModelObject> statements = new ArrayList<OutputModelObject>();
        
        public void add(OutputModelObject omo) {
            statements.add(omo);
        }
    }
}
