package cymbol.compiler;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.Stat_VarDeclContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.model.Expression.Primary;
import cymbol.model.MethodFunction;
import cymbol.model.SourceFile;
import cymbol.model.Statement;
import cymbol.model.Statement.Block;
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
        
        for(VarDeclarationContext var : ctx.getRuleContexts(VarDeclarationContext.class)) {
            src.add((VariableDeclaration) models.get(var));
        }
        
        for(StructDeclarationContext struct : ctx.getRuleContexts(StructDeclarationContext.class)) {
            src.add((Struct) models.get(struct));
        }
        
        for(MethodDeclarationContext method : ctx.getRuleContexts(MethodDeclarationContext.class)) {
            src.add((MethodFunction) models.get(method));
        }
        
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
        for(ParserRuleContext<Token> var: ctx.getRuleContexts(Stat_VarDeclContext.class)) {
            block.add((Statement) models.get(var));
        }
        
        models.put(ctx, block);
    }


    @Override
    public void exitStat_VarDecl(Stat_VarDeclContext ctx) {
        copyModel(ctx.varDeclaration(), ctx);
    }

    @Override
    public void enterExpr_Primary(Expr_PrimaryContext ctx) {
       Primary p = new Primary(ctx.getStart().getText());
       models.put(ctx, p);
    }
    
    private void copyModel(ParserRuleContext<Token> from, ParserRuleContext<Token> to) {
       OutputModelObject model = models.get(from);
       models.put(to, model);
    }
}
    
