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

public class TestGenerateSource {

    @Test
    public void testHelloWorld() {
        String expected = "/**\n" +
        		          " * Cymbol generated C\n" + 
                          " * <Test>\n" +
                          " */\n" + 
                          "\n" +
                          "#include <stdio.h>\n" + 
                          "#include <stdlib.h>\n" + 
                          "#include <string.h>\n" + 
                          "\n" + 
                          "#include \"gc.h\"\n" + 
                          "\n" +
                          "\n" +
                          "void _main();\n" + 
                          "void main();\n" + 
                          "\n" + 
                          "String *_String_literals[1];\n" + 
                          "\n" + 
                          "void _main() {\n" +
                          "    int i;\n" + 
                          "    gc_init(256 * 1000);\n" +
                          "    _String_literals[0] = new_String(\"hello world\\n\");\n" +
                          "    for(i = 0; i < 1; i++)\n" +
                          "        ADD_ROOT(_string_literals[i]);\n" +
                          "}\n" +
                          "\n" +
                          "void main() {\n" + 
                          "    GC_SAVE_RP;\n" +
                          "    _main();\n" +
                          "    printf(_String_literals[0]->elements);\n" +
                          "    GC_RESTORE_RP;\n" +
                          "}\n";
        
        String src = "void main() {" +
        		     "    printf(\"hello world\\n\");" +
        		     "}";
//        System.out.println(src);
//        System.out.println(expected);
        ST result = run(src);
        assertEquals(expected, result.render());
    }
    
    @Test
    public void testBinTree() {
        String expected = "/**\n" +
                          " * Cymbol generated C\n" + 
                          " * <Test>\n" +
                          " */\n" + 
                          "\n" +
                          "#include <stdio.h>\n" + 
                          "#include <stdlib.h>\n" + 
                          "#include <string.h>\n" + 
                          "\n" + 
                          "#include \"gc.h\"\n" + 
                          "\n" +
                          "typedef struct Tree {\n" + 
                          "    TypeDescriptor *type;\n" + 
                          "    byte *forward;\n" + 
                          "    Tree *left;\n" + 
                          "    Tree *right;\n" + 
                          "    Int *value;\n" + 
                          "} Tree;\n" + 
                          "\n" + 
                          "int Tree_field_offsets[3] = {\n" + 
                          "    offsetof(Tree, left),\n" + 
                          "    offsetof(Tree, right),\n" + 
                          "    offsetof(Tree, value)\n" + 
                          "}\n" + 
                          "\n" + 
                          "TypeDescriptor Tree_type = {\n" + 
                          "    \"Tree\",\n" + 
                          "    sizeof(Tree),\n" + 
                          "    3,\n" + 
                          "    Tree_field_offsets\n" + 
                          "}\n" + 
                          "\n" + 
                          "Tree *new_Tree() {\n" + 
                          "    return alloc(&Tree_type);\n" + 
                          "}\n" +
                          "\n" +
                          "void _main();\n" + 
                          "void main();\n" + 
                          "\n" + 
                          "String *_String_literals[0];\n" + 
                          "Int *_Int_literals[1];\n" + 
                          "\n" + 
                          "void _main() {\n" +
                          "    int i;\n" + 
                          "    gc_init(256 * 1000);\n" +
                          "\n" +
                          "    for(i = 0; i < 0; i++)\n" +
                          "        ADD_ROOT(_String_literals[i]);\n" +
                          "\n" +
                          "    _Int_literals[0] = new_Int(50);\n" +
                          "    for(i = 0; i < 1; i++)\n" +
                          "        ADD_ROOT(_Int_literals[i]);\n" +
                          "}\n" +
                          "\n" +
                          "void main() {\n" + 
                          "    GC_SAVE_RP;\n" +
                          "    Tree fifty = new_Tree();\n" +
                          "    fifty->value = _Int_literals[1]->value;\n" +
                          "    GC_RESTORE_RP;\n" +
                          "}\n";
        
        String src = "\n" +
        		     "struct Tree {\n" +
        		     "    Tree left;\n" +
        		     "    Tree right;\n" +
        		     "    int value;\n" +
        		     "}\n" +
        		     "\n" +
        		     "void main() {\n" +
                     "    Tree fifty = new Tree();\n" +
                     "    fifty.value = 50;\n" +
                     "}";
//        System.out.println(src);
//        System.out.println(expected);
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
