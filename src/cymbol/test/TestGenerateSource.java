package cymbol.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;
import cymbol.symtab.Scope;
import cymbol.symtab.Type;

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
                          "#include <stddef.h>\n" + 
                          "#include <string.h>\n" + 
                          "\n" + 
                          "#include \"gc.h\"\n" + 
                          "\n" +
                          "\n" +
                          "void _main();\n" + 
                          "void main();\n" + 
                          "\n" + 
                          "String *_String_literals[1];\n" + 
                          "Int *_Int_literals[0];\n" + 
                          "\n" + 
                          "void _main() {\n" +
                          "    int i;\n" + 
                          "    gc_init(256 * 1000);\n" +
                          "\n" +
                          "    _String_literals[0] = new_String(\"hello world\\n\");\n" +
                          "    for(i = 0; i < 1; i++)\n" +
                          "        ADD_ROOT(_String_literals[i]);\n" +
                          "\n" +
                          "    for(i = 0; i < 0; i++)\n" +
                          "        ADD_ROOT(_Int_literals[i]);\n" +
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
    public void testBinTree() throws IOException {
        String src = readFile("src/cymbol/test/functional/run/tree.cymbol");
        String expected = readFile("src/cymbol/test/functional/run/tree.expected");
//        System.out.println(src);
//        System.out.println(expected);
        ST result = run(src);
        assertEquals(expected, result.render());
    }
    
    /**
     * http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
     */
    private String readFile(String pathname) throws IOException {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {        
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }


    
    public ST run(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<Test>";
        Compiler c = new Compiler(in);
        ParseTreeProperty<Scope> scopes = c.define();
        ParseTreeProperty<Type> types = c.resolve(scopes);
        SourceFile src = c.build(scopes, types);
        ModelTemplateWalker walker = new ModelTemplateWalker(c);
        return walker.walk(src);
    }
  
}
