package cymbol.compiler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.ExprContext;
import cymbol.compiler.CymbolParser.Expr_ArrayContext;
import cymbol.compiler.CymbolParser.Expr_BinaryContext;
import cymbol.compiler.CymbolParser.Expr_CallContext;
import cymbol.compiler.CymbolParser.Expr_GroupContext;
import cymbol.compiler.CymbolParser.Expr_MemberContext;
import cymbol.compiler.CymbolParser.Expr_NewContext;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.Expr_UnaryContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.Prim_IntContext;
import cymbol.compiler.CymbolParser.Prim_StringContext;
import cymbol.compiler.CymbolParser.StatContext;
import cymbol.compiler.CymbolParser.Stat_AssignContext;
import cymbol.compiler.CymbolParser.Stat_BlockContext;
import cymbol.compiler.CymbolParser.Stat_ConditionalContext;
import cymbol.compiler.CymbolParser.Stat_ReturnContext;
import cymbol.compiler.CymbolParser.Stat_StructDeclContext;
import cymbol.compiler.CymbolParser.Stat_VarDeclContext;
import cymbol.compiler.CymbolParser.StatementContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.model.Block;
import cymbol.model.Expressions.ConstructorExpression;
import cymbol.model.Expressions.Expression;
import cymbol.model.Expressions.GenericExpression;
import cymbol.model.Literals;
import cymbol.model.Literals.Literal;
import cymbol.model.MethodFunction;
import cymbol.model.SourceFile;
import cymbol.model.Statement;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.Symbol;
import cymbol.symtab.VariableSymbol;

public class ListenerBuildPhase extends CymbolBaseListener {

    private static final Statement CALL_CYMBOL_MAIN_STATEMENT = new Statement("_main();");
    
    private ScopeUtil scopes;
    public ParseTreeProperty<OutputModelObject> models;
    public List<Literal> stringLiterals = new ArrayList<Literal>();
    public List<Literal> intLiterals = new ArrayList<Literal>();
    private String sourceName;
    
    public ListenerBuildPhase(ScopeUtil scopes, ParseTreeProperty<OutputModelObject> models, String sourceName) {
        this.scopes = scopes;
        this.models = models;
        this.sourceName = sourceName;
    }

    @Override
    public void exitCompilationUnit(CompilationUnitContext ctx) {
        SourceFile src = new SourceFile(sourceName);
        
        src.addAll(getAll(ctx.getRuleContexts(VarDeclarationContext.class)));
        src.addAll(getAll(ctx.getRuleContexts(StructDeclarationContext.class)));
        src.addAll(getAll(ctx.getRuleContexts(MethodDeclarationContext.class)));
        src.stringLiterals = stringLiterals;
        src.intLiterals = intLiterals;
        
        models.put(ctx, src);
    }

    @Override
    public void exitStructDeclaration(StructDeclarationContext ctx) {
        Symbol s = scopes.resolve(ctx);
        Struct struct = new Struct(s);
        models.put(ctx, struct);
    }
    
    @Override
    public void exitVarDeclaration(VarDeclarationContext ctx) {
        Symbol s = scopes.resolve(ctx);
        VariableDeclaration var = new VariableDeclaration((VariableSymbol) s);
        Expression expr = (Expression) models.get(ctx.expr());
        var.add(expr);
        models.put(ctx, var);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        Symbol s = scopes.resolve(ctx);
        BlockContext blockCtx = ctx.block();
        Block block = (Block) models.get(blockCtx);
        MethodFunction func = new MethodFunction(s, block);
        
        
        if(func.name.equals("main")) {
            block.prepend(CALL_CYMBOL_MAIN_STATEMENT);
        }
        
        models.put(ctx, func);
    }

    @Override
    public void exitBlock(BlockContext ctx) {
        Block block = new Block();
        List<OutputModelObject> all = getAll(ctx.getRuleContexts(StatementContext.class));
        block.addAll(all);
        models.put(ctx, block);
    }

    @Override
    public void exitStat_Block(Stat_BlockContext ctx) {
        copyModel(ctx.block(), ctx);
    }

    @Override
    public void exitStat_StructDecl(Stat_StructDeclContext ctx) {
       copyModel(ctx.structDeclaration(), ctx);
    }

    @Override
    public void exitStat_VarDecl(Stat_VarDeclContext ctx) {
        copyModel(ctx.varDeclaration(), ctx);
    }

    @Override
    public void exitStat_Conditional(Stat_ConditionalContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("if(");
        Expression ifExpr = (Expression) models.get(ctx.expr());
        sb.append(ifExpr.getObj() + ") ");
        OutputModelObject ifStatement = models.get(ctx.statement(0));
        sb.append(ifStatement);
        StatementContext elseStatement = ctx.statement(1);
        if(elseStatement != null) {
            sb.append(" else " + models.get(elseStatement).toString());
        }
        
        models.put(ctx, new Statement(sb.toString()));
    }
    
    @Override
    public void exitStat_Return(Stat_ReturnContext ctx) {
        Expression expr = (Expression) models.get(ctx.expr());
        String optional = expr != null ? " " + expr.getObj() : "";
        models.put(ctx, new Statement("return" + optional + ";"));
    }

