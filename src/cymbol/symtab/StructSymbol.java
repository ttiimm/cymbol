package cymbol.symtab;

/***
 * Excerpted from "Language Implementation Patterns",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book information.
 ***/
import java.util.Map;
import java.util.LinkedHashMap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class StructSymbol extends ScopedSymbol implements Type, Scope {
    Map<String, Symbol> fields = new LinkedHashMap<String, Symbol>();

    public StructSymbol(String name, Scope parent,
            ParserRuleContext tree) {
        super(name, parent, tree);
    }

    /** For a.b, only look in fields to resolve b, not up scope tree */
    public Symbol resolveMember(String name) {
        return fields.get(name);
    }

    @Override
    public Map<String, Symbol> getMembers() {
        return fields;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public String toString() {
        return "struct " + name + ":{"
                + stripBrackets(fields.keySet().toString()) + "}";
    }
}
