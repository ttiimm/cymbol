package cymbol.compiler;

import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;
import cymbol.symtab.VariableSymbol;

public class ListenerRefPhase extends ListenerForCompilation implements
        CymbolListener {

    public ListenerRefPhase(Compiler compiler, Scope globals) {
        super(compiler, globals);
    }

    @Override
    public void enterRule(CymbolParser.varDeclarationContext ctx) {
    }

    @Override
    public void exitRule(CymbolParser.varDeclarationContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        current.define(var);
    }

    @Override
    public void enterRule(CymbolParser.structDeclarationContext ctx) {
        Symbol struct = resolve(ctx, ctx.name.getText());
        push((Scope) struct);
    }

    @Override
    public void exitRule(CymbolParser.structDeclarationContext ctx) {
        pop();
    }

    @Override
    public void enterRule(CymbolParser.structMemberContext ctx) {
    }

    @Override
    public void exitRule(CymbolParser.structMemberContext ctx) {
        if (ctx.t != null) {
            Symbol s = resolve(ctx, ctx.name.getText());
            s.type = ctx.t.type;
        }
    }

    @Override
    public void enterRule(CymbolParser.typeContext ctx) {

    }

    @Override
    public void exitRule(CymbolParser.typeContext ctx) {
        Symbol type = resolve(ctx, ctx.t);
        ctx.type = (Type) type;
    }

    @Override
    public void enterRule(CymbolParser.methodDeclarationContext ctx) {
        String name = ctx.name.getText();
        Symbol symbol = resolve(ctx, name);
        MethodSymbol method = (MethodSymbol) symbol;
        push(method);
    }

    @Override
    public void exitRule(CymbolParser.methodDeclarationContext ctx) {
        MethodSymbol method = (MethodSymbol) current;
        pop();
        method.type = ctx.ret.type;
    }

    @Override
    public void enterRule(CymbolParser.parameterContext ctx) {
    }

    @Override
    public void exitRule(CymbolParser.parameterContext ctx) {
        VariableSymbol var = new VariableSymbol(ctx.name.getText(), ctx.t.type);
        current.define(var);
    }

    @Override
    public void enterRule(CymbolParser.blockContext ctx) {
        push(ctx.scope);
    }

    @Override
    public void exitRule(CymbolParser.blockContext ctx) {
        pop();
    }

}