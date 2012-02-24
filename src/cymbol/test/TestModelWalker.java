package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
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
        		          "int x;" +
        		          "\n" +
        		          "\n";
        
        SourceFile src = new SourceFile("<Test>");
        src.add(new VariableDeclaration(SymbolTable.INT, "x"));
        ST result = runTest(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testStruct() {
        String expected = "// Cymbol generated C\n" + 
                          "// <Test>\n" +
                          "\n" +
                          "struct A {\n" +
                          "\n" +
                          "\n" +
                          "    int x;\n" +
                          "}\n" +
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
                "\n" +
                "    struct B {\n" +
                "\n" +
                "\n" +
                "        int x;\n" +
                "    }\n" +
                "\n" +
                "}\n" +
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
    
    public ST runTest(SourceFile src) {
        Compiler compiler = new Compiler();
        ModelTemplateWalker walker = new ModelTemplateWalker(compiler);
        return walker.walk(src);
    }
}
