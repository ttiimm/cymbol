package cymbol.model;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnitSource {

    @Model public List<Variable> globalVars = new ArrayList<Variable>();
    @Model public List<Struct> globalStructs = new ArrayList<Struct>();
    @Model public List<MethodFunction> globalMethodFuncs = new ArrayList<MethodFunction>();

    public void add(Variable var) {
        globalVars.add(var);
    }

    public void add(Struct struct) {
        globalStructs.add(struct);
    }

    public void add(MethodFunction methodFunction) {
        globalMethodFuncs.add(methodFunction);
    }
    
}
