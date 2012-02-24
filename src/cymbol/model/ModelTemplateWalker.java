/*
 [The "BSD license"]
 Copyright (c) 2011 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cymbol.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.codegen.model.OutputModelObject;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.compiler.FormalArgument;

import cymbol.compiler.Compiler;

public class ModelTemplateWalker {
    
    private STGroup templates = new STGroupFile("resources/Cymbol.stg");
    private Compiler compiler;

    public ModelTemplateWalker(Compiler compiler) {
        this.compiler = compiler;
    }
    
    public ST walk(OutputModelObject omo) {
        // CREATE TEMPLATE FOR THIS OUTPUT OBJECT
        Class<? extends OutputModelObject> cl = omo.getClass();
        String templateName = cl.getSimpleName();
        if ( templateName == null ) {
            compiler.error("Unknown OutputModelObject: " + templateName);
            return new ST("["+templateName+" invalid]");
        }
        ST st = templates.getInstanceOf(templateName);
        if ( st == null ) {
            compiler.error("Template not found: " + templateName);
            return new ST("["+templateName+" invalid]");
        }
        
        if ( st.impl.formalArguments == null ) {
            compiler.error("FormalArguments were null for template: " + templateName);
            return st;
        }

        Map<String,FormalArgument> formalArgs = st.impl.formalArguments;

        // PASS IN OUTPUT MODEL OBJECT TO TEMPLATE AS FIRST ARG
        Set<String> argNames = formalArgs.keySet();
        Iterator<String> arg_it = argNames.iterator();
        String modelArgName = arg_it.next(); // ordered so this is first arg
        st.add(modelArgName, omo);

        // COMPUTE STs FOR EACH NESTED MODEL OBJECT MARKED WITH @ModelElement AND MAKE ST ATTRIBUTE
        Field fields[] = cl.getFields();
        for (Field fi : fields) {
            Annotation[] annotations = fi.getAnnotations();
            if ( annotations.length==0 ) continue;
            String fieldName = fi.getName();
            // Just don't set @ModelElement fields w/o formal arg in target ST
            if ( formalArgs.get(fieldName)==null ) continue;
            try {
                Object o = fi.get(omo);
                if ( o instanceof OutputModelObject ) {  // SINGLE MODEL OBJECT?
                    OutputModelObject nestedOmo = (OutputModelObject)o;
                    ST nestedST = walk(nestedOmo);
//                  System.out.println("set ModelElement "+fieldName+"="+nestedST+" in "+templateName);
                    st.add(fieldName, nestedST);
                }
                else if ( o instanceof Collection || o instanceof OutputModelObject[] ) {
                    // LIST OF MODEL OBJECTS?
                    if ( o instanceof OutputModelObject[] ) {
                        o = Arrays.asList((OutputModelObject[])o);
                    }
                    Collection<? extends OutputModelObject> nestedOmos = (Collection)o;
                    for (OutputModelObject nestedOmo : nestedOmos) {
                        if ( nestedOmo==null ) continue;
                        ST nestedST = walk(nestedOmo);
//                      System.out.println("set ModelElement "+fieldName+"="+nestedST+" in "+templateName);
                        st.add(fieldName, nestedST);
                    }
                }
                else if ( o instanceof Map ) {
                    Map<Object, OutputModelObject> nestedOmoMap = (Map<Object, OutputModelObject>)o;
                    Map<Object, ST> m = new HashMap<Object, ST>();
                    for (Object key : nestedOmoMap.keySet()) {
                        ST nestedST = walk(nestedOmoMap.get(key));
//                      System.out.println("set ModelElement "+fieldName+"="+nestedST+" in "+templateName);
                        m.put(key, nestedST);
                    }
                    st.add(fieldName, m);
                }
                else if ( o!=null ) {
                    compiler.error("Unrecognized nested model element: " + fieldName);
                }
            }
            catch (IllegalAccessException iae) {
                compiler.error("Unexpected error while building templates", iae);
            }
        }
        //st.impl.dump();
        return st;
    }
    
}
