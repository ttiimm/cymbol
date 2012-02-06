package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.compiler.CymbolParser;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.VariableSymbol;

public class TestRefPhase {

    @Test
    public void testDefineGlobalVars() {
        String source = "int a;" +
        		        "char b;" +
        		        "float c;" +
        		        "struct D { int x; }" +
        		        "D d;" +
        		        "boolean e;" +
        		        "int f[];";
        SymbolTable t = runTest(source).table;
        assertEquals("<global.a:global.int>", t.globals.resolve("a").toString());
        assertEquals("<global.b:global.char>", t.globals.resolve("b").toString());
        assertEquals("<global.c:global.float>", t.globals.resolve("c").toString());
        assertEquals("<global.d:struct D:{x}>", t.globals.resolve("d").toString());
        assertEquals("<global.e:global.boolean>", t.globals.resolve("e").toString());
        // TODO create Array type
        assertEquals("<global.f:global.int>", t.globals.resolve("f").toString());
    }
    
    @Test
    public void testDefineStructVars() {
        String source = "struct A {" +
        		        "    int x;" +
        		        "    float y;" +
        		        "}";
        SymbolTable t = runTest(source).table;
        StructSymbol a = (StructSymbol) t.globals.resolve("A");
        assertEquals("<A.x:global.int>",a.resolve("x").toString());
        assertEquals("<A.y:global.float>", a.resolve("y").toString());
    }
    
    @Test
    public void testDefineMethodDecl() {
        String source = "void foo(int x, char y) {" +
                		"}";
        SymbolTable t = runTest(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        assertEquals("global.void", m.type.toString());
        assertEquals("<foo.x:global.int>", m.resolve("x").toString());
        assertEquals("<foo.y:global.char>", m.resolve("y").toString());
    }
    
    @Test
    public void testDefineVarDecl() {
        String source = "void foo() {" +
                        "   int a[];" +
                        "   float x = a[0];" +
                        "}";
        SymbolTable t = runTest(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        CymbolParser.blockContext local = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("<local.a:global.int>", local.scope.resolve("a").toString());
        assertEquals("<local.x:global.float>", local.scope.resolve("x").toString());
        
    }
    
    @Test
    public void testDefineVarWithForwardGlobalStruct() {
        String source = "void foo() {" +
                        "   A a;" +
                        "}" +
                        "" +
                        "struct A { int x; }";
        SymbolTable t = runTest(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        CymbolParser.blockContext local = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("<local.a:struct A:{x}>", local.scope.resolve("a").toString());
    }
    
    @Test
    public void testDefineVarWithForwardLocalStruct() {
        String source = "void foo() {" +
                "   A a;" +
                "   struct A { int x; }" +
                "}";
        SymbolTable t = runTest(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        CymbolParser.blockContext local = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("<local.a:struct A:{x}>", local.scope.resolve("a").toString());
    }
    
    @Test
    public void testDefineVarWithLocalStruct() {
        String source = "void foo() {" +
                "   struct A { int x; }" +
                "   A a;" +
                "}";
        SymbolTable t = runTest(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        CymbolParser.blockContext local = (CymbolParser.blockContext) m.tree.getChild(4);
        assertEquals("<local.a:struct A:{x}>", local.scope.resolve("a").toString());
    }
    
    @Test
    public void testDefineVarWithUnknownType() {
        String source = "A a;";
        Compiler c = runTest(source);
        VariableSymbol a = (VariableSymbol) c.table.globals.resolve("a");
        assertEquals("global.a", a.toString());
        assertEquals("1:0: unknown symbol: A", c.errors.get(0));
        
    }
    
    public Compiler runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler(in);
        c.compile();

        return c;
    }
}
