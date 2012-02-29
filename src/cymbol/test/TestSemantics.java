package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;

public class TestSemantics {

    @Ignore
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
    
}
