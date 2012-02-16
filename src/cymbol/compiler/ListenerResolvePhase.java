package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.compiler.CymbolParser.exprContext;
import cymbol.compiler.CymbolParser.primaryContext;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerResolvePhase extends BlankCymbolListener {

    private Compiler compiler;


    public ListenerResolvePhase(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void exit(CymbolParser.varDeclarationContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        ctx.scope.define(var);
    }

    @Override
    public void exit(CymbolParser.structMemberContext ctx) {
        if (ctx.t != null) {
            Symbol s = resolve(ctx.name.getText(), ctx.scope, ctx);
            s.type = ctx.t.type;
        }
    }

    @Override
    public void enter(CymbolParser.methodDeclarationContext ctx) {
        ctx.ret.method = ctx.props.symbol;
    }

    @Override
    public void exit(CymbolParser.parameterContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        ctx.scope.define(var);
    }
    
    @Override
    public void exit(CymbolParser.typeContext ctx) {
        ctx.type = (Type) resolve(ctx.t, ctx.scope, ctx);
        if(ctx.method != null) { ctx.method.type = ctx.type; }
    }
    
    @Override
    public void enter(exprContext ctx) {
        // if struct ref, then scope is not set correctly on member
        if(ctx.member != null) {
            Symbol structVar = resolve(ctx.e1.p.id.getText(), ctx.e1.p.scope, ctx);
            StructSymbol struct = (StructSymbol) structVar.type;
            ctx.member.p.scope = struct;
        }
    }

    @Override
    public void exit(exprContext ctx) {
        if(ctx.e1 != null) { ctx.type = ctx.e1.type; }
        if(ctx.p != null) { ctx.type = ctx.p.type; }
        if(ctx.member != null) { ctx.type = ctx.member.type; }
    }

    @Override
    public void enter(primaryContext ctx) {
        if(ctx.id != null) {
            Symbol s = ctx.scope.resolve(ctx.id.getText());
            ctx.type = s.type;
        } else if(ctx.i != null) {
            ctx.type = SymbolTable.INT;
        } else if(ctx.f != null) {
            ctx.type = SymbolTable.FLOAT;
        } else if(ctx.c != null) {
            ctx.type = SymbolTable.CHAR;
        } else if(ctx.bool != null) {
            ctx.type = SymbolTable.BOOLEAN;
        }
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