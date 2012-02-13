package cymbol.test;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;

public class TestSemantics {

    
    @Test 
    public void testDefineVarWithIllegalForwardRef() {
        String source = "void foo() { " +
                        "     x = 3;" +
                        "     int x;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        Scope local = Util.resolveLocalScope(m);
        assertEquals("local.x", local.resolve("x"));
    }
    
    public Compiler runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler(in);
        c.compile();

        return c;
    }

}
