package cymbol.compiler;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.Expr_PrimaryContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.model.Expression.Primary;
import cymbol.model.MethodFunction;
import cymbol.model.SourceFile;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;

public class ListenerBuildPhase extends CymbolBaseListener {

    private SourceFile src;
    private ParseTreeProperty<CymbolProperties> properties;

    public ListenerBuildPhase(SourceFile src, ParseTreeProperty<CymbolProperties> properties) {
        this.src = src;
        this.properties = properties;
    }

    @Override
    public void exitCompilationUnit(CompilationUnitContext ctx) {
        for(VarDeclarationContext var : ctx.getRuleContexts(VarDeclarationContext.class)) {
            src.add((VariableDeclaration) properties.get(var).model);
        }
        
        for(StructDeclarationContext struct : ctx.getRuleContexts(StructDeclarationContext.class)) {
            StructSymbol s = (StructSymbol) struct.props.symbol;
            src.add(new Struct(s));
        }
        
        for(MethodDeclarationContext method : ctx.getRuleContexts(MethodDeclarationContext.class)) {
            Symbol s = method.props.symbol;
            src.add(new MethodFunction(s));
        }
    }

    @Override
    public void exitVarDeclaration(VarDeclarationContext ctx) {
      Symbol symbol = ctx.props.symbol;
      VariableDeclaration var = new VariableDeclaration(symbol);
      CymbolProperties exprProps = properties.get(ctx.expr());
      OutputModelObject expr = exprProps != null ? exprProps.model : null;
      var.add(expr);
      ctx.props.model = var;
      properties.put(ctx, ctx.props);
    }

    @Override
    public void enterExpr_Primary(Expr_PrimaryContext ctx) {
       Primary p = new Primary(ctx.getStart().getText());
       ctx.props.model = p;
       properties.put(ctx, ctx.props);
    }

    
}
