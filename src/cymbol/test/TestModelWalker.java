package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;
import cymbol.symtab.Scope;

public class TestModelWalker {

    @Test
    public void testHelloWorld() {
        String expected = "// Cymbol generated C\n" + 
                          "// <Test>\n" + 
                          "\n" +
                          "#include <stdio.h>\n" + 
                          "#include <stdlib.h>\n" + 
                          "#include <string.h>\n" + 
                          "\n" + 
                          "#include \"gc.h\"\n" + 
                          "\n" +
                          "void _main();\n" + 
                          "void main();\n" + 
                          "\n" + 
                          "void _main() {\n" + 
                          "    gc_init(256 * 1000000);\n" +
                          "}\n" +
                          "\n" +
                          "void main() {\n" + 
                          "    GC_SAVE_RP;\n" +
                          "    _main();\n" +
                          "    String *s;\n" +
                          "    ADD_ROOT(s);\n" +
                          "    s = alloc_String(12);\n" +
                          "    strcpy(s->elements, \"hello world\\n\");\n" +
                          "    printf(\"%s\", s->elements);\n" +
                          "    GC_RESTORE_RP;\n" +
                          "}\n";
        
        String src = "void main() {" +
        		     "    printf(\"hello world\");" +
        		     "}";
                
        ST result = run(src);
        assertEquals(expected, result.render());
    }

    
    public ST run(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<Test>";
        Compiler c = new Compiler(in);
        ParseTreeProperty<Scope> scopes = c.define();
        c.resolve(scopes);
        SourceFile src = c.build(scopes);
        ModelTemplateWalker walker = new ModelTemplateWalker(c);
        return walker.walk(src);
    }
  
}
