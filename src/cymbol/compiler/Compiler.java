package cymbol.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.symtab.SymbolTable;

public class Compiler {

    private ParseTree tree;
    public SymbolTable table;

    public Compiler(CharStream in) {
        this.tree = constructParseTree(in);
        this.table = new SymbolTable();
    }
    
    public ParseTree constructParseTree(CharStream in) {
        CymbolLexer l = new CymbolLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(l);
        CymbolParser p = new CymbolParser(tokens);
        p.setBuildParseTree(true);
        
        return p.compilationUnit();
    }
    
    public void compile() {
        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTable table = new SymbolTable();
        ListenerDefPhase def = new ListenerDefPhase(table.globals);
        walker.walk(def, tree);
        ListenerRefPhase ref = new ListenerRefPhase(table.globals);
        walker.walk(ref, tree);
    }
    
    public void define() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerDefPhase defl = new ListenerDefPhase(table.globals);
        walker.walk(defl, tree);
    }

    public void reference() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerRefPhase refl = new ListenerRefPhase(table.globals);
        walker.walk(refl, tree);
    }
}
