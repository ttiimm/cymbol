package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.SymbolTable;

public class TestModelWalker {

    private ModelTemplateWalker walker;

    @Before
    public void setUp() {
        Compiler compiler = new Compiler();
        walker = new ModelTemplateWalker(compiler);
    }
    
    @Test
    public void testCompilationUnitSourceWithVarDecl() {
        String expected = "// Cymbol generated C\n" + 
        		          "// <Test>\n" +
        		          "\n" +
        		          "\n" +
        		          "int x;" +
        		          "\n" +
        		          "\n";
        
        SourceFile src = new SourceFile("<Test>");
        src.add(new VariableDeclaration(SymbolTable.INT, "x"));
        ST result = walker.walk(src);
        assertEquals(expected, result.render());
    }
    
}
