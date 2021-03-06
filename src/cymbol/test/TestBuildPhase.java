package cymbol.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;

import cymbol.compiler.Compiler;
import cymbol.model.MethodFunction;
import cymbol.model.SourceFile;
import cymbol.model.Struct;
import cymbol.model.VariableDeclaration;
import cymbol.symtab.Scope;
import cymbol.symtab.Type;

public class TestBuildPhase {

    @Test
    public void sourceFile() {
        String source = "int x;";
        SourceFile src = runCompilerOn(source);
        assertEquals("<String>", src.name);
    }

    @Test
    public void stringLiteral() {
        String source = "String s = \"hi\";";
        SourceFile src = runCompilerOn(source);
        assertEquals("_String_literals[0]", src.stringLiterals.get(0).getObj());
        assertEquals("_String_literals[0]->elements", src.stringLiterals.get(0).getPrimitive());
    }

    @Test
    public void var() {
        String source = "int x;";
        SourceFile src = runCompilerOn(source);
        VariableDeclaration var = src.vars.get(0);
        assertEquals("Int *x;", var.toString());
    }

    @Test
    public void varDeclWithPrimary() {
        String source = "int x = 1;";
        SourceFile src = runCompilerOn(source);
        VariableDeclaration var = src.vars.get(0);
        assertEquals("Int *x = _Int_literals[0];", var.toString());
    }

    @Test
    public void varDeclAsArrat() {
        String source = "int x[];";
        SourceFile src = runCompilerOn(source);
        VariableDeclaration var = src.vars.get(0);
        assertEquals("Int *x[];", var.toString());
    }

    @Test
    public void multipleVarDecls() {
        String source = "int x; int y;";
        SourceFile src = runCompilerOn(source);
        VariableDeclaration x = src.vars.get(0);
        assertEquals("Int *x;", x.toString());
        VariableDeclaration y = src.vars.get(1);
        assertEquals("Int *y;", y.toString());
    }

    
    @Test
    public void struct() {
        String source = "struct A { int x; }";
        SourceFile src = runCompilerOn(source);
        Struct struct = src.structs.get(0);
        assertEquals("A", struct.name);
        assertEquals("Int *x;", struct.vars.get(0).toString());
    }
    
    @Test
    public void nestedStruct() {
        String source = "struct A { struct B { int x; } }";
        SourceFile src = runCompilerOn(source);
        Struct struct = src.structs.get(0);
        assertEquals("A", struct.name);
        Struct nested = struct.nested.get(0);
        assertEquals("B", nested.name);
        assertEquals("Int *x;", nested.vars.get(0).toString());
    }
    
    @Test
    public void emptyFunc() {
        String source = "void foo(float y){ }";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[float y]", f.toString());
        assertNotNull(f.block);
    }

    @Test
    public void funcWithVarDeclaration() {
        String source = "void foo(float y){ int x; }";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[float y]", f.toString());
        assertEquals("Int *x;", f.block.statements.get(0).toString());
    }

    @Test
    public void funcWithStructDeclaration() {
        String source = "void foo(){\n" +
        		        "    struct A {\n" +
        		        "        int x;\n" +
        		        "    }\n" +
        		        "}";
        String struct = "struct A {\n" +
        		        "    []\n" +
        		        "    [Int *x;]\n" +
        		        "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(struct, f.block.structs.get(0).toString());
    }
    
