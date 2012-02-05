
package cymbol.compiler;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import cymbol.symtab.GlobalScope;

public class ListenerRefPhase implements CymbolListener {
	
    private GlobalScope current;
    
    public ListenerRefPhase(GlobalScope globals) {
        this.current = globals;
    }
    
    @Override public void enterRule(CymbolParser.expressionContext ctx) { }
	@Override public void exitRule(CymbolParser.expressionContext ctx) { }
	@Override public void enterRule(CymbolParser.compilationUnitContext ctx) { }
	@Override public void exitRule(CymbolParser.compilationUnitContext ctx) { }
	@Override public void enterRule(CymbolParser.expressionListContext ctx) { }
	@Override public void exitRule(CymbolParser.expressionListContext ctx) { }
	@Override public void enterRule(CymbolParser.unaryExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.unaryExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.structMemberContext ctx) { }
	@Override public void exitRule(CymbolParser.structMemberContext ctx) { }
	@Override public void enterRule(CymbolParser.blockContext ctx) { }
	@Override public void exitRule(CymbolParser.blockContext ctx) { }
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
	@Override public void enterRule(CymbolParser.structDeclarationContext ctx) { }
	@Override public void exitRule(CymbolParser.structDeclarationContext ctx) { }
	@Override public void enterRule(CymbolParser.multiplicativeExpressionContext ctx) { }
	@Override public void exitRule(CymbolParser.multiplicativeExpressionContext ctx) { }
	@Override public void enterRule(CymbolParser.methodDeclarationContext ctx) { }
	@Override public void exitRule(CymbolParser.methodDeclarationContext ctx) { }
	@Override public void enterRule(CymbolParser.varDeclarationContext ctx) { }
	@Override public void exitRule(CymbolParser.varDeclarationContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext<Token > ctx) { }
	@Override public void exitEveryRule(ParserRuleContext<Token > ctx) { }
	@Override public void visitTerminal(ParserRuleContext<Token > ctx, Token symbol) { }
}