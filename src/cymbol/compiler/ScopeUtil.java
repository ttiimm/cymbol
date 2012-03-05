package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;

public class ScopeUtil {
    
    private Compiler compiler;
    private ParseTreeProperty<Scope> scopes;

    public ScopeUtil(Compiler compiler, ParseTreeProperty<Scope> scopes) {
        this.compiler = compiler;
        this.scopes = scopes;
    }

    public Type lookup(ParserRuleContext<Token> ctx) {
        String name = Util.name(ctx);
        Scope scope = get(ctx);
        Type type= scope.lookup(name);
        if(type == null) { 
            String msg = "unknown type: " + name;
            compiler.reportError(ctx, msg);
        }
        return type;
    }
    
    public Symbol resolve(ParserRuleContext<Token> ctx) {
        String name = Util.name(ctx);
        Scope scope = get(ctx);
        Symbol symbol = scope.resolve(name);
        if(symbol == null) { 
            String msg = "unknown symbol: " + name;
            compiler.reportError(ctx, msg);
        }
        return symbol;
    }

    public Scope get(ParserRuleContext<Token> ctx) {
        return scopes.get(ctx);
    }
}
