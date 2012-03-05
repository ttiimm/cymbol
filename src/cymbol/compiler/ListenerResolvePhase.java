package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.Expr_UnaryContext;
import cymbol.compiler.CymbolParser.ExpressionContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.ParameterContext;
import cymbol.compiler.CymbolParser.PrimaryContext;
import cymbol.compiler.CymbolParser.PrimitiveTypeContext;
import cymbol.compiler.CymbolParser.StructMemberContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerResolvePhase extends CymbolBaseListener {
    
    private ScopeUtil scopes;
    public ParseTreeProperty<Type> types;

    public ListenerResolvePhase(ScopeUtil scopes) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<Type>();
    }


    @Override
    public void exitVarDeclaration(VarDeclarationContext ctx) {
        Type type = scopes.lookup(ctx.type());
        String varName = Util.name(ctx);
        VariableSymbol var = new VariableSymbol(varName, type);
        
        Scope scope = scopes.get(ctx);
        scope.define(var);
    }


    @Override
    public void exitStructMember(StructMemberContext ctx) {
        if (ctx.type() != null) {
            Symbol s = scopes.resolve(ctx);
            Type type = scopes.lookup(ctx.type());
            s.type = type;
        }
    }

    @Override
    public void exitParameter(ParameterContext ctx) {
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);
        Scope scope = scopes.get(ctx);
        scope.define(var);
    }
    
    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        Symbol method = scopes.resolve(ctx);
        String returnType = ctx.type().getStart().getText();
        method.type = method.scope.lookup(returnType);
    }


    @Override
    public void enterExpression(ExpressionContext ctx) {
//        System.out.println(ctx.start + " " + ctx.stop);
//        ctx.props = new CymbolProperties();
        // if struct ref, then scope is not set correctly on member
//        if(ctx.member != null) {
//            Symbol structVar = resolve(ctx.e1.p.id.getText(), ctx.e1.p.props.scope, ctx);
//            StructSymbol struct = (StructSymbol) structVar.type;
//            ctx.member.p.props.scope = struct;
//        }
    }

    @Override
    public void exitExpression(ExpressionContext ctx) {
//        System.out.println("BYE: " +ctx.start + " " + ctx.stop);
        copyType(ctx.expr(0), ctx);
    }

    @Override
    public void exitExpr_Unary(Expr_UnaryContext ctx) {
        copyType(ctx.expr(), ctx);
    }

    @Override
    public void exitExpr_Primary(Expr_PrimaryContext ctx) {
        copyType(ctx.primary(), ctx);
    }

    @Override
    public void enterPrimitiveType(PrimitiveTypeContext ctx) {
        setType(ctx);
    }

    @Override
    public void enterPrimary(PrimaryContext ctx) {
        setType(ctx);
    }

    private void setType(ParserRuleContext<Token> ctx) {
        int tokenValue = ctx.start.getType();
        String tokenName = ctx.start.getText();
        if(tokenValue == CymbolParser.ID) {
            Scope scope = scopes.get(ctx);
            Symbol s = scope.resolve(ctx.start.getText());
            stashType(ctx, s.type);
        } else if (tokenValue == CymbolParser.INT || 
                   tokenName.equals("int")) {
            stashType(ctx, SymbolTable.INT);   
        } else if (tokenValue == CymbolParser.FLOAT ||
                   tokenName.equals("float")) {
            stashType(ctx, SymbolTable.FLOAT);            
        } else if (tokenValue == CymbolParser.CHAR ||
                   tokenName.equals("char")) {
            stashType(ctx, SymbolTable.CHAR);
        } else if (tokenName.equals("true") ||
                   tokenName.equals("false")||
                   tokenName.equals("boolean")) {
            stashType(ctx, SymbolTable.BOOLEAN);            
        } else if (tokenName.equals("void")) {
            stashType(ctx, SymbolTable.VOID);
        }
    }

    private void stashType(ParserRuleContext<Token> ctx, Type type) {
        types.put(ctx, type);
    }
    
    private void copyType(ParserRuleContext<Token> from, ParserRuleContext<Token> to) {
        Type type = types.get(from);
        types.put(to, type);
    }
    
}