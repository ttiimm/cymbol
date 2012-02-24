package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import cymbol.compiler.Compiler;

public class TestBuildPhase {

    @Test
    public void compilationUnitSource() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        assertNotNull(c.compSource);
    }

    @Test
    public void globalVars() {
        String source = "int x;" +
        		        "char c;" +
        		        "float f;" +
        		        "boolean b[];";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(4, c.compSource.globalVars.size());
    }
    
    @Test
    public void globalStructs() {
        String source = "struct A { int x; }" +
        		        "struct B { int y; }";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(2, c.compSource.globalStructs.size());
    }
    
    @Test
    public void globalMethodFuncs() {
        String source = "void foo(){ }" +
                        "char bar(){ }" +
                        "float baz(){ }";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(3, c.compSource.globalMethodFuncs.size());
    }
}
