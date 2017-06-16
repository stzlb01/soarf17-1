import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StudentRemote extends Remote {

    int getId() throws RemoteException;

    String getName() throws RemoteException;
}
