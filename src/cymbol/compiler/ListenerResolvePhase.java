package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.Expr_ArrayContext;
import cymbol.compiler.CymbolParser.Expr_BinaryContext;
import cymbol.compiler.CymbolParser.Expr_CallContext;
import cymbol.compiler.CymbolParser.Expr_GroupContext;
import cymbol.compiler.CymbolParser.Expr_MemberContext;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.Expr_UnaryContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.ParameterContext;
import cymbol.compiler.CymbolParser.PrimaryContext;
import cymbol.compiler.CymbolParser.PrimitiveTypeContext;
import cymbol.compiler.CymbolParser.StructMemberContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;
import cymbol.symtab.SymbolTable;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerResolvePhase extends CymbolBaseListener {
    
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int ARRAY_EXPR = 0;
    private static final int FUNC_EXPR = 0;
    private static final int STRUCT = 0;
    private static final int MEMBER_PARENT = 2;
    private static final int MEMBER = 0;
    
    private ScopeUtil scopes;
    public ParseTreeProperty<Type> types;
    private Compiler compiler;

    public ListenerResolvePhase(ScopeUtil scopes, Compiler compiler) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<Type>();
        this.compiler = compiler;
    }


    @Override
    public void exitVarDeclaration(VarDeclarationContext ctx) {
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);
        var.isArray = Util.isArrayDeclaration(ctx); 
        if(type == null) { compiler.reportError(ctx, "Unknown type when declaring variable: " + var); }
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
        var.isArray = Util.isArrayDeclaration(ctx);
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
    public void exitExpr_Call(Expr_CallContext ctx) {
        copyType(ctx.expr(FUNC_EXPR), ctx);
    }

    @Override
    public void exitExpr_Array(Expr_ArrayContext ctx) {
        copyType(ctx.expr(ARRAY_EXPR), ctx);
    }

    @Override
    public void exitExpr_Group(Expr_GroupContext ctx) {
        copyType(ctx.expr(), ctx);
    }

    @Override
    public void visitTerminal(TerminalNode<Token> node) {
        if(node.getSymbol().getText().equals(".")) {
            ParserRuleContext<Token> parent = (ParserRuleContext<Token>) node.getParent();
            StructSymbol struct = (StructSymbol) types.get(parent.getChild(STRUCT));
            ParserRuleContext<Token> member = (ParserRuleContext<Token>) parent.getChild(MEMBER_PARENT).getChild(MEMBER);
            String name = member.start.getText();
            Type memberType = struct.resolveMember(name).type;
            stashType(member, memberType);
        }
    }

    @Override
    public void exitExpr_Member(Expr_MemberContext ctx) {
        copyType(ctx.expr(RIGHT), ctx);
    }

    @Override
    public void exitExpr_Binary(Expr_BinaryContext ctx) {
        copyType(ctx.expr(LEFT), ctx);
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
        // already defined type as in the case of struct members
        if(types.get(ctx) != null) { return; }
        
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