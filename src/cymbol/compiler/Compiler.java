package cymbol.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import cymbol.model.SourceFile;
import cymbol.symtab.Scope;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;

public class Compiler {

    public CymbolParser parser;
    public ParseTree tree;
    public SymbolTable table;
    private String sourceName;

    public List<String> errors = new ArrayList<String>();

    public Compiler() {
        this(null);
    }
    
    public Compiler(CharStream in) {
        this.parser = setupParser(in);
        this.tree = in != null ? parse(parser, in) : null;
        this.table = new SymbolTable();
        this.sourceName = in != null ? in.getSourceName() : "<UNDEFINED>";
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

    public SourceFile compile() {
        ParseTreeProperty<Scope> scopes = define();
        ParseTreeProperty<Type> types = resolve(scopes);
        SourceFile src = build(scopes, types);
        
        if(errors.size() > 0) { return null; }
        else { return src; }
    }

    public ParseTreeProperty<Scope> define() {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerDefinePhase defl = new ListenerDefinePhase(table.globals);
        walker.walk(defl, tree);
        return defl.scopes;
    }

    public ParseTreeProperty<Type> resolve(ParseTreeProperty<Scope> scopes) {
        ParseTreeWalker walker = new ParseTreeWalker();
        ListenerResolvePhase refl = new ListenerResolvePhase(new ScopeUtil(this, scopes), this);
        walker.walk(refl, tree);
        return refl.types;
    }
    
    public SourceFile build(ParseTreeProperty<Scope> scopes, ParseTreeProperty<Type> types) {
        ParseTreeWalker walker = new ParseTreeWalker();
        ParseTreeProperty<OutputModelObject> models = new ParseTreeProperty<OutputModelObject>();
        ScopeUtil scopeUtil = new ScopeUtil(this, scopes);
        ListenerBuildPhase builder = new ListenerBuildPhase(scopeUtil, types, models, sourceName);
        walker.walk(builder, tree);
        return (SourceFile) models.get(tree);
    }

    public void reportError(ParserRuleContext ctx, String msg) {
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
