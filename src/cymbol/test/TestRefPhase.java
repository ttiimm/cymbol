package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.symtab.SymbolTable;

public class TestRefPhase {

    @Test
    public void testReferenceGlobalVars() {
        String source = "int a;" +
        		        "char b;" +
        		        "float c;" +
        		        "struct D { int x; }" +
        		        "D d;" +
        		        "boolean e;";
        SymbolTable t = runTest(source);
        assertEquals("<global.a:global.int>", t.globals.resolve("a").toString());
        assertEquals("<global.b:global.char>", t.globals.resolve("b").toString());
        assertEquals("<global.c:global.float>", t.globals.resolve("c").toString());
        assertEquals("<global.d:struct D:{x}>", t.globals.resolve("d").toString());
        assertEquals("<global.e:global.boolean>", t.globals.resolve("e").toString());
    }

    public static SymbolTable runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler();
        ParseTree t = c.constructParseTree(in);
        SymbolTable table = c.define(t);
        c.reference(t, table);

        return table;
    }
}
