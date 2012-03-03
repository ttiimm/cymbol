package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;

public class TestDefinePhase {
    
    public ParseTreeProperty<Scope> scopes;
    
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
        BlockContext ctx = (BlockContext) m.tree.getChild(4);
        assertEquals("local[A]", scopes.get(ctx).toString());
    }
    
    @Test
    public void testStructWithMethodLocalBlock() {
        String source = "void M() {" +
        		        "    {" +
        		        "         struct A { int x; }" +
        		        "    }" +
        		        "}";
        SymbolTable t = runTest(source);
        MethodSymbol m = (MethodSymbol) t.globals.resolve("M");
        BlockContext first = (BlockContext) m.tree.getChild(4);
        BlockContext second = (BlockContext) first.getChild(1).getChild(0);
        assertEquals("local[A]", scopes.get(second).toString());
    }
    
    public SymbolTable runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler(in);
        scopes = c.define();
        
        return c.table;
    }
    
}