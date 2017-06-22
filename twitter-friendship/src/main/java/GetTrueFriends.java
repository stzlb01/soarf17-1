/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zachary Balga
 * Description: obtain true friends of a given account
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.AuthorizationConfiguration;
import twitter4j.auth.OAuthSupport;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.Logger;

public class GetTrueFriends {

    private static final String PROPERTIES_FILE_NAME = "config.properties";
    private static Logger log = Logger.getLogger(GetTrueFriends.class.toString());
    private final Properties propty;
    private final String screenName;
    private MongoClient mongoClient;
    private MongoDatabase soarf17;

    GetTrueFriends(String args[]) throws Exception {

        // command-line validation
        if (args.length == 0) {
            help();
            throw new IllegalArgumentException();
        }
        this.screenName = args[0].toLowerCase();

        // get configuration properties
        this.propty = new Properties();
        InputStream propertiesStream = TwitterKeysManager.class.getClassLoader().getResourceAsStream(GetTrueFriends.PROPERTIES_FILE_NAME);
        propty.load(propertiesStream);

        // get Twitter keys
        Registry registry = LocateRegistry.getRegistry();
        TwitterKeysManagerRemote twitterKeysManagerRemote = (TwitterKeysManagerRemote) registry.lookup("twitterkeys");
        TwitterKey key1 = twitterKeysManagerRemote.requestKey(GetTrueFriends.class.toString() + " - getFollowers");
        if (key1 == null) {
            throw new Exception("Twitter key not avaiable!");
        }
        Twitter twitter1 = connectTwitter(key1);
        TwitterKey key2 = twitterKeysManagerRemote.requestKey(GetTrueFriends.class.toString() + " - getFriends");
        if (key2 == null) {
            throw new Exception("Twitter key not avaiable!");
        }
        Twitter twitter2 = connectTwitter(key2);
        System.out.println("twitter1: " + (Object) twitter1);
        System.out.println("twitter2: " + (Object) twitter2);

        // connect to mongodb
        connectMongoDB();

        // update profile
        updateProfile(twitter1);

        // start threads
        new Thread(new GetFollowersThread(this.soarf17, twitter1, this.screenName)).start();
        new Thread(new GetFriendsThread(this.soarf17, twitter2, this.screenName)).start();

        // release keys
        twitterKeysManagerRemote.releaseKey(key1.getId());
        twitterKeysManagerRemote.releaseKey(key2.getId());
    }

    void help() {
        System.out.println("Use: java " + GetTrueFriends.class + " <screen_name>\n");
    }

    private void connectMongoDB() {
        log.info("in connectMongoDB()");

        this.mongoClient = new MongoClient(this.propty.getProperty("db_server"), Integer.parseInt(this.propty.getProperty("db_port")));
        this.soarf17 = this.mongoClient.getDatabase(this.propty.getProperty("db_name"));
    }

    private Twitter connectTwitter(TwitterKey twitterKey) {
        log.info("in connectTwitter(TwitterKey)");

        TwitterFactory tf = new TwitterFactory();
        Twitter twitter = tf.getInstance();
        twitter.setOAuthConsumer(twitterKey.getConsumerKey(), twitterKey.getConsumerSecret());
        twitter.setOAuthAccessToken(new AccessToken(twitterKey.getAccessToken(), twitterKey.getAccessTokenSecret()));
        return twitter; 
    }

    private void updateProfile(Twitter twitter) throws TwitterException {
        log.info("in updateProfile()");

        // query Twitter for latest info about the user
        User user = twitter.showUser(this.screenName);

        // create profile (if it is a new user) or update info (if it is an old user)
        MongoCollection users = this.soarf17.getCollection("users");
        BasicDBObject findQuery = (BasicDBObject) JSON.parse("{\"screen_name\": \"" + this.screenName + "\"}");
        log.info("-> querying DB for user " + this.screenName);
        FindIterable<Document> result = users.find(findQuery);
        MongoCursor cursor = result.iterator();

        // just update info
        if (cursor.hasNext()) {
            log.info("-> NOT firstTimeUser: updating key attributes in DB");
            String strUpdate = "{" +
                    "$set: {" +
                    "\"description\": \"" + user.getDescription() + "\", " +
                    "\"location\": \"" + user.getLocation() + "\", " +
                    "\"protected\": " + user.isProtected() + "}}";
            BasicDBObject update = (BasicDBObject) JSON.parse(strUpdate);
            users.updateOne(findQuery, update);
        }
        // new user
        else {
            log.info("-> firstTimeUser: inserting new user into DB");
            String strInsert = "{" +
                    "\"_id\": " + user.getId() + ", " +
                    "\"screen_name\": \"" + this.screenName  + "\", " +
                    "\"description\": \"" + user.getDescription() + "\", " +
                    "\"location\": \"" + user.getLocation() + "\", " +
                    "\"protected\": " + user.isProtected() + ", " +
                    "\"followers\": [], " +
                    "\"next_followers_cursor\": NumberLong(-1), " +
                    "\"friends\": [], " +
                    "\"next_friends_cursor\": NumberLong(-1), " +
                    "\"true_friends\": [] }";
            //log.info(strInsert);
            Document doc = Document.parse(strInsert);
            users.insertOne(doc);
        }
        log.info("updateProfile() is done");
    }

    public static void main(String[] args) throws Exception {
        new GetTrueFriends(args);
    }
}
