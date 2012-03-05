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
    
    class Binary extends OutputModelObject {
        
        private Expression left;
        private Expression right;
        private String op;
        
        public Binary(Expression left, String op, Expression right) {
            this.left = left;
            this.right = right;
            this.op = op;
        }
     
        @Override
        public String toString() {
            return left.toString() + op + right.toString();
        } 
    }
}
