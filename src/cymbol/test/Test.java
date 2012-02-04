package cymbol.test;

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
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.compiler.CymbolLexer;
import cymbol.compiler.CymbolParser;
import cymbol.compiler.DefineListener;

public class Test {
    private static CharStream determineInput(String[] args) throws IOException {
        if (args.length > 0) {
            return new ANTLRFileStream(args[0]);
        } else {
            return new ANTLRInputStream(System.in);
        }
    }

    public static void main(String[] args) throws IOException {
        CharStream in = determineInput(args);
        CymbolLexer lexer = new CymbolLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokens);
        parser.setBuildParseTree(true);
        ParserRuleContext<Token> t = parser.compilationUnit();
        // System.out.println("tree = "+t.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        DefineListener dcl = new DefineListener();
        walker.walk(dcl, t);
        // System.out.println(t.getChild(0));
        // System.out.println("result from tree walk = "+ ectx.v);
//        DefRef def = new DefRef(nodes, symtab); // use custom constructor
//        def.downup(t); // trigger symtab actions upon certain subtrees 
//        System.out.println("globals: "+symtab.globals);
    }
}
