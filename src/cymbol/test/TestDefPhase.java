package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.compiler.CymbolParser;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;

public class TestDefPhase {
    
    @Test
    public void testSimpleStruct() {
        String source = "struct A { int x; }";
        SymbolTable t = runTest(source);
        Symbol s = t.globals.resolve("A");
        assertEquals("struct A:{x}", s.toString());
    }
    
    @Test
    public void testNestedStructWithinStruct() {
        String source = "struct A {   " + 
                        "   struct B {" +
                        "       int x;" +
                        "   }         " +
                        "}";
        
        SymbolTable t = runTest(source);
        StructSymbol a = (StructSymbol) t.globals.resolve("A");
        assertEquals("struct A:{B}", a.toString());
        assertNull(t.globals.resolve("B"));
        Symbol b = a.resolve("B");
        assertEquals("struct B:{x}", b.toString());
    }

    @Test
    public void testEmptyMethod() {
        String source = "void M(){ }";
        SymbolTable t = runTest(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        assertEquals("global.M()", m.toString());
    }
    
    @Test
    public void testMethodWithNestedStruct() {
        String source = "void M(){" +
        		        "    struct A { int x; } " +
        		        "}";
        SymbolTable t = runTest(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        assertEquals("global.M()", m.toString());
        CymbolParser.blockContext ctx = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("local[A]", ctx.scope.toString());
    }
    
    @Test
    public void testStructWithMethodLocalBlock() {
        String source = "void M(){" +
        		        "    {" +
        		        "         struct A { int x; }" +
        		        "    }" +
        		        "}";
        SymbolTable t = runTest(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        CymbolParser.blockContext first = (CymbolParser.blockContext) m.tree.getChild(4);
        CymbolParser.blockContext second = (CymbolParser.blockContext) first.getChild(1).getChild(0);
        assertEquals("local[A]", second.scope.toString());
    }
    
    public static SymbolTable runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler();
        ParseTree t = c.constructParseTree(in);
        SymbolTable table = c.define(t);
        
        return table;
    }
    
}