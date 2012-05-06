package cymbol.symtab;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
 ***/
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class MethodSymbol extends ScopedSymbol {
    Map<String, Symbol> orderedArgs = new LinkedHashMap<String, Symbol>();
    public boolean builtin = false;

    public MethodSymbol(String name, Type retType, Scope parent,
            ParserRuleContext<Token> token) {
        super(name, retType, parent);
    }

    public MethodSymbol(String name, Scope parent,
            ParserRuleContext<Token> tree) {
        super(name, parent, tree);
    }

    public Map<String, Symbol> getMembers() {
        return orderedArgs;
    }

    public String getName() {
        return name + "(" + stripBrackets(orderedArgs.keySet().toString())
                + ")";
    }
}
