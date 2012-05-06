package cymbol.model;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;

public class Primitives {

    public abstract static class Primitive extends OutputModelObject {
        
        public boolean builtin = false;
        
        public Primitive() {
        }
        
        public abstract String getPrimitive();
        public abstract String getObj();
    }

    public static class GenericPrimitive extends Primitive {

        public String text;
        private Type type;

        public GenericPrimitive(Type type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String getPrimitive() {
            if(type == SymbolTable.INT) {
                return text + "->value";
            } else if(type == SymbolTable.STRING) {
                return text + "->elements";
            } else if(type == SymbolTable.NULL) {
                return "NULL";
            } else {
                return text;                
            }
        }

        @Override
        public String getObj() {
            if(type == SymbolTable.NULL) {
                return "NULL";
            } else {
                return text;
            }
        }

    }
    
    public static class MethodPrimitive extends Primitive {
        public String text;

        public MethodPrimitive(String text, boolean builtin) {
            this.text = text;
            this.builtin = builtin;
        }

        @Override
        public String getPrimitive() {
            return text;
        }

        @Override
        public String getObj() {
            return text;
        }
    }
    
    public static class StringLiteral extends Primitive {

        public String text;
        public int id;

        public StringLiteral(String text, int id) {
            this.text = text;
            this.id = id;
        }
        
        public String getPrimitive() {
            return "_String_literals[" + id + "]->elements";
        }
        
        public String getObj() {
            return "_String_literals[" + id + "]";
        }
        
    }
    
    public static class IntLiteral extends Primitive {

        public String text;
        public int id;

        public IntLiteral(String text, int id) {
            this.text = text;
            this.id = id;
        }

        @Override
        public String getPrimitive() {
            return "_Int_literals[" + id + "]->value";
        }

        @Override
        public String getObj() {
            return "_Int_literals[" + id + "]";
        }

    }
    
}
