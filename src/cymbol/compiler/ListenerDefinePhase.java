package cymbol.compiler;

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

    private Scope current;

    public ListenerDefinePhase(Scope globals) {
        this.current = globals;
    }

    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enterStructDeclaration(StructDeclarationContext ctx) {
        StructSymbol struct = new StructSymbol(ctx.ID().getText(), current, ctx);
        current.define(struct);
        ctx.props.symbol = struct;
        push(struct);
    }

    @Override
    public void exitStructDeclaration(StructDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(ctx.ID().getText(), current, ctx);
        current.define(method);
        push(method);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterStructMember(StructMemberContext ctx) {
        if (ctx.ID() != null) {
            ctx.props.scope = current;
            VariableSymbol member = new VariableSymbol(ctx.ID().getText());
            current.define(member);
        }
    }

    @Override
    public void enterParameter(ParameterContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enterType(TypeContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enterVarDeclaration(VarDeclarationContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enterBlock(BlockContext ctx) {
        LocalScope local = new LocalScope(this.current);
        current = local;
        ctx.props.scope = local;
    }

    @Override
    public void exitBlock(BlockContext ctx) {
        pop();
    }
    
    @Override
    public void enterPrimary(PrimaryContext ctx) {
        ctx.props.scope = current;
    }

    private void push(Scope scope) {
        this.current = scope;
    }

    private void pop() {
        this.current = current.getEnclosingScope();
    }

}