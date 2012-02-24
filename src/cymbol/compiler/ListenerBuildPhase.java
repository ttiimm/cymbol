package cymbol.compiler;

import cymbol.compiler.CymbolParser.compilationUnitContext;
import cymbol.compiler.CymbolParser.methodDeclarationContext;
import cymbol.compiler.CymbolParser.structDeclarationContext;
import cymbol.compiler.CymbolParser.varDeclarationContext;
import cymbol.model.SourceFile;
import cymbol.model.FunctionDeclaration;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;

public class ListenerBuildPhase extends CymbolBaseListener {

    private SourceFile src;

    public ListenerBuildPhase(SourceFile src) {
        this.src = src;
    }

    @Override
    public void enter(compilationUnitContext ctx) {
        for(varDeclarationContext var : ctx.getRuleContexts(varDeclarationContext.class)) {
            src.add(new VariableDeclaration());
        }
        
        for(structDeclarationContext struct : ctx.getRuleContexts(structDeclarationContext.class)) {
            src.add(new Struct());
        }
        
        for(methodDeclarationContext method : ctx.getRuleContexts(methodDeclarationContext.class)) {
            src.add(new FunctionDeclaration());
        }
    }

}
