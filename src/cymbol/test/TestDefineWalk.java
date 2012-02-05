package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import cymbol.compiler.CymbolDefineListener;
import cymbol.compiler.CymbolLexer;
import cymbol.compiler.CymbolParser;
import cymbol.compiler.StructSymbol;
import cymbol.compiler.SymbolTable;

public class TestDefineWalk {

    private SymbolTable define(String source) {
        ANTLRInputStream input = new ANTLRInputStream(source);
        CymbolLexer l = new CymbolLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(l);
        // System.out.println(tokens.getTokens());
        CymbolParser p = new CymbolParser(tokens);
        p.setBuildParseTree(true);
        ParserRuleContext<Token> t = p.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTable table = new SymbolTable();
        CymbolDefineListener defl = new CymbolDefineListener(table.globals);
        walker.walk(defl, t);
        // System.out.println(((Tree)r.getTree()).toStringTree());

        return table;
    }

    @Test
    public void testSimpleStruct() {
        String source = "struct A { int x; }";
        SymbolTable t = define(source);
        StructSymbol s = (StructSymbol) t.globals.resolve("A");
        assertEquals("struct A:{}", s.toString());
    }

    @Test
    public void testNestedStruct() {
        String source = "struct A {   " + 
                        "   struct B {" +
                        "       int x;" +
                        "   }         " +
                        "}";
        
        SymbolTable t = define(source);
        StructSymbol a = (StructSymbol) t.globals.resolve("A");
        assertEquals("struct A:{struct B:{}}", a.toString());
        assertNull(t.globals.resolve("B"));
        StructSymbol b = (StructSymbol) a.resolve("B");
        assertEquals("struct B:{}", b.toString());
    }

}