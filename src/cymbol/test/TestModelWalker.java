package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.Block;
import cymbol.model.Expression.Primary;
import cymbol.model.MethodFunction;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.VariableSymbol;

public class TestModelWalker {

    @Test
    public void testVarDecl() {
        String expected = "// Cymbol generated C\n" + 
        		          "// <Test>\n" +
        		          "\n" +
        		          "\n" +
        		          "\n" +
        		          "int x;" +
        		          "\n" +
        		          "\n";
        
        SourceFile src = new SourceFile("<Test>");
        src.add(new VariableDeclaration(SymbolTable.INT, "x"));
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testVarWithAssignment() {
        String expected = "// Cymbol generated C\n" + 
                "// <Test>\n" +
                "\n" +
                "\n" +
                "\n" +
                "int x = 1;" +
                "\n" +
                "\n";
        
        SourceFile src = new SourceFile("<Test>");
        VariableDeclaration var = new VariableDeclaration(SymbolTable.INT, "x");
        var.add(new Primary("1"));
        src.add(var);
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testStruct() {
        String expected = "// Cymbol generated C\n" + 
                          "// <Test>\n" +
                          "\n" +
                          "struct A {\n" +
                          "    int x;\n" +
                          "}\n" +
                          "\n" +
                          "\n" +
                          "\n";
        
        SourceFile src = new SourceFile("<Test>");
        StructSymbol symbol = new StructSymbol("A", null, null);
        symbol.define(new VariableSymbol("x", SymbolTable.INT));
        src.add(new Struct(symbol));
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testNestedStruct() {
        String expected = "// Cymbol generated C\n" + 
                "// <Test>\n" +
                "\n" +
                "struct A {\n" +
                "    struct B {\n" +
                "        int x;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n";
        
        SourceFile src = new SourceFile("<Test>");
        StructSymbol a = new StructSymbol("A", null, null);
        StructSymbol b = new StructSymbol("B", null, null);
        a.define(b);
        b.define(new VariableSymbol("x", SymbolTable.INT));
        src.add(new Struct(a));
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testMethodFuncs() {
        String expected = "// Cymbol generated C\n" + 
                          "// <Test>\n" +
                          "\n" +
                          "\n" +
                          "void foo(float y);\n" +
                          "\n" +
                          "\n" +
                          "\n" +
                          "void foo(float y) {\n" +
                          "}\n";
        
        MethodSymbol m = new MethodSymbol("foo", SymbolTable.VOID, null, null);
        m.define(new VariableSymbol("y", SymbolTable.FLOAT));
        MethodFunction func = new MethodFunction(m, new Block());
        SourceFile src = new SourceFile("<Test>");
        src.add(func);
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testMultipleMethodFuncs() {
        String expected = "// Cymbol generated C\n" + 
                    		"// <Test>\n" + 
                    		"\n" + 
                    		"\n" + 
                    		"void foo(float y);\n" + 
                    		"char bar(int x);\n" + 
                    		"\n" + 
                    		"\n" + 
                    		"\n" + 
                    		"void foo(float y) {\n" + 
                    		"}\n" + 
                    		"\n" + 
                    		"char bar(int x) {\n" + 
                    		"}\n";
        
        MethodSymbol mfoo = new MethodSymbol("foo", SymbolTable.VOID, null, null);
        mfoo.define(new VariableSymbol("y", SymbolTable.FLOAT));
        MethodFunction foo = new MethodFunction(mfoo, new Block());
        MethodSymbol mbar = new MethodSymbol("bar", SymbolTable.CHAR, null, null);
        mbar.define(new VariableSymbol("x", SymbolTable.INT));
        MethodFunction bar = new MethodFunction(mbar, new Block());
        SourceFile src = new SourceFile("<Test>");
        src.add(foo);
        src.add(bar);
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testFuncWithVarDecl() {
        String expected = "// Cymbol generated C\n" + 
                "// <Test>\n" + 
                "\n" + 
                "\n" + 
                "void foo();\n" + 
                "\n" + 
                "\n" + 
                "\n" + 
                "void foo() {\n" + 
                "    int x;\n" + 
                "}\n";
        
        Block block = new Block();
        block.add(new VariableDeclaration(SymbolTable.INT, "x"));
        MethodSymbol mfoo = new MethodSymbol("foo", SymbolTable.VOID, null, null);
        MethodFunction foo = new MethodFunction(mfoo, block);
        SourceFile src = new SourceFile("<Test>");
        src.add(foo);
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }

    @Test
    public void testFuncWithStructDecl() {
        String expected = "// Cymbol generated C\n" + 
                "// <Test>\n" + 
                "\n" + 
                "\n" + 
                "void foo();\n" + 
                "\n" + 
                "\n" + 
                "\n" + 
                "void foo() {\n" + 
                "    struct A {\n" +
                "        int x;\n" +
                "    }\n" + 
                "}\n";
        
        Block block = new Block();
        StructSymbol struct = new StructSymbol("A", null, null);
        struct.define(new VariableSymbol("x", SymbolTable.INT));
        block.add(new Struct(struct));
        MethodSymbol mfoo = new MethodSymbol("foo", SymbolTable.VOID, null, null);
        MethodFunction foo = new MethodFunction(mfoo, block);
        SourceFile src = new SourceFile("<Test>");
        src.add(foo);
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    public ST runTest(SourceFile src) {
        Compiler compiler = new Compiler();
        ModelTemplateWalker walker = new ModelTemplateWalker(compiler);
        return walker.walk(src);
    }
}
