package cymbol.test;

import static org.junit.Assert.assertTrue;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.symtab.Scope;

public class TestSemantics {

    @Test 
    public void testDefineVarWithIllegalForwardRef() {
        String source = "void foo() { " +
                        "     x = 3;" +
                        "     int x;" +
                        "}";
        Compiler c = runCompilerOn(source);
        assertTrue(c.errors.size() > 0);
    }
    
    
    public Compiler runCompilerOn(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<String>";
        Compiler c = new Compiler(in);
        ParseTreeProperty<Scope> scopes = c.define();
        c.resolve(scopes);
        
        return c;
    }
    
}
