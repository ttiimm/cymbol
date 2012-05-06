package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.model.Expressions.Expression;
import cymbol.model.Primitives.Primitive;

public class Expressions {

    public abstract static class Expression extends OutputModelObject {
        
        public Expression() {
        }

        public abstract String getExpr(); 
        public abstract String getPrimitiveExpr();
    }
    
    public static class GenericExpression extends Expression {

        private String expr;

        public GenericExpression(String theExpression) {
            this.expr = theExpression;
        }

        @Override
        public String getExpr() {
            return expr;
        }

        @Override
        public String getPrimitiveExpr() {
            return expr;
        }
    }
    
    public static class ConstructorExpression extends Expression {

        private String expr;

        public ConstructorExpression(String constructorCall) {
            this.expr = "new_" + constructorCall;
        }

        @Override
        public String getExpr() {
            return expr;
        }

        @Override
        public String getPrimitiveExpr() {
            return expr;
        }

    }
    
    public static class PrimaryExpression extends Expression {

        public Primitive primitive;
        
        public PrimaryExpression(Primitive literal) {
            this.primitive = literal;
        }

        public boolean isBuiltin() {
            return primitive.builtin;
        }
        
        @Override
        public String getExpr() {
            return primitive.getObj();
        }

        @Override
        public String getPrimitiveExpr() {
            return primitive.getPrimitive();
        }

    }
    
    public static class AccessExpression extends Expression {

        private Expression struct;
        private Expression member;

        public AccessExpression(Expression struct, Expression member) {
            this.struct = struct;
            this.member = member;
        }
        
        @Override
        public String getExpr() {
            return struct.getExpr() + "->" + member.getExpr();
        }

        @Override
        public String getPrimitiveExpr() {
            return struct.getExpr() + "->" + member.getPrimitiveExpr();
        }
        
    }

}
