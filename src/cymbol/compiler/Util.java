package cymbol.compiler;

import java.lang.reflect.Field;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree.TerminalNode;

public class Util {

    public static String name(ParserRuleContext<Token> ctx) {
        try {
            TerminalNode<Token> id = (TerminalNode<Token>) ctx.getClass().getMethod("ID", new Class[0])
                    .invoke(ctx, new Object[0]);
            if(id != null) { return id.getSymbol().getText(); }
            else { return ctx.start.getText(); }
        } catch (Throwable e) {
            throw new IllegalStateException("Context does not have an ID to derive name from "
                    + ctx.getClass() + "\nCause was\n" + e);
        }
    }
    
    public static boolean isArrayDeclaration(ParserRuleContext<Token> ctx) {
        try {
            Field array = ctx.getClass().getField("array");
            return array.get(ctx) != null;
        } catch (Throwable e) {
            throw new IllegalStateException("Context does not have an array field ["
                    + ctx.getClass() + "]\nCause was\n" + e);
        }
    }

}
