// EmployeeType.java

package jyinterface.interfaces;
import org.python.core.PyList;
import jyinterface.interfaces.DependentType;


public interface EmployeeType {
    public String getEmployeeFirst();
    public String getEmployeeLast();
    public String getEmployeeId();
    public PyList getDependents();
    public void addDependent(DependentType dependent);
}

