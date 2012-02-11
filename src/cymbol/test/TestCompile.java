package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import cymbol.compiler.Compiler;

public class TestCompile {

    @Test
    public void testParseFailure() {
        String source = "class A { }";
        Compiler c = runTest(source);
        assertEquals("Error parsing <String>", c.errors.get(0));
        System.err.println("[TestCompile] Parsing failure expected.");
    }
    
    public Compiler runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<String>";
        Compiler c = new Compiler(in);
        
        if(c.errors.size() == 0) { c.compile(); }
        else { System.err.println("[TestCompile] Problem parsing input, no compilation performed."); }

        return c;
    }

}
