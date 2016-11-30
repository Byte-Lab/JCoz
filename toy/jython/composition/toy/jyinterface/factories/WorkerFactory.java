// EmployeeFactory.java

package jyinterface.factories;

import jyinterface.interfaces.Worker;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyInteger;
import org.python.util.PythonInterpreter;

public class WorkerFactory {

    public WorkerFactory() {
        String cmd = "from Worker import Worker\n";
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec(cmd);
        jyWorkerClass = interpreter.get("Worker");
    }

    public Worker createWorker(String id) {
        PyObject workerObj = jyWorkerClass.__call__(new PyString(id));
        return (Worker)workerObj.__tojava__(Worker.class);
    }

    private PyObject jyWorkerClass;
}

