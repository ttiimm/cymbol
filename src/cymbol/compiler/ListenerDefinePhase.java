package cymbol.compiler;

import cymbol.compiler.CymbolParser.compilationUnitContext;
import cymbol.compiler.CymbolParser.parameterContext;
import cymbol.compiler.CymbolParser.primaryContext;
import cymbol.compiler.CymbolParser.typeContext;
import cymbol.compiler.CymbolParser.varDeclarationContext;
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
    public void enter(compilationUnitContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enter(CymbolParser.structDeclarationContext ctx) {
        StructSymbol struct = new StructSymbol(ctx.ID().getText(), current, ctx);
        current.define(struct);
        push(struct);
    }

    @Override
    public void exit(CymbolParser.structDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enter(CymbolParser.methodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(ctx.ID().getText(), current, ctx);
        current.define(method);
        push(method);
    }

    @Override
    public void exit(CymbolParser.methodDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enter(CymbolParser.structMemberContext ctx) {
        if (ctx.ID() != null) {
            ctx.props.scope = current;
            VariableSymbol member = new VariableSymbol(ctx.ID().getText());
            current.define(member);
        }
    }

    @Override
    public void enter(parameterContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enter(typeContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enter(varDeclarationContext ctx) {
        ctx.props.scope = current;
    }

    @Override
    public void enter(CymbolParser.blockContext ctx) {
        LocalScope local = new LocalScope(this.current);
        current = local;
        ctx.props.scope = local;
    }

    @Override
    public void exit(CymbolParser.blockContext ctx) {
        pop();
    }
    
    @Override
    public void enter(primaryContext ctx) {
        ctx.props.scope = current;
    }

    private void push(Scope scope) {
        this.current = scope;
    }

    private void pop() {
        this.current = current.getEnclosingScope();
    }

}