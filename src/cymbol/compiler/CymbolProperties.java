package cymbol.compiler;

import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;

public class CymbolProperties {
    
    public Scope scope;
    public Type type;
    public Symbol symbol;
    
    public CymbolProperties(Scope scope, Type type, Symbol symbol) {
        this.scope = scope;
        this.type = type;
        this.symbol = symbol;
    }
    
    
}
