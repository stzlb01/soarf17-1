import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by meotm01 on 6/16/17.
 */
public class TwitterKeysClient {

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry();
        TwitterKeysManagerRemote twitterKeysManagerRemote = (TwitterKeysManagerRemote) registry.lookup("twitterkeys");
<<<<<<< HEAD
        //TwitterKey twitterKey = twitterKeysManagerRemote.requestKey(TwitterKeysClient.class.toString());
        //System.out.println(twitterKey);
        twitterKeysManagerRemote.releaseKey(1);
=======
        TwitterKey twitterKey = twitterKeysManagerRemote.getAvailableKey();
        System.out.println(twitterKey);
        //twitterKeysManagerRemote.releaseKey(3);
>>>>>>> d127f7448115507c84822cb18722bce5b2a1a82a
    }
}
