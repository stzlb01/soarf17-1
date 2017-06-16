import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Properties;
import java.util.logging.Logger;

import static com.sun.jndi.ldap.LdapCtx.DEFAULT_PORT;

/**
 * Created by meotm01 on 6/16/17.
 */

public class TwitterKeysManager {

    private static final Logger log = Logger.getLogger("soarf17");
    private static final String PROPERTIES_FILE_NAME = "config.properties";
    private Properties properties;
    private MongoClient mongoClient;
    private MongoDatabase soarf17;

    public TwitterKeysManager() throws FileNotFoundException, IOException {
        properties = new Properties();
        InputStream propertiesStream = TwitterKeysManager.class.getClassLoader().getResourceAsStream(TwitterKeysManager.PROPERTIES_FILE_NAME);
        properties.load(propertiesStream);

        this.connectMongoDB();
    }

    private void connectMongoDB() {
        log.info("in connectMongoDB()");

        this.mongoClient = new MongoClient(this.properties.getProperty("db_server"), Integer.parseInt(this.properties.getProperty("db_port")));
        this.soarf17 = this.mongoClient.getDatabase(this.properties.getProperty("db_name"));
    }

    public void run() throws UnknownHostException, SocketException {
        log.info("in run()");

        InetAddress serverAddress = InetAddress.getLocalHost();
        int serverPort = Integer.parseInt(this.properties.getProperty("twitter_keys_upd_port"));
        DatagramSocket socket = new DatagramSocket(serverPort, serverAddress);
        log.info("-> UDP server socket created!")

        DatagramPacket packet = new DatagramPacket(new byte[PAYLOAD_SIZE], PAYLOAD_SIZE);
        while (true) {

        }

    }


    public static void main(String[] args) throws Exception {
        TwitterKeysManager twitterKeysManager = new TwitterKeysManager();
    }

}


/*

db.twitterkeys.insert({
'_id': 0,
'consumer_key': 'ZK9tX0zK2o82tA65o6JpEnLQg',
'consumer_secret': 'YjeXfZUl2VAEEwUa22lA0eC5bYuiVjAR68cY2j4Xf78bXCsH01',
'access_token': '1529712390-9ap8L8UgMh1zZA9VfWT2JEECxRLC61i0a9RQ2bs',
'access_token_secret': 'wxIi3J952a6Dqc4vG4OCrk7gXXMndcoC4lrrF1ol2VtCN',
'available': true})

db.twitterkeys.insert({
'_id': 1,
'consumer_key': 'WqnhN2rWB34GwIaMxASqAtpGF',
'consumer_secret': 'whcYUZtds0m0MU1fBGX8ev5k9bptdw9wl4BasnaL8vsdGKQ6z1',
'access_token': '1529712390-kfCRrLNJpeC6j42K40soQbcaHyAmhPrsGvHdJNj',
'access_token_secret': 'Tp8vqch8vKOdB0mZaYWmi3fES3wnDpz2HInDEgQTcXdML',
'available': true})

db.twitterkeys.insert({
'_id': 2,
'consumer_key': 'BzyAEumfTpmGyMt8gUXgz1sRJ',
'consumer_secret': 'hLhUVEsYCtgigFGfzxEMcq8LslSf8uHYI1c3aXTwbnbSHFC3sE',
'access_token': '1529712390-zZ2HhzRjJZr2UnQUyN2TwUSPbDezF5bN4Ra1eVK',
'access_token_secret': 'UMrIYEmABbCk0HywqjwwuNaYcIfBlXn6zAmdt23jZN7GY',
'available': true})

db.twitterkeys.insert({
'_id': 3,
'consumer_key': 'yJkGRu5f7jFmIWHjwqn53SGo5',
'consumer_secret': '6VHropzHZmaydBNiP3VdGfSfIWlfekfTsDcj8kmVP6fvDVCnWd',
'access_token': '1529712390-5lRoGalVBsqC8RzBJaDWig6yRtjXR4jBogN8FE0',
'access_token_secret': 'MhoLk0wXHKxJW4g7kU3chkIhxO9Q6L2WTd8E1oWjgVirk',
'available': true})

 */
