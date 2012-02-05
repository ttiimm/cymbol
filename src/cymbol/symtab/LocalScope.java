package cymbol.symtab;

import java.util.ArrayList;
import java.util.List;

/***
 * Excerpted from "Language Implementation Patterns", published by The Pragmatic
 * Bookshelf. Copyrights apply to this code. It may not be used to create
 * training material, courses, books, articles, and the like. Contact us if you
 * are in doubt. We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book
 * information.
 ***/
public class LocalScope extends BaseScope {
    
    List<LocalScope> nested = new ArrayList<LocalScope>();
    
    public LocalScope(Scope parent) {
        super(parent);
    }

    @Override
    public String getScopeName() {
        return "local";
    }

}
