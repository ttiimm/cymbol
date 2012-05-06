package cymbol.tools;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
 ***/

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.stringtemplate.v4.ST;

import cymbol.compiler.Compiler;
import cymbol.model.ModelTemplateWalker;
import cymbol.model.SourceFile;

public class Runner {

    private static CharStream determineInput(String[] args) throws IOException {
        if (args.length > 0) {
            return new ANTLRFileStream(args[0]);
        } else {
            ANTLRInputStream in = new ANTLRInputStream(System.in);
            in.name = "<System.in>";
            return in;
        }
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

    private static void writeOut(String compiled) {
        String fileSeparator = System.getProperty("file.separator");
        String path = System.getProperty("java.io.tmpdir") + "cymbol" + fileSeparator + "cymbolgen.c";
        System.out.println(path);
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

        writeOut(st.render());
    }
}
