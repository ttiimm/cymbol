package cymbol.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.symtab.SymbolTable;

public class Compiler {

    public CymbolParser parser;
    public ParseTree tree;
    public SymbolTable table;
    private ParseTreeWalker walker;

    public List<String> errors = new ArrayList<String>();

    public Compiler(CharStream in) {
        this.parser = setupParser(in);
        this.tree = parse(in);
        this.table = new SymbolTable();
        this.walker = new ParseTreeWalker();
    }

    private CymbolParser setupParser(CharStream in) {
        CymbolLexer l = new CymbolLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(l);
        CymbolParser p = new CymbolParser(tokens);
        p.setBuildParseTree(true);
        return p;
    }

    private ParseTree parse(CharStream in) {
        ParseTree tree = null;
        
        try {
            tree = this.parser.compilationUnit();
            if (this.parser.getNumberOfSyntaxErrors() > 0) {
                error("Error parsing " + in.getSourceName());
                return null;
            }
        } catch (RecognitionException re) {
            error("Error parsing " + in.getSourceName(), re);
        }
        
        return tree;
    }

    public void compile() {
        define();
        reference();
    }

    public void define() {
        ListenerDefPhase defl = new ListenerDefPhase(table.globals);
        walker.walk(defl, tree);
    }

    public void reference() {
        ListenerRefPhase refl = new ListenerRefPhase(this);
        walker.walk(refl, tree);
    }

    public void reportError(ParserRuleContext<Token> ctx, String msg) {
        Token start = ctx.getStart();
        int line = start.getLine();
        int pos = start.getCharPositionInLine();
        error(line + ":" + pos + ": " + msg);
    }
    
    public void error(String message) {
        errors.add(message);
    }

    public void error(String msg, Exception e) {
        errors.add(msg + "\n" + Arrays.toString(e.getStackTrace()));
    }
}
