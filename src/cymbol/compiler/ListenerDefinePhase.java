package cymbol.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.compiler.CymbolParser.CompilationUnitContext;
import cymbol.compiler.CymbolParser.MethodDeclarationContext;
import cymbol.compiler.CymbolParser.ParameterContext;
import cymbol.compiler.CymbolParser.PrimaryContext;
import cymbol.compiler.CymbolParser.StructDeclarationContext;
import cymbol.compiler.CymbolParser.StructMemberContext;
import cymbol.compiler.CymbolParser.TypeContext;
import cymbol.compiler.CymbolParser.VarDeclarationContext;
import cymbol.symtab.LocalScope;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.VariableSymbol;

public class ListenerDefinePhase extends CymbolBaseListener {

    private Scope theScope;
    public ParseTreeProperty<Scope> scopes;
    
    public ListenerDefinePhase(Scope globals) {
        this.theScope = globals;
        this.scopes = new ParseTreeProperty<Scope>();
    }

    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx) {
        stashScope(ctx);
    }

    @Override
    public void enterStructDeclaration(StructDeclarationContext ctx) {
        StructSymbol struct = new StructSymbol(Util.name(ctx), theScope, ctx);
        theScope.define(struct);
        stashScope(ctx);
        pushScope(struct);
    }

    @Override
    public void exitStructDeclaration(StructDeclarationContext ctx) {
        popScope();
    }

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(Util.name(ctx), theScope, ctx);
        theScope.define(method);
        stashScope(ctx);
        pushScope(method);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        popScope();
    }

    @Override
    public void exitStructMember(StructMemberContext ctx) {
        if (ctx.ID() != null) {
            stashScope(ctx);
            VariableSymbol member = new VariableSymbol(Util.name(ctx));
            theScope.define(member);
        }
    }

    @Override
    public void exitParameter(ParameterContext ctx) {
        stashScope(ctx);
    }

    @Override
    public void exitVarDeclaration(VarDeclarationContext ctx) {
        stashScope(ctx);
    }

    @Override
    public void enterBlock(BlockContext ctx) {
        LocalScope local = new LocalScope(this.theScope);
        theScope = local;
        stashScope(ctx);
    }

    @Override
    public void exitBlock(BlockContext ctx) {
        popScope();
    }
    
    @Override
    public void enterPrimary(PrimaryContext ctx) {
        stashScope(ctx);
    }
    
    @Override
    public void enterType(TypeContext ctx) {
        stashScope(ctx);
    }
    
    private void stashScope(ParserRuleContext<Token> ctx) {
        scopes.put(ctx, theScope);
    }

    private void pushScope(Scope scope) {
        this.theScope = scope;
    }

    private void popScope() {
        this.theScope = theScope.getEnclosingScope();
    }

}