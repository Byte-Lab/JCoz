// EmployeeType.java

package jyinterface.interfaces;
import org.python.core.PyList;
import java.util.concurrent.*;


public interface Worker extends Callable<Void> {
    public Void call();
}

