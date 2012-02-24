package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.model.SourceFile;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.VariableDeclaration;
import cymbol.compiler.Compiler;

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
        		          "// <Test>";
        
        SourceFile src = new SourceFile("<Test>");
        src.add(new VariableDeclaration());
        ST result = walker.walk(src);
        assertEquals(expected, result.render());
    }
    
}
