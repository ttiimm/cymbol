package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

public interface Expression {
    
    class Primary extends OutputModelObject {

        private String value;

        public Primary(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        } 
    }
}