    @Override
    public void exitStat_Assign(Stat_AssignContext ctx) {
        Expression left = (Expression) models.get(ctx.expr(0));
        Expression right = (Expression) models.get(ctx.expr(1));
        Statement assign = new Statement(left.getObj() + " = " + right.getObj() + ";");
        models.put(ctx, assign);
    }
    
    @Override
    public void exitStat(StatContext ctx) {
        Expression expr = (Expression) models.get(ctx.expr());
        Statement statement = new Statement(expr.getObj() + ";");
        models.put(ctx, statement);
    }
    
    @Override
    public void exitExpr_Call(Expr_CallContext ctx) {
        String call_expr = buildCallString(ctx);
        models.put(ctx, new GenericExpression(call_expr));
    }

    @Override
    public void exitExpr_Array(Expr_ArrayContext ctx) {
        Expression array = (Expression) models.get(ctx.expr(0));
        Expression index = (Expression) models.get(ctx.expr(1));
        OutputModelObject access = new GenericExpression(array.getObj() + "[" + index.getObj() + "]");
        models.put(ctx, access);
    }

    @Override
    public void exitExpr_Member(Expr_MemberContext ctx) {
        Expression struct = (Expression) models.get(ctx.expr(0));
        Expression member = (Expression) models.get(ctx.getChild(2));
        String accessOp = "->";
        OutputModelObject memberAccess = new GenericExpression(struct.getObj() + accessOp + member.getObj());
        models.put(ctx, memberAccess);
    }

    @Override
    public void exitExpr_Unary(Expr_UnaryContext ctx) {
        Expression expr = (Expression) models.get(ctx.expr());
        OutputModelObject unary = new GenericExpression(ctx.start.getText() + expr.getObj());
        models.put(ctx, unary);
    }

    @Override
    public void exitExpr_Binary(Expr_BinaryContext ctx) {
        Expression left = (Expression) models.get(ctx.expr(0));
        Expression right = (Expression) models.get(ctx.expr(1));
        String theExpression = left.getObj() + " " + ctx.o.getText() + " " + right.getObj();
        OutputModelObject binary = new GenericExpression(theExpression);
        models.put(ctx, binary );
    }

    @Override
    public void exitExpr_Group(Expr_GroupContext ctx) {
        Expression expr = (Expression) models.get(ctx.expr());
        GenericExpression group = new GenericExpression("(" + expr.getObj() + ")");
        models.put(ctx, group);
    }

    @Override
    public void exitExpr_New(Expr_NewContext ctx) {
        String constructorCall = buildCallString(ctx);
        models.put(ctx, new ConstructorExpression(constructorCall));
    }

    @Override
    public void exitExpr_Primary(Expr_PrimaryContext ctx) {
        ParserRuleContext<Token> sl = ctx.getChild(Prim_StringContext.class, 0);
        ParserRuleContext<Token> il = ctx.getChild(Prim_IntContext.class, 0);
        OutputModelObject literal = models.get(sl);
        if(literal == null){ literal = models.get(il); }
        GenericExpression primary = literal != null ? new GenericExpression(literal) : new GenericExpression(ctx.getStart().getText());
        models.put(ctx, primary);
    }

    @Override
    public void exitPrim_Int(Prim_IntContext ctx) {
        int id = intLiterals.size();
        Literal literal = new Literals.IntLiteral(ctx.getStart().getText(), id);
        intLiterals.add(literal);
        models.put(ctx, literal);
    }

    @Override
    public void exitPrim_String(Prim_StringContext ctx) {
        int id = stringLiterals.size();
        Literal literal = new Literals.StringLiteral(ctx.getStart().getText(), id);
        stringLiterals.add(literal);
        models.put(ctx, literal);
    }
    
    
    private String buildCallString(ParserRuleContext<Token> ctx) {
        StringBuilder sb = new StringBuilder();
        Expression method = (Expression) models.get(ctx.getRuleContext(ExprContext.class, 0));
        String funcName = method.getObj();
        sb.append(funcName + "(");
        List<? extends ExprContext> args = ctx.getRuleContexts(ExprContext.class);
        
        for(int i = 1; i < args.size(); i++) {
            Expression arg = (Expression) models.get(args.get(i));
            sb.append(arg.getLiteral() + ", ");
        }
        
        if(args.size() > 1) {
            sb.setLength(sb.length() - 2);
            sb.append(")");
        } else {
            sb.append(")");            
        }
        
        return sb.toString();
    }

    private List<OutputModelObject> getAll(List<? extends ParserRuleContext<Token>> ctxs) {
        List<OutputModelObject> ms = new ArrayList<OutputModelObject>();
        for(ParserRuleContext<Token> ctx : ctxs) { ms.add(models.get(ctx)); }
        return ms;
    }
    
    private void copyModel(ParserRuleContext<Token> from, ParserRuleContext<Token> to) {
       OutputModelObject model = models.get(from);
       models.put(to, model);
    }
}
    
