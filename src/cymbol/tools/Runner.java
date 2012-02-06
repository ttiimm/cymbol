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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.compiler.ListenerDefPhase;
import cymbol.compiler.Compiler;
import cymbol.compiler.ListenerRefPhase;
import cymbol.symtab.SymbolTable;

public class Runner {
    private static CharStream determineInput(String[] args) throws IOException {
        if (args.length > 0) {
            return new ANTLRFileStream(args[0]);
        } else {
            return new ANTLRInputStream(System.in);
        }
    }
    
    public static void main(String[] args) throws IOException {
        CharStream in = determineInput(args);
        Compiler c = new Compiler();
        ParseTree t = c.constructParseTree(in);

        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTable table = new SymbolTable();
        ListenerDefPhase def = new ListenerDefPhase(table.globals);
        walker.walk(def, t);
        ListenerRefPhase ref = new ListenerRefPhase(table.globals);
        walker.walk(ref, t);
        System.out.println(table.globals);
    }
}
