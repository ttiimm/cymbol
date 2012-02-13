package cymbol.compiler;

import cymbol.compiler.CymbolParser.parameterContext;
import cymbol.compiler.CymbolParser.primaryContext;
import cymbol.compiler.CymbolParser.typeContext;
import cymbol.compiler.CymbolParser.varDeclarationContext;
import cymbol.symtab.LocalScope;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.VariableSymbol;

public class ListenerDefinePhase extends BlankCymbolListener implements CymbolListener {

    private Scope current;

    public ListenerDefinePhase(Scope globals) {
        this.current = globals;
    }

    @Override
    public void enterRule(CymbolParser.structDeclarationContext ctx) {
        StructSymbol struct = new StructSymbol(ctx.name.getText(), current, ctx);
        current.define(struct);
        push(struct);
    }

    @Override
    public void exitRule(CymbolParser.structDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterRule(CymbolParser.methodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(ctx.name.getText(), current, ctx);
        current.define(method);
        ctx.method = method;
        push(method);
    }

    @Override
    public void exitRule(CymbolParser.methodDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterRule(CymbolParser.structMemberContext ctx) {
        if (ctx.name != null) {
            ctx.scope = current;
            VariableSymbol member = new VariableSymbol(ctx.name.getText());
            current.define(member);
        }
    }

    @Override
    public void enterRule(parameterContext ctx) {
        ctx.scope = current;
    }

    @Override
    public void enterRule(typeContext ctx) {
        ctx.scope = current;
    }

    @Override
    public void enterRule(varDeclarationContext ctx) {
        ctx.scope = current;
    }

    @Override
    public void enterRule(CymbolParser.blockContext ctx) {
        LocalScope local = new LocalScope(this.current);
        current = local;
        ctx.scope = local;
    }

    @Override
    public void exitRule(CymbolParser.blockContext ctx) {
        pop();
    }
    
    @Override
    public void enterRule(primaryContext ctx) {
        ctx.scope = current;
    }

    private void push(Scope scope) {
        this.current = scope;
    }

    private void pop() {
        this.current = current.getEnclosingScope();
    }

}