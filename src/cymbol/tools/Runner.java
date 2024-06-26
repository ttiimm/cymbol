package cymbol.tools;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
 ***/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;

public class Runner {

    private static CharStream determineInput(String[] args) throws IOException {
        InputStream is = null;
        if (args.length > 0) {
            is = new FileInputStream(args[0]);
        } else {
            is = System.in;
        }

        return CharStreams.fromStream(is);
    }

    /**
     * Checks for errors during compilations.
     * 
     * If an error is occurred during a stage of compilation, then the error is printed out and compilation halts.
     */
    private static void checkErrors(Compiler c) {
        boolean hasErrors = c.errors.size() > 0;
        if (hasErrors) {
            for (String e : c.errors) { System.err.println(e); }
            System.exit(-1);
        }
    }
    
    private static String mkPath() {
        String fileSeparator = System.getProperty("file.separator");
        String path = System.getProperty("java.io.tmpdir") + fileSeparator + "cymbol" + fileSeparator;
        new File(path).mkdirs();
        return path;
    }

    private static void writeOut(String path, String compiled) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(path + "cymbolgen.c"));
            out.write(compiled);
        } finally {
            if(out != null) out.close();
        }

    }

    public static void main(String[] args) throws IOException {
        CharStream in = determineInput(args);
        Compiler c = new Compiler(in);

        checkErrors(c);
        SourceFile src = c.compile();
        checkErrors(c);
        ModelTemplateWalker walker = new ModelTemplateWalker(c);
        ST st = walker.walk(src);
        checkErrors(c);

        System.out.println(st.render());
    }
}
