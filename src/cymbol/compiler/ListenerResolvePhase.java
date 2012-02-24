package cymbol.compiler;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.compiler.CymbolParser.compilationUnitContext;
import cymbol.compiler.CymbolParser.exprContext;
import cymbol.compiler.CymbolParser.expr_primaryContext;
import cymbol.compiler.CymbolParser.methodDeclarationContext;
import cymbol.compiler.CymbolParser.primaryContext;
import cymbol.compiler.CymbolParser.primitiveTypeContext;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerResolvePhase extends CymbolBaseListener {
    
    private Compiler compiler;

    public ListenerResolvePhase(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public void enter(compilationUnitContext ctx) {
       List<? extends methodDeclarationContext> contexts = ctx.getRuleContexts(methodDeclarationContext.class);
       for(methodDeclarationContext mctx : contexts) {
           MethodSymbol method = (MethodSymbol) ctx.props.scope.resolve(mctx.ID().getText());
           method.type = (Type) ctx.props.scope.resolve(mctx.start.getText());
       }
    }

    @Override
    public void exit(CymbolParser.varDeclarationContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.ID().getText(), ctx.type().props.type);
        ctx.props.scope.define(var);
        ctx.props.symbol = var;
    }

    @Override
    public void exit(CymbolParser.structMemberContext ctx) {
        if (ctx.type() != null) {
            Symbol s = resolve(ctx.ID().getText(), ctx.props.scope, ctx);
            s.type = ctx.type().props.type;
        }
    }

    @Override
    public void exit(CymbolParser.parameterContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.ID().getText(), ctx.type().props.type);
        ctx.props.scope.define(var);
    }
    
    @Override
    public void exit(CymbolParser.typeContext ctx) {
        if(ctx.ID() != null) { ctx.props.type = (Type) resolve(ctx.ID().getText(), ctx.props.scope, ctx); }
        if(ctx.primitiveType() != null) { ctx.props.type = ctx.primitiveType().props.type;}
    }


    @Override
    public void enter(exprContext ctx) {
//        System.out.println(ctx.start + " " + ctx.stop);
        ctx.props = new CymbolProperties();
        // if struct ref, then scope is not set correctly on member
//        if(ctx.member != null) {
//            Symbol structVar = resolve(ctx.e1.p.id.getText(), ctx.e1.p.props.scope, ctx);
//            StructSymbol struct = (StructSymbol) structVar.type;
//            ctx.member.p.props.scope = struct;
//        }
    }

    @Override
    public void exit(exprContext ctx) {
//        System.out.println("BYE: " +ctx.start + " " + ctx.stop);
        ctx.props.type = ctx.expr(0).props.type;
    }

    @Override
    public void exit(expr_primaryContext ctx) {
        ctx.props.type = ctx.primary().props.type;
    }

    @Override
    public void enter(primitiveTypeContext ctx) {
        setType(ctx.props, ctx.start);
    }

    @Override
    public void enter(primaryContext ctx) {
        setType(ctx.props, ctx.start);
    }

    private void setType(CymbolProperties props, Token start) {
        int tokenValue = start.getType();
        String tokenName = start.getText();
        if(tokenValue == CymbolParser.ID) {
            Symbol s = props.scope.resolve(start.getText());
            props.type = s.type;
        } else if (tokenValue == CymbolParser.INT || 
                   tokenName.equals("int")) {
            props.type = SymbolTable.INT;   
        } else if (tokenValue == CymbolParser.FLOAT ||
                   tokenName.equals("float")) {
            props.type = SymbolTable.FLOAT;            
        } else if (tokenValue == CymbolParser.CHAR ||
                   tokenName.equals("char")) {
            props.type = SymbolTable.CHAR;
        } else if (tokenName.equals("true") ||
                   tokenName.equals("false")||
                   tokenName.equals("boolean")) {
            props.type = SymbolTable.BOOLEAN;            
        } else if (tokenName.equals("void")) {
            props.type = SymbolTable.VOID;
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