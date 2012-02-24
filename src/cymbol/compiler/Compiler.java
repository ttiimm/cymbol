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

import cymbol.model.CompilationUnitSource;
import cymbol.symtab.SymbolTable;

public class Compiler {

    public CymbolParser parser;
    public ParseTree tree;
    public SymbolTable table;
    
    public CompilationUnitSource compSource;

    public List<String> errors = new ArrayList<String>();

    public Compiler(CharStream in) {
        this.parser = setupParser(in);
        this.tree = parse(parser, in);
        this.table = new SymbolTable();
    }

    private CymbolParser setupParser(CharStream in) {
        CymbolLexer l = new CymbolLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(l);
        CymbolParser p = new CymbolParser(tokens);
        p.setBuildParseTree(true);
        return p;
    }

    private ParseTree parse(CymbolParser parser, CharStream in) {
        ParseTree tree = null;
        
        try {
            tree = parser.compilationUnit();
            if (parser.getNumberOfSyntaxErrors() > 0) {
                error("Error parsing " + in.getSourceName());
                return null;
            }
        } catch (RecognitionException re) {
            error("Error parsing " + in.getSourceName(), re);
        }
        
        return tree;
    }

    public CompilationUnitSource compile() {
        compSource = new CompilationUnitSource();
        define();
        reference();
        build();
        
        if(errors.size() > 0) { return null; }
        else { return compSource; }
    }

    public void define() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerDefinePhase defl = new ListenerDefinePhase(table.globals);
        walker.walk(defl, tree);
    }

    public void reference() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerResolvePhase refl = new ListenerResolvePhase(this);
        walker.walk(refl, tree);
    }
    
    private void build() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerBuildPhase builder = new ListenerBuildPhase(compSource);
        walker.walk(builder, tree);
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
