import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {

    public static void main(String[] args) throws Exception {
        Student student = new Student(1, "John");
        StudentRemote studentRemote = (StudentRemote) UnicastRemoteObject.exportObject(student, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.bind("student", studentRemote);
    }
}
