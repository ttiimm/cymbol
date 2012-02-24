package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.model.VariableDeclaration;

public class TestBuildPhase {

    @Test
    public void sourceFile() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        assertEquals("<String>", c.src.name);
    }

    @Test
    public void globalVar() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        VariableDeclaration var = c.src.vars.get(0);
        assertEquals("int", var.type.getName());
        assertEquals("x", var.name);
        
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
