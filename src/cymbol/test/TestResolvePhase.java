package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Ignore;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.compiler.CymbolBaseListener;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.ExpressionContext;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class TestResolvePhase {

    @Test
    public void testResolveGlobalVars() {
        String source = "int a;" +
        		        "char b;" +
        		        "float c;" +
        		        "struct D { int x; }" +
        		        "D d;" +
        		        "boolean e;" +
        		        "int f[];";
        SymbolTable t = Util.runCompilerOn(source).table;
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
        SymbolTable t = Util.runCompilerOn(source).table;
        StructSymbol a = (StructSymbol) t.globals.resolve("A");
        assertEquals("<A.x:global.int>",a.resolve("x").toString());
        assertEquals("<A.y:global.float>", a.resolve("y").toString());
    }
    
    @Test
    public void testResolveMethodDecl() {
        String source = "void foo(int x, char y) {" +
                		"}";
        SymbolTable t = Util.runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        assertEquals("global.void", m.type.toString());
        assertEquals("<foo.x:global.int>", m.resolve("x").toString());
        assertEquals("<foo.y:global.char>", m.resolve("y").toString());
    }

    @Test
    public void testResolveMultipleMethodDecl() {
        String source = "void foo(int x, char y) {}" +
        		        "float bar(char z){}";
        SymbolTable t = Util.runCompilerOn(source).table;
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
        SymbolTable t = Util.runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        Scope local = Util.resolveLocalScope(m);
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
        SymbolTable t = Util.runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        Scope local = Util.resolveLocalScope(m);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }

    @Test
    public void testResolveVarWithForwardLocalStruct() {
        String source = "void foo() {" +
                        "   A a;" +
                        "   struct A { int x; }" +
                        "}";
        SymbolTable t = Util.runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        Scope local = Util.resolveLocalScope(m);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }
    
    @Test
    public void testResolveVarWithLocalStruct() {
        String source = "void foo() {" +
                "   struct A { int x; }" +
                "   A a;" +
                "}";
        SymbolTable t = Util.runCompilerOn(source).table;
        MethodSymbol m = (MethodSymbol) t.globals.resolve("foo");
        Scope local = Util.resolveLocalScope(m);
        assertEquals("<local.a:struct A:{x}>", local.resolve("a").toString());
    }
    
    @Test
    public void testResolveVarWithUnknownType() {
        String source = "A a;";
        Compiler c = Util.runCompilerOn(source);
        VariableSymbol a = (VariableSymbol) c.table.globals.resolve("a");
        assertEquals("global.a", a.toString());
        assertEquals("1:0: unknown symbol: A", c.errors.get(0));
        
    }
    
    @Test
    public void testResolveExprTypeWithPrimaryInt() {
        String source = "void foo() {" +
        		        "     4;" +
        		        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }

    @Test
    public void testResolveExprTypeWithPrimaryFloat() {
        String source = "void foo() {" +
                        "     4.0;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type floating = (Type) c.table.globals.resolve("float");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(floating);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }

    @Test
    public void testResolveExprTypeWithPrimaryChar() {
        String source = "void foo() {" +
                        "     'h';" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type character = (Type) c.table.globals.resolve("char");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(character);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }
    
    @Test
    public void testResolveExprTypeWithPrimaryBoolean() {
        String source = "void foo() {" +
                        "     true;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type bool = (Type) c.table.globals.resolve("boolean");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(bool);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }

    @Test
    public void testResolveExprTypeWithPrimaryId() {
        String source = "void foo() {" +
                        "int a;" +
                        "    a;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }

    @Test
    public void testResolveExprTypeWithRecursiveFunctionCall() {
        String source = "void foo() {" +
                        "    foo();" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type v = (Type) c.table.globals.resolve("void");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(v, v);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }
    
    @Test
    public void testResolveExprTypeWithFunctionCall() {
        String source = "int bar() { }" +
        		        "void foo() {" +
                        "    bar();" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer, integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }

    @Test
    public void testResolveExprTypeWithArrayRef() {
        String source = "void foo() {" +
                        "    char c[];" +
                        "    c[1];" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type integer = (Type) c.table.globals.resolve("int");
        Type character = (Type) c.table.globals.resolve("char");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(character, character, integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }
    
    @Ignore
    @Test
    public void testResolveStructRef() {
        String source = "struct A{ int x;}" +
                        "void foo() {" +
                        "    A a;" +
                        "    a.x;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type A = (Type) c.table.globals.resolve("A");
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer, A, integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
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
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type A = (Type) c.table.globals.resolve("A");
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer, A, integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }
    
    @Test
    public void testUnaryMinusExpr() {
        String source = "void foo() {" +
                        "    int a;" +
                        "    -a;" +
                        "}";
        Compiler c = Util.runCompilerOn(source);
        ParseTreeWalker walker = new ParseTreeWalker();
        Type integer = (Type) c.table.globals.resolve("int");
        ExprTypeVerifierListener verifier = new ExprTypeVerifierListener(integer, integer);
        walker.walk(verifier, c.tree);
        assertEquals(verifier.p, verifier.expected.length);
    }
    
    class ExprTypeVerifierListener extends CymbolBaseListener {

        private Type[] expected;
        public int p = 0;

        public ExprTypeVerifierListener(Type... expected) {
            this.expected = expected;
        }

        @Override
        public void enterExpression(ExpressionContext ctx) {
//            System.out.println(ctx.start + " " + ctx.stop);
//            System.out.println(ctx.props.type);
            assertEquals(expected[p++], ctx.props.type);
        }

        @Override
        public void enterExpr_Primary(Expr_PrimaryContext ctx) {
//            System.out.println(ctx.start + " " + ctx.stop);
//            System.out.println(ctx.props.type);
            assertEquals(expected[p++], ctx.props.type);
        }
        
    }
}
