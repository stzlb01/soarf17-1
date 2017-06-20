import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by meotm01 on 6/16/17.
 */
public class TwitterKeysClient {

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry();
        TwitterKeysManagerRemote twitterKeysManagerRemote = (TwitterKeysManagerRemote) registry.lookup("twitterkeys");
        TwitterKey twitterKey = twitterKeysManagerRemote.getAvailableKey();
        System.out.println(twitterKey);
        //twitterKeysManagerRemote.releaseKey(3);
    }
}
