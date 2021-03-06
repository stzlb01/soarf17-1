import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TwitterKeysManagerRemote extends Remote {

    TwitterKey requestKey(String requester) throws RemoteException;

    void releaseKey(int _id) throws RemoteException;
}
