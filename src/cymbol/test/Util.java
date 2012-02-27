package cymbol.test;

import org.antlr.v4.runtime.ANTLRInputStream;

import cymbol.compiler.Compiler;
import cymbol.compiler.CymbolParser.BlockContext;
import cymbol.symtab.MethodSymbol;
import cymbol.symtab.Scope;

public class Util {
    
    public static Compiler runCompilerOn(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<String>";
        Compiler c = new Compiler(in);
        c.compile();

        return c;
    }
    
    public static Scope resolveLocalScope(MethodSymbol m) {
        return ((BlockContext) m.tree.getChild(4)).props.scope;
    }
}
