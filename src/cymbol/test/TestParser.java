package cymbol.test;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.RuleContext;
import org.junit.Test;

import cymbol.compiler.Compiler;

public class TestParser {

    @Test
    public void precedenceWithMathOperations() {
        String source = "int a = x + y * z;";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type (primitiveType int)) a = " +
        		        "(expr (expr (primary x)) + (expr (expr (primary y)) * (expr (primary z)))) ;))";
        assertEquals(expect, t);
    }
    
    @Test
    public void precedenceWithGrouping() {
        String source = "int a = (x + y) * z;";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type (primitiveType int)) a = " +
        		        "(expr (expr ( (expr (expr (primary x)) + (expr (primary y))) )) * (expr (primary z))) ;))";
        assertEquals(expect, t);
    }
    
    @Test
    public void primitiveBooleanType() {
        String source = "bool a;";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type (primitiveType bool)) a ;))";
        assertEquals(expect, t);
    }
    
    @Test
    public void stringDeclaration() {
        String source = "String s;";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type (primitiveType String)) s ;))";
        assertEquals(expect, t);
    }
    
    @Test
    public void stringLiteral() {
        String source = "String s = \"hello world\";";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type (primitiveType String)) s = (expr (primary \"hello world\")) ;))";
        assertEquals(expect, t);
    }

    @Test
    public void newExpr() {
        String source = "Integer i = new Integer();";
        String t = runTest(source);
        String expect = "(compilationUnit (varDeclaration (type Integer) i = (expr new (expr (primary Integer)) ( )) ;))";
        assertEquals(expect, t);
    }

    public String runTest(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        Compiler c = new Compiler(in);
        return ((RuleContext) c.tree).toStringTree(c.parser);
    }
}
