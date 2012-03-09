package cymbol.compiler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.Expr_BinaryContext;
import cymbol.compiler.CymbolParser.Expr_GroupContext;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.Expr_UnaryContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.StatContext;
import cymbol.compiler.CymbolParser.Stat_BlockContext;
import cymbol.compiler.CymbolParser.Stat_StructDeclContext;
import cymbol.compiler.CymbolParser.Stat_VarDeclContext;
import cymbol.compiler.CymbolParser.StatementContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.model.Block;
import cymbol.model.Expression;
import cymbol.model.MethodFunction;
import cymbol.model.SourceFile;
import cymbol.model.Statement;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.Symbol;

public class ListenerBuildPhase extends CymbolBaseListener {

    private ScopeUtil scopes;
    public ParseTreeProperty<OutputModelObject> models;
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
        VariableDeclaration var = new VariableDeclaration(s);
        OutputModelObject expr = models.get(ctx.expr());
        var.add(expr);
        models.put(ctx, var);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        Symbol s = scopes.resolve(ctx);
        BlockContext blockCtx = ctx.block();
        Block block = (Block) models.get(blockCtx);
        MethodFunction func = new MethodFunction(s, block);
        models.put(ctx, func);
    }

    @Override
    public void exitBlock(BlockContext ctx) {
        Block block = new Block();
        List<OutputModelObject> vars = getAll(ctx.getRuleContexts(Stat_VarDeclContext.class));
        block.addAll(vars);
        List<OutputModelObject> structs = getAll(ctx.getRuleContexts(Stat_StructDeclContext.class));
        block.addAll(structs);
        List<OutputModelObject> all = getAll(ctx.getRuleContexts(StatementContext.class));
        all.removeAll(vars);
        all.removeAll(structs);
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
    public void exitStat(StatContext ctx) {
        OutputModelObject expr = models.get(ctx.expr());
        Statement statement = new Statement(expr.toString() + ";");
        models.put(ctx, statement);
    }
    
    @Override
    public void exitExpr_Unary(Expr_UnaryContext ctx) {
        OutputModelObject expr = models.get(ctx.expr());
        OutputModelObject unary = new Expression(ctx.start.getText() + " " + expr.toString());
        models.put(ctx, unary);
    }

    @Override
    public void exitExpr_Binary(Expr_BinaryContext ctx) {
        OutputModelObject left = models.get(ctx.expr(0));
        OutputModelObject right = models.get(ctx.expr(1));
        String theExpression = left.toString() + " " + ctx.o.getText() + " " + right.toString();
        OutputModelObject binary = new Expression(theExpression);
        models.put(ctx, binary );
    }

    @Override
    public void exitExpr_Group(Expr_GroupContext ctx) {
        OutputModelObject expr = models.get(ctx.expr());
        Expression group = new Expression("(" + expr.toString() + ")");
        models.put(ctx, group);
    }

    @Override
    public void enterExpr_Primary(Expr_PrimaryContext ctx) {
       Expression primary = new Expression(ctx.getStart().getText());
       models.put(ctx, primary);
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
    
