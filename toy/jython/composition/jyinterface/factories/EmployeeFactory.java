// EmployeeFactory.java

package jyinterface.factories;

import jyinterface.interfaces.EmployeeType;
import jyinterface.interfaces.DependentType;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyInteger;
import org.python.util.PythonInterpreter;

public class EmployeeFactory {

    public EmployeeFactory() {
        String cmd = "from Employee import Employee\nfrom Employee import Dependent";
        PythonInterpreter interpreter = new PythonInterpreter();
        //interpreter.exec("from Employee import Employee");
        //interpreter.exec("from Employee import Dependent");
        interpreter.exec(cmd);
        jyEmployeeClass = interpreter.get("Employee");
        jyDependentClass = interpreter.get("Dependent");
    }

    public EmployeeType createEmployee(String first, String last, String id) {
        PyObject employeeObj = jyEmployeeClass.__call__(
            new PyString(first),
            new PyString(last),
            new PyString(id));
        return (EmployeeType)employeeObj.__tojava__(EmployeeType.class);
    }

    public DependentType createDependent(String first, String last, int id) {
        PyObject dependentObj = jyDependentClass.__call__(
            new PyString(first),
            new PyString(last),
            new PyInteger(id));
        return (DependentType)dependentObj.__tojava__(DependentType.class);
    }

    private PyObject jyEmployeeClass;
    private PyObject jyDependentClass;
}

