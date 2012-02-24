package cymbol.compiler;

import cymbol.compiler.CymbolParser.compilationUnitContext;
import cymbol.compiler.CymbolParser.methodDeclarationContext;
import cymbol.compiler.CymbolParser.structDeclarationContext;
import cymbol.compiler.CymbolParser.varDeclarationContext;
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
    public void enter(compilationUnitContext ctx) {
        for(varDeclarationContext var : ctx.getRuleContexts(varDeclarationContext.class)) {
            Symbol symbol = var.props.symbol;
            src.add(new VariableDeclaration(symbol));
        }
        
        for(structDeclarationContext struct : ctx.getRuleContexts(structDeclarationContext.class)) {
            StructSymbol s = (StructSymbol) struct.props.symbol;
            src.add(new Struct(s));
        }
        
        for(methodDeclarationContext method : ctx.getRuleContexts(methodDeclarationContext.class)) {
            Symbol s = method.props.symbol;
            src.add(new FunctionDeclaration(s));
        }
    }

}
