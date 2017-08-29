import java.rmi.Remote;
import java.rmi.RemoteException;

public class Student implements StudentRemote {

    private int id;
    private String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() throws RemoteException {
        return id;
    }

    public String getName() throws RemoteException {
        return name;
    }
}
