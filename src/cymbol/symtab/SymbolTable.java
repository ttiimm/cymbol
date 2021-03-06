package cymbol.symtab;

/***
 * Excerpted from "Language Implementation Patterns", published by The Pragmatic
 * Bookshelf. Copyrights apply to this code. It may not be used to create
 * training material, courses, books, articles, and the like. Contact us if you
 * are in doubt. We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/tpdsl for more book
 * information.
 ***/
public class SymbolTable {
    
    public static final BuiltInTypeSymbol INT = new BuiltInTypeSymbol("int");
    public static final BuiltInTypeSymbol FLOAT = new BuiltInTypeSymbol("float");
    public static final BuiltInTypeSymbol BOOLEAN = new BuiltInTypeSymbol("boolean");
    public static final BuiltInTypeSymbol CHAR = new BuiltInTypeSymbol("char");
    public static final BuiltInTypeSymbol STRING = new BuiltInTypeSymbol("String");
    public static final BuiltInTypeSymbol VOID = new BuiltInTypeSymbol("void");
    public static final BuiltInTypeSymbol NULL = new BuiltInTypeSymbol("null");
    public static final BuiltInTypeSymbol UNDEFINED = new BuiltInTypeSymbol("undefined");
    
    public GlobalScope globals = new GlobalScope();
    
    public SymbolTable() {
        globals.define(INT);
        globals.define(FLOAT);
        globals.define(BOOLEAN);
        globals.define(CHAR);
        globals.define(STRING);
        globals.define(VOID);
        globals.define(NULL);
        globals.define(UNDEFINED);
        
        MethodSymbol PRINTF = new MethodSymbol("printf", globals, null); 
        PRINTF.builtin = true;
        globals.define(PRINTF);
        MethodSymbol GC = new MethodSymbol("gc", globals, null); 
        GC.builtin = true;
        globals.define(GC);
    }

    public String toString() {
        return globals.toString();
    }
}
