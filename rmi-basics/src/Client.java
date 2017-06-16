import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry();
        StudentRemote student = (StudentRemote) registry.lookup("student");
        System.out.println(student.getId() + ":" + student.getName());
    }
}
