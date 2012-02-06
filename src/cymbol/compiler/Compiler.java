package cymbol.compiler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.symtab.SymbolTable;

public class Compiler {

    private ParseTree tree;
    public SymbolTable table;
    private ParseTreeWalker walker;

    public List<String> errors = new ArrayList<String>();

    public Compiler(CharStream in) {
        this.tree = constructParseTree(in);
        this.table = new SymbolTable();
        this.walker = new ParseTreeWalker();
    }

    public ParseTree constructParseTree(CharStream in) {
        CymbolLexer l = new CymbolLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(l);
        CymbolParser p = new CymbolParser(tokens);
        p.setBuildParseTree(true);

        return p.compilationUnit();
    }

    public void compile() {
        define();
        reference();
    }

    public void define() {
        ListenerDefPhase defl = new ListenerDefPhase(this, table.globals);
        walker.walk(defl, tree);
    }

    public void reference() {
        ListenerRefPhase refl = new ListenerRefPhase(this, table.globals);
        walker.walk(refl, tree);
    }
    
    public void error(String message) {
        errors.add(message);
    }
}
