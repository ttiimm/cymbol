package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.model.FunctionDeclaration;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;

public class TestBuildPhase {

    @Test
    public void sourceFile() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        assertEquals("<String>", c.src.name);
    }

    @Test
    public void var() {
        String source = "int x;";
        Compiler c = Util.runCompilerOn(source);
        VariableDeclaration var = c.src.vars.get(0);
        assertEquals("int x", var.toString());
    }
    
    @Test
    public void struct() {
        String source = "struct A { int x; }";
        Compiler c = Util.runCompilerOn(source);
        Struct struct = c.src.structs.get(0);
        assertEquals("A", struct.name);
        assertEquals("int x", struct.vars.get(0).toString());
    }
    
    @Test
    public void nestedStruct() {
        String source = "struct A { struct B { int x; } }";
        Compiler c = Util.runCompilerOn(source);
        Struct struct = c.src.structs.get(0);
        assertEquals("A", struct.name);
        Struct nested = struct.nested.get(0);
        assertEquals("B", nested.name);
        assertEquals("int x", nested.vars.get(0).toString());
    }
    
    @Test
    public void func() {
        String source = "void foo(float y){ }";
        Compiler c = Util.runCompilerOn(source);
        FunctionDeclaration f = c.src.funcDefs.get(0);
        assertEquals("void foo[float y]", f.toString());
    }
}
