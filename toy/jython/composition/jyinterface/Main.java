// Main.java

package jyinterface;

import jyinterface.factories.EmployeeFactory;
import jyinterface.interfaces.EmployeeType;
import jyinterface.interfaces.DependentType;
import org.python.core.PyObject;
import org.python.core.PyList;

public class Main {

    private static void printEmployee(EmployeeType employee) {
        System.out.println("Name: " + employee.getEmployeeFirst() + " "
                + employee.getEmployeeLast());
        System.out.println("Id: " + employee.getEmployeeId());
        PyList deplist = employee.getDependents();
        int count = deplist.__len__();
        System.out.println("count: " + count);
        for (int idx = 0; idx < count; idx++) {
            PyObject obj = deplist.__getitem__(idx);
            DependentType dep = (DependentType)obj.__tojava__(DependentType.class);
            printDependent(dep);
        }
    }

    private static void printDependent(DependentType dependent) {
        System.out.println("Dep Name: " + dependent.getDependentFirst() + " "
                + dependent.getDependentLast());
        System.out.println("Dep Id: " + dependent.getDependentId());
    }

    public static void main(String[] args) {
        EmployeeFactory factory = new EmployeeFactory();
        EmployeeType emp = factory.createEmployee("Josh", "Juneau", "1");
        printEmployee(emp);
        printEmployee(factory.createEmployee("Charlie", "Groves", "2"));
        System.out.println("------------------");
        DependentType dependent = factory.createDependent(
            "Dave", "Kuhlman", 4);
        printDependent(dependent);
        System.out.println("------------------");
        emp.addDependent(dependent);
        printEmployee(emp);
    }
}

