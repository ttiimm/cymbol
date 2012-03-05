package cymbol.compiler;

import org.antlr.v4.codegen.model.OutputModelObject;

import cymbol.symtab.Scope;
import cymbol.symtab.Symbol;
import cymbol.symtab.Type;

public class CymbolProperties {

    public Scope scope;
    public Type type;
    public Symbol symbol;
    public OutputModelObject model;

}
