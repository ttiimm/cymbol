package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import cymbol.compiler.CymbolLexer;
import cymbol.compiler.CymbolParser;
import cymbol.compiler.ListenerDefPhase;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;

public class TestDefPhase {
    
    @Test
    public void testSimpleStruct() {
        String source = "struct A { int x; }";
        SymbolTable t = define(source);
        Symbol s = t.globals.resolve("A");
        assertEquals("struct A:{x}", s.toString());
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
        assertEquals("struct A:{B}", a.toString());
        assertNull(t.globals.resolve("B"));
        Symbol b = a.resolve("B");
        assertEquals("struct B:{x}", b.toString());
    }

    @Test
    public void testEmptyMethod() {
        String source = "void M(){ }";
        SymbolTable t = define(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        assertEquals("global.M()", m.toString());
    }
    
    @Test
    public void testMethodWithNestedStruct() {
        String source = "void M(){" +
        		        "    struct A { int x; } " +
        		        "}";
        SymbolTable t = define(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        assertEquals("global.M()", m.toString());
        CymbolParser.blockContext ctx = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("local[A]", ctx.scope.toString());
    }
    
    @Test
    public void testDoublyNestedStruct() {
        String source = "void M(){" +
        		        "    {" +
        		        "         struct A { int x; }" +
        		        "    }" +
        		        "}";
        SymbolTable t = define(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        CymbolParser.blockContext first = (CymbolParser.blockContext) m.tree.getChild(4);
        CymbolParser.blockContext second = (CymbolParser.blockContext) first.getChild(1).getChild(0);
        assertEquals("local[A]", second.scope.toString());
    }
    
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
        ListenerDefPhase defl = new ListenerDefPhase(table.globals);
        walker.walk(defl, t);
        // System.out.println(((Tree)r.getTree()).toStringTree());

        return table;
    }
}