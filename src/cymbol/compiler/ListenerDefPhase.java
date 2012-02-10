package cymbol.compiler;

import cymbol.symtab.LocalScope;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.VariableSymbol;

public class ListenerDefPhase extends ListenerForCompilation implements
        CymbolListener {

    public ListenerDefPhase(Compiler compiler, Scope global) {
        super(compiler, global);
    }

    @Override
    public void enterRule(CymbolParser.structDeclarationContext ctx) {
        StructSymbol struct = new StructSymbol(ctx.name.getText(),
                this.current, ctx);
        current.define(struct);
        push(struct);
    }

    @Override
    public void exitRule(CymbolParser.structDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterRule(CymbolParser.methodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(ctx.name.getText(),
                this.current, ctx);
        current.define(method);
        push(method);
    }

    @Override
    public void exitRule(CymbolParser.methodDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterRule(CymbolParser.structMemberContext ctx) {
        if (ctx.name != null) {
            VariableSymbol field = new VariableSymbol(ctx.name.getText());
            current.define(field);
        }
    }

    @Override
    public void exitRule(CymbolParser.structMemberContext ctx) {
    }

    @Override
    public void enterRule(CymbolParser.blockContext ctx) {
        LocalScope local = new LocalScope(this.current);
        this.current = local;
        ctx.scope = local;
    }

    @Override
    public void exitRule(CymbolParser.blockContext ctx) {
        pop();
    }

}