    @Test
    public void funcWithNested() {
        String source = "void foo(){\n" +
                        "    {\n" +
                        "        int x;\n" +
                        "    }\n" +
                        "}";
        String block = "{\n" +
        		       "    {\n" +
                       "    Int *x;\n" +
                       "    ADD_ROOT(x);\n" +
                       "}\n" +
                       "\n" +
                       "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    @Test
    public void funcWithParanExpr() {
        String source = "void foo() {\n" +
        		        "    (1);" +
        		        "}";
        String block = "{\n" +
        		        "    (_Int_literals[0]);\n" +
        		        "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithConditionalExpr() {
        String source = "void foo() {\n" +
                "    1 == 2;" +
                "}";
        String block = "{\n" +
                "    _Int_literals[0]->value == _Int_literals[1]->value;\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithAdditiveExpr() {
        String source = "void foo() {\n" +
                "    1 + 2;" +
                "}";
        String block = "{\n" +
                "    _Int_literals[0]->value + _Int_literals[1]->value;\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithMultiplicativeExpr() {
        String source = "void foo() {\n" +
                "    1 * 2;" +
                "}";
        String block = "{\n" +
                "    _Int_literals[0]->value * _Int_literals[1]->value;\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithNegationExpr() {
        String source = "void foo() {\n" +
                "    -(1 + 2);" +
                "}";
        String block = "{\n" +
                "    -(_Int_literals[0]->value + _Int_literals[1]->value);\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    @Test
    public void funcWithComplimentExpr() {
        String source = "void foo() {\n" +
                "    !true;" +
                "}";
        String block = "{\n" +
                "    !true;\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithMemberAccessExpr() {
        String source = "struct A { int x; }" +
                		"void foo() {\n" +
                		"    A a;" +
                        "    a.x;" +
                        "}";
        String block = "{\n" +
                	   "    A *a;\n" +
                	   "    ADD_ROOT(a);\n" +
                       "    a->x;\n" +
                       "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithArrayAccessExpr() {
        String source = "void foo() {\n" +
                        "    int a[];" +
                        "    a[0];" +
                        "}";
        
        String block = "{\n" +
                       "    Int *a[];\n" +
                       "    ADD_ROOT(a);\n" +
                       "    a[_Int_literals[0]->value];\n" +
                       "}\n";
        
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithCallExpr() {
        String source = "void foo() {\n" +
                        "    foo();" +
                        "}";
        String block = "{\n" +
                       "    foo();\n" +
                       "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    @Test
    public void funcWithAssignment() {
        String source = "void foo() {\n" +
        		        "    int y = 0;" +
                        "    int x;" +
                        "    x = y;" +
                        "}";
        String block = "{\n" +
                       "    Int *y = _Int_literals[0];\n" +
                       "    ADD_ROOT(y);\n" +
                       "    Int *x;\n" +
                       "    ADD_ROOT(x);\n" +
                       "    x = y;\n" +
                       "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    @Test
    public void funcWithConditional() {
        String source = "void foo() {\n" +
                "    if(true) 1;" +
                "}";
        String block = "{\n" +
                "    if(true) _Int_literals[0];\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithConditionalWithElse() {
        String source = "void foo() {\n" +
                "    if(true) 1; else 2;" +
                "}";
        String block = "{\n" +
                "    if(true) _Int_literals[0]; else _Int_literals[1];\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }

    @Test
    public void funcWithReturn() {
        String source = "void foo() {\n" +
                "    return;" +
                "}";
        String block = "{\n" +
                "    GC_RESTORE_RP;\n" +
                "    return;\n" +
                "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    @Test
    public void funcWithReturnWithExpr() {
        String source = "void foo() {\n" +
                        "    return 42;" +
                        "}";
        String block = "{\n" +
                       "    GC_RESTORE_RP;\n" +
                       "    return _Int_literals[0];\n" +
                       "}\n";
        SourceFile src = runCompilerOn(source);
        MethodFunction f = src.functionDefinitions.get(0);
        assertEquals("void foo[]", f.toString());
        assertEquals(block, f.block.toString());
    }
    
    public SourceFile runCompilerOn(String source) {
        ANTLRInputStream in = new ANTLRInputStream(source);
        in.name = "<String>";
        Compiler c = new Compiler(in);
        ParseTreeProperty<Scope> scopes = c.define();
        ParseTreeProperty<Type> types = c.resolve(scopes);
        SourceFile src = c.build(scopes, types);
        
        return src;
    }
}
