package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cymbol.compiler.Compiler;

public class TestBuildPhase {

    @Test
    public void sourceFile() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        assertEquals("<String>", c.src.name);
    }

    @Test
    public void globalVars() {
        String source = "int x;" +
        		        "char c;" +
        		        "float f;" +
        		        "boolean b[];";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(4, c.src.vars.size());
    }
    
    @Test
    public void globalStructs() {
        String source = "struct A { int x; }" +
        		        "struct B { int y; }";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(2, c.src.structs.size());
    }
    
    @Test
    public void globalMethodFuncs() {
        String source = "void foo(){ }" +
                        "char bar(){ }" +
                        "float baz(){ }";
        Compiler c = Util.runCompilerOn(source);
        assertEquals(3, c.src.funcDefs.size());
    }
}
