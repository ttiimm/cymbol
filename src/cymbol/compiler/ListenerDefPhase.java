
package cymbol.compiler;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.symtab.LocalScope;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;
import cymbol.symtab.StructSymbol;
import cymbol.symtab.VariableSymbol;


public class ListenerDefPhase implements CymbolListener {
    
    private Compiler compiler;
    private Scope current;
	
    public ListenerDefPhase(Compiler compiler, Scope global) {
        this.current = global;
        this.compiler = compiler;
    }
	
    @Override public void enterRule(CymbolParser.structDeclarationContext ctx) {
       StructSymbol struct = new StructSymbol(ctx.name.getText(), this.current, ctx);
       current.define(struct);
       push(struct);
    }

    @Override public void exitRule(CymbolParser.structDeclarationContext ctx) { 
        pop();
    }
    
    @Override public void enterRule(CymbolParser.methodDeclarationContext ctx) {
        MethodSymbol method = new MethodSymbol(ctx.name.getText(), this.current, ctx);
        current.define(method);
        push(method);
    }
    
    @Override public void exitRule(CymbolParser.methodDeclarationContext ctx) {
        pop();
    }
    
    @Override public void enterRule(CymbolParser.structMemberContext ctx) {
        if(ctx.name != null) {
            VariableSymbol field = new VariableSymbol(ctx.name.getText());
            current.define(field);
        }
    }
    
    @Override public void exitRule(CymbolParser.structMemberContext ctx) { }

    @Override public void enterRule(CymbolParser.blockContext ctx) { 
        LocalScope local = new LocalScope(this.current);
        this.current = local;
        ctx.scope = local;
    }
    
    @Override public void exitRule(CymbolParser.blockContext ctx) { 
        pop();
    }
    
    private void push(Scope scope) {
        this.current = scope;
    }
    
    private void pop() {
        this.current = current.getEnclosingScope();
    }
        
    @Override public void enterRule(CymbolParser.varDeclarationContext ctx) { }
    @Override public void exitRule(CymbolParser.varDeclarationContext ctx) { }
    @Override public void enterRule(CymbolParser.compilationUnitContext ctx) { }
    @Override public void exitRule(CymbolParser.compilationUnitContext ctx) { }
    @Override public void enterRule(CymbolParser.expressionContext ctx) { }
    @Override public void exitRule(CymbolParser.expressionContext ctx) { }
	@Override public void enterRule(CymbolParser.expressionListContext ctx) { }
	@Override public void exitRule(CymbolParser.expressionListContext ctx) { }
	@Override public void enterRule(CymbolParser.unaryExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.unaryExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.additiveExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.additiveExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.exprContext ctx) { }
	@Override public void exitRule(CymbolParser.exprContext ctx) { }
	@Override public void enterRule(CymbolParser.typeContext ctx) { }
	@Override public void exitRule(CymbolParser.typeContext ctx) { }
	@Override public void enterRule(CymbolParser.postfixExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.postfixExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.lhsContext ctx) { }
	@Override public void exitRule(CymbolParser.lhsContext ctx) { }
	@Override public void enterRule(CymbolParser.statementContext ctx) { }
	@Override public void exitRule(CymbolParser.statementContext ctx) { }
	@Override public void enterRule(CymbolParser.equalityExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.equalityExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.parameterContext ctx) { }
	@Override public void exitRule(CymbolParser.parameterContext ctx) { }
	@Override public void enterRule(CymbolParser.formalParametersContext ctx) { }
	@Override public void exitRule(CymbolParser.formalParametersContext ctx) { }
	@Override public void enterRule(CymbolParser.primitiveTypeContext ctx) { }
	@Override public void exitRule(CymbolParser.primitiveTypeContext ctx) { }
	@Override public void enterRule(CymbolParser.primaryContext ctx) { }
	@Override public void exitRule(CymbolParser.primaryContext ctx) { }
	@Override public void enterRule(CymbolParser.relationalExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.relationalExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.multiplicativeExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.multiplicativeExpressionContext ctx) { }
	@Override public void enterEveryRule(ParserRuleContext<Token > ctx) { }
	@Override public void exitEveryRule(ParserRuleContext<Token > ctx) { }
	@Override public void visitTerminal(ParserRuleContext<Token > ctx, Token symbol) { }
}