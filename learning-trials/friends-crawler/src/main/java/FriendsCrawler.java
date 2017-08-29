/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zachary Balga
 * Description: obtain all (true) friends of a given account
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

public class FriendsCrawler {

    private String root;
    private int n;
    private Properties prop;
    private MongoClient mongoClient;
    private MongoDatabase soarf17;
    private Twitter twitter;
    private TwitterKeys twitterKeys;

    private static Logger log = Logger.getLogger(FriendsCrawler.class.getName());


    void help() {
        System.out.println("Use: java " + FriendsCrawler.class + " <root> <n>, where:");
        System.out.println("<root> is the \"root\" screen name and");
        System.out.println("<n> is the number of friends levels to dig starting from the root user.\n");
    }

    public FriendsCrawler(String[] args) throws IllegalArgumentException, NumberFormatException, IOException {
        // command-line validation
        if (args.length != 2) {
            help();
            throw new IllegalArgumentException();
        }
        this.root = args[0].toLowerCase();
        this.n = Integer.parseInt(args[1]);

        // load properties
        this.prop = new Properties();
        this.prop.load(FriendsCrawler.class.getResourceAsStream("/config.properties"));
    }

    private void connectToMongoDB() {
        String dbServer = this.prop.getProperty("db_server");
        int dbPort = Integer.parseInt(this.prop.getProperty("db_port"));
        this.mongoClient = new MongoClient(dbServer, dbPort);
        String dbName = this.prop.getProperty("db_name");
        this.soarf17 = this.mongoClient.getDatabase(dbName);
        log.info("Connected to MongoDB");
    }

    private void obtainTwitterKeys() {
        MongoCollection twitterkeysCollection = this.soarf17.getCollection("twitterkeys");
        BasicDBObject query = BasicDBObject.parse("{ \"available\": true }");

        while (true) {
            // query for available twitter key
            MongoCursor<Document> cursor = twitterkeysCollection.find(query).iterator();

            // if found...
            if (cursor.hasNext()) {
                // get the information about the key and...
                Document doc = cursor.next();
                int _id = doc.getInteger("_id");
                String consumerKey = doc.getString("consumer_key");
                String consumerSecret = doc.getString("consumer_secret");
                String accessToken = doc.getString("access_token");
                String accessTokenSecret = doc.getString("access_token_secret");

                // reserve it!
                BasicDBObject updateQuery = BasicDBObject.parse(" { \"_id\": " + _id + " }");
                BasicDBObject updateOperation = BasicDBObject.parse(" { $set: {\"available\": false }}");
                UpdateResult result = twitterkeysCollection.updateOne(updateQuery, updateOperation);

                // make sure the operation was successful
                if (result.getMatchedCount() == 1 && result.getModifiedCount() == 1) {
                    this.twitterKeys = new TwitterKeys(consumerKey, consumerSecret, accessToken, accessTokenSecret);
                    log.info("Twitter credentials obtained");
                    return;
                }
            }

            // if not found (or something went wrong), put the thread to sleep for a while and try again after it
            int sleeptime = Integer.parseInt(this.prop.getProperty("obtain_twitterkeys_sleeptime"));
            try {
                log.info("No Twitter credentials available, sleeping for " + sleeptime + "s...");
                Thread.sleep(sleeptime * 1000);
            }
            catch (InterruptedException ex) {

            }
        }
    }


    private void connectToTwitter() {
        this.twitter = TwitterFactory.getSingleton();
        obtainTwitterKeys();
        this.twitter.setOAuthConsumer(this.twitterKeys.getConsumerKey(), this.twitterKeys.getConsumerSecret());
        AccessToken accessToken = new AccessToken(this.twitterKeys.getAccessToken(), this.twitterKeys.getAccessTokenSecret());
        this.twitter.setOAuthAccessToken(accessToken);
        log.info("Connected to Twitter");

    }

    public void run() {
        this.connectToMongoDB();
        this.connectToTwitter();

        MongoCollection usersCollection = this.soarf17.getCollection("users");
        BasicDBObject query, updateQuery, updateOperation;
        MongoCursor<Document> cursor;
        Document doc;
        String screenName;
        int level = 0;

        while (level <= this.n) {
            // search for a task to complete
            query = BasicDBObject.parse("{ \"root\": \"" + this.root + "\", \"level\": " + level + ", \"status\": 0 }");
            cursor =  usersCollection.find(query).iterator();

            // if found task...
            if (cursor.hasNext()) {
                doc = cursor.next();
                screenName = doc.getString("screen_name");
                updateQuery = BasicDBObject.parse("{ \"root\": \"" + this.root + "\", \"level\": " + level + ", \"status\": 0 }");

            }

        }
    }

    public static void main(String[] args) throws IOException {
        FriendsCrawler crawler = new FriendsCrawler(args);
        crawler.run();
    }
}
