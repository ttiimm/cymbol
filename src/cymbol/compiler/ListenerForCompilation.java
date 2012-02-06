package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;

public class ListenerForCompilation {

    protected Compiler compiler;
    protected Scope current;

    public ListenerForCompilation(Compiler compiler, Scope globals) {
        this.compiler = compiler;
        this.current = globals;
    }

    protected void push(Scope scope) {
        this.current = scope;
    }

    protected void pop() {
        this.current = current.getEnclosingScope();
    }

    protected Symbol resolve(ParserRuleContext<Token> ctx, String name) {
        Symbol symbol = current.resolve(name);
        
        if(symbol == null) { 
            String msg = "unknown symbol: " + name;
            reportError(ctx, msg);
        }
        
        return symbol;
    }

    private void reportError(ParserRuleContext<Token> ctx, String msg) {
        Token start = ctx.getStart();
        int line = start.getLine();
        int pos = start.getCharPositionInLine();
        compiler.error(line + ":" + pos + ": " + msg);
    }

}