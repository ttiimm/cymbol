package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Ignore;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class TestResolvePhase {

    private ParseTreeProperty<Scope> scopes;
    private ParseTreeProperty<Type> types;

    @Test
    public void testResolveGlobalVars() {
        String source = "int a;" +
        		        "char b;" +
        		        "float c;" +
        		        "struct D { int x; }" +
        		        "D d;" +
        		        "boolean e;" +
        		        "int f[];";
        SymbolTable t = runCompilerOn(source).table;
        assertEquals("<global.a:global.int>", t.globals.resolve("a").toString());
        assertEquals("<global.b:global.char>", t.globals.resolve("b").toString());
        assertEquals("<global.c:global.float>", t.globals.resolve("c").toString());
        assertEquals("<global.d:struct D:{x}>", t.globals.resolve("d").toString());
        assertEquals("<global.e:global.boolean>", t.globals.resolve("e").toString());
        // TODO create Array type
        assertEquals("<global.f:global.int>", t.globals.resolve("f").toString());
    }
    
    @Test
    public void testResolveStructVars() {
        String source = "struct A {" +
        		        "    int x;" +
        		        "    float y;" +
        		        "}";
        SymbolTable t = runCompilerOn(source).table;
        StructSymbol a = (StructSymbol) t.globals.resolve("A");
        assertEquals("<A.x:global.int>",a.resolve("x").toString());
        assertEquals("<A.y:global.float>", a.resolve("y").toString());
    }
    
    @Test
    public void testResolveMethodDecl() {
        String source = "void foo(int x, char y) {" +
                		"}";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        assertEquals("global.void", m.type.toString());
        assertEquals("<foo.x:global.int>", m.resolve("x").toString());
        assertEquals("<foo.y:global.char>", m.resolve("y").toString());
    }

    @Test
    public void testResolveMultipleMethodDecl() {
        String source = "void foo(int x, char y) {}" +
        		        "float bar(char z){}";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        assertEquals("global.void", m.type.toString());
        assertEquals("<foo.x:global.int>", m.resolve("x").toString());
        assertEquals("<foo.y:global.char>", m.resolve("y").toString());
        MethodSymbol bar = (MethodSymbol) t.globals.resolve("bar");
        assertEquals("global.float", bar.type.toString());
    }
    
    @Test
    public void testResolveVarDecl() {
        String source = "void foo() {" +
                        "   int a[];" +
                        "   float x = a[0];" +
                        "}";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        ParseTree blockCtx = getBlock(m);
        Scope local = scopes.get(blockCtx);
        assertEquals("<local.a:global.int>", local.resolve("a").toString());
        assertEquals("<local.x:global.float>", local.resolve("x").toString());
        
    }
    
    @Test
    public void testResolveVarWithForwardGlobalStruct() {
        String source = "void foo() {" +
                        "   A a;" +
                        "}" +
                        "" +
                        "struct A { int x; }";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        ParseTree blockCtx = getBlock(m);
        Scope local = scopes.get(blockCtx);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }

    @Test
    public void testResolveVarWithForwardLocalStruct() {
        String source = "void foo() {" +
                        "   A a;" +
                        "   struct A { int x; }" +
                        "}";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        ParseTree blockCtx = getBlock(m);
        Scope local = scopes.get(blockCtx);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }
    
    @Test
    public void testResolveVarWithLocalStruct() {
        String source = "void foo() {" +
                        "   struct A { int x; }" +
                        "   A a;" +
                        "}";
        SymbolTable t = runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        ParseTree blockCtx = getBlock(m);
        Scope local = scopes.get(blockCtx);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }
    
    @Test
    public void testResolveVarWithUnknownType() {
        String source = "A a;";
        Compiler c = runCompilerOn(source);
        VariableSymbol a = (VariableSymbol) c.table.globals.resolve("a");
        assertEquals("global.a", a.toString());
        assertEquals("1:0: unknown type: A", c.errors.get(0));
    }
    
    @Test
    public void testResolveExprTypeWithPrimaryInt() {
        String source = "void foo() {" +
        		        "     4;" +
        		        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.INT, types.get(block.getChild(1).getChild(0)));
    }

    @Test
    public void testResolveExprTypeWithPrimaryFloat() {
        String source = "void foo() {" +
                        "     4.0;" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.FLOAT, types.get(block.getChild(1).getChild(0)));
    }

    @Test
    public void testResolveExprTypeWithPrimaryChar() {
        String source = "void foo() {" +
                        "     'h';" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.CHAR, types.get(block.getChild(1).getChild(0)));
    }
    
    @Test
    public void testResolveExprTypeWithPrimaryBoolean() {
        String source = "void foo() {" +
                        "     true;" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.BOOLEAN, types.get(block.getChild(1).getChild(0)));
    }

    @Test
    public void testResolveExprTypeWithPrimaryId() {
        String source = "void foo() {" +
                        "int a;" +
                        "    a;" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.INT, types.get(block.getChild(2).getChild(0)));
    }

    @Test
    public void testResolveExprTypeWithRecursiveFunctionCall() {
        String source = "void foo() {" +
                        "    foo();" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.VOID, types.get(block.getChild(1).getChild(0)));
    }
    
    @Test
    public void testResolveExprTypeWithFunctionCall() {
        String source = "int bar() { }" +
        		        "void foo() {" +
                        "    bar();" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.INT, types.get(block.getChild(1).getChild(0)));
    }

    @Test
    public void testResolveExprTypeWithArrayRef() {
        String source = "void foo() {" +
                        "    char c[];" +
                        "    c[1];" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.CHAR, types.get(block.getChild(2).getChild(0)));
     }
    
    @Ignore
    @Test
    public void testResolveStructRef() {
        String source = "struct A{ int x;}" +
                        "void foo() {" +
                        "    A a;" +
                        "    a.x;" +
                        "}";
        Compiler c = runCompilerOn(source);
        Type A = (Type) c.table.globals.resolve("A");
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(A, types.get(block.getChild(2).getChild(0)));
        assertEquals(SymbolTable.INT, types.get(block.getChild(2).getChild(2)));
    }
    
    @Ignore
    @Test
    public void testResolveNestedStructRef() {
        String source = "struct A{" +
                		"    struct B{" +
                		"        int x;" +
                		"    }" +
                		"    B b;" +
                		"}" +
                        "void foo() {" +
                        "    A a;" +
                        "    a.b.x;" +
                        "}";
        Compiler c = runCompilerOn(source);
//        Type A = (Type) c.table.globals.resolve("A");
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.INT, types.get(block.getChild(2).getChild(4)));
    }
    
    @Test
    public void testUnaryMinusExpr() {
        String source = "void foo() {" +
                        "    int a;" +
                        "    -a;" +
                        "}";
        Compiler c = runCompilerOn(source);
        MethodSymbol m = (MethodSymbol) c.table.globals.resolve("foo");
        ParserRuleContext<Token> block = getBlock(m);
        assertEquals(SymbolTable.INT, types.get(block.getChild(2).getChild(0)));
    }
    
    public Compiler runCompilerOn(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<String>";
        Compiler c = new Compiler(in);
        scopes = c.define();
        types = c.resolve(scopes);

        return c;
    }
    
    private ParserRuleContext<Token> getBlock(MethodSymbol m) {
        ParseTree blockCtx = m.tree.getChild(4);
        return (ParserRuleContext<Token>) blockCtx;
    }
}
