package cymbol.compiler;

import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.model.FunctionDeclaration;
import cymbol.model.SourceFile;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.Symbol;

public class ListenerBuildPhase extends CymbolBaseListener {

    private SourceFile src;

    public ListenerBuildPhase(SourceFile src) {
        this.src = src;
    }

    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx) {
        for(VarDeclarationContext var : ctx.getRuleContexts(VarDeclarationContext.class)) {
            Symbol symbol = var.props.symbol;
            src.add(new VariableDeclaration(symbol));
        }
        
        for(StructDeclarationContext struct : ctx.getRuleContexts(StructDeclarationContext.class)) {
            StructSymbol s = (StructSymbol) struct.props.symbol;
            src.add(new Struct(s));
        }
        
        for(MethodDeclarationContext method : ctx.getRuleContexts(MethodDeclarationContext.class)) {
            Symbol s = method.props.symbol;
            src.add(new FunctionDeclaration(s));
        }
    }

}
