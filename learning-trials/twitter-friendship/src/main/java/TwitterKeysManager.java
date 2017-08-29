import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.util.JSON;
import org.bson.Document;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.logging.*;

public class TwitterKeysManager implements TwitterKeysManagerRemote {

    private static final Logger log = Logger.getLogger("soarf17");
    private static final String PROPERTIES_FILE_NAME = "config.properties";
    private Properties propty;
    private MongoClient mongoClient;
    private MongoDatabase soarf17;

    public TwitterKeysManager() throws FileNotFoundException, IOException {
        // get configuration properties
        this.propty = new Properties();
        InputStream propertiesStream = TwitterKeysManager.class.getClassLoader().getResourceAsStream(TwitterKeysManager.PROPERTIES_FILE_NAME);
        propty.load(propertiesStream);

        // connect to MongoDB
        this.connectMongoDB();
    }

    private void connectMongoDB() {
        log.info("in connectMongoDB()");

        this.mongoClient = new MongoClient(this.propty.getProperty("db_server"), Integer.parseInt(this.propty.getProperty("db_port")));
        this.soarf17 = this.mongoClient.getDatabase(this.propty.getProperty("db_name"));
    }

    public TwitterKey requestKey(String requester) throws RemoteException {
        MongoCollection twitterkeys = this.soarf17.getCollection("twitterkeys");
        BasicDBObject findQuery = (BasicDBObject) JSON.parse("{\"available\": true}");
        MongoCursor<Document> cursor =  twitterkeys.find(findQuery).iterator();
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            int _id = doc.getInteger("_id");
            String consumerKey = doc.getString("consumer_key");
            String consumerSecret = doc.getString("consumer_secret");
            String accessToken = doc.getString("access_token");
            String accessTokenSecret = doc.getString("access_token_secret");
            TwitterKey twitterKey = new TwitterKey(_id, consumerKey, consumerSecret, accessToken, accessTokenSecret);
            BasicDBObject updateQuery = (BasicDBObject) JSON.parse("{\"_id\": " + _id + "}");
            BasicDBObject update = (BasicDBObject) JSON.parse("{$set: {\"available\": false, \"requester\": \"" + requester + "\"}}");
            twitterkeys.updateOne(updateQuery, update);
            return twitterKey;
        }
        else
            return null;
    }

    public void releaseKey(int _id) throws RemoteException {
        MongoCollection twitterkeys = this.soarf17.getCollection("twitterkeys");
        BasicDBObject updateQuery = (BasicDBObject) JSON.parse("{\"_id\": " + _id + "}");
        BasicDBObject update = (BasicDBObject) JSON.parse("{$set: {\"available\": true, \"requester\": " + null + "}}");
        twitterkeys.updateOne(updateQuery, update);
    }

    public static void main(String[] args) throws Exception {
        TwitterKeysManager twitterKeysManager = new TwitterKeysManager();
        TwitterKeysManagerRemote twitterKeysManagerRemote = (TwitterKeysManagerRemote) UnicastRemoteObject.exportObject(twitterKeysManager, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("twitterkeys", twitterKeysManagerRemote);
        TwitterKeysManager.log.info("Ready for service!");
    }

}

/*

db.twitterkeys.insert({
'_id': NumberInt(0),
'consumer_key': 'ZK9tX0zK2o82tA65o6JpEnLQg',
'consumer_secret': 'YjeXfZUl2VAEEwUa22lA0eC5bYuiVjAR68cY2j4Xf78bXCsH01',
'access_token': '1529712390-9ap8L8UgMh1zZA9VfWT2JEECxRLC61i0a9RQ2bs',
'access_token_secret': 'wxIi3J952a6Dqc4vG4OCrk7gXXMndcoC4lrrF1ol2VtCN',
'requester': null,
'available': true})

db.twitterkeys.insert({
'_id': NumberInt(1),
'consumer_key': 'WqnhN2rWB34GwIaMxASqAtpGF',
'consumer_secret': 'whcYUZtds0m0MU1fBGX8ev5k9bptdw9wl4BasnaL8vsdGKQ6z1',
'access_token': '1529712390-kfCRrLNJpeC6j42K40soQbcaHyAmhPrsGvHdJNj',
'access_token_secret': 'Tp8vqch8vKOdB0mZaYWmi3fES3wnDpz2HInDEgQTcXdML',
'requester': null,
'available': true})

db.twitterkeys.insert({
'_id': NumberInt(2),
'consumer_key': 'BzyAEumfTpmGyMt8gUXgz1sRJ',
'consumer_secret': 'hLhUVEsYCtgigFGfzxEMcq8LslSf8uHYI1c3aXTwbnbSHFC3sE',
'access_token': '1529712390-zZ2HhzRjJZr2UnQUyN2TwUSPbDezF5bN4Ra1eVK',
'access_token_secret': 'UMrIYEmABbCk0HywqjwwuNaYcIfBlXn6zAmdt23jZN7GY',
'requester': null,
'available': true})

db.twitterkeys.insert({
'_id': NumberInt(3),
'consumer_key': 'yJkGRu5f7jFmIWHjwqn53SGo5',
'consumer_secret': '6VHropzHZmaydBNiP3VdGfSfIWlfekfTsDcj8kmVP6fvDVCnWd',
'access_token': '1529712390-5lRoGalVBsqC8RzBJaDWig6yRtjXR4jBogN8FE0',
'access_token_secret': 'MhoLk0wXHKxJW4g7kU3chkIhxO9Q6L2WTd8E1oWjgVirk',
'requester': null,
'available': true})

 */
