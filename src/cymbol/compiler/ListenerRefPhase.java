package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerRefPhase extends BlankCymbolListener implements
        CymbolListener {

    private Compiler compiler;


    public ListenerRefPhase(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void exitRule(CymbolParser.varDeclarationContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        ctx.scope.define(var);
    }

    @Override
    public void exitRule(CymbolParser.structMemberContext ctx) {
        if (ctx.t != null) {
            Symbol s = resolve(ctx.name.getText(), ctx.scope, ctx);
            s.type = ctx.t.type;
        }
    }

    @Override
    public void exitRule(CymbolParser.typeContext ctx) {
        ctx.type = (Type) resolve(ctx.t, ctx.scope, ctx);
    }

    @Override
    public void exitRule(CymbolParser.methodDeclarationContext ctx) {
        ctx.method.type = ctx.ret.type;
    }

    @Override
    public void exitRule(CymbolParser.parameterContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        ctx.scope.define(var);
    }


    public Symbol resolve(String name, Scope scope, ParserRuleContext<Token> ctx) {
        Symbol symbol = scope.resolve(name);
        if(symbol == null) { 
            String msg = "unknown symbol: " + name;
            compiler.reportError(ctx, msg);
        }
        return symbol;
    }

}