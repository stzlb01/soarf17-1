/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student:
 * Description: obtain followers of a given account
 */

import com.mongodb.util.JSON;
import twitter4j.*;
import twitter4j.auth.*;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.*;

public class GetFollowers {

    private Twitter twitter;

    private MongoClient   mongoClient;
    private MongoDatabase soarf17;

    private String screenName;
    private long    followers_cursor;

    private static Logger log = Logger.getLogger(GetFollowers.class);

    GetFollowers(String args[]) {

        // command-line validation
        if (args.length == 0) {
            help();
            throw new IllegalArgumentException();
        }
        this.screenName = args[0];
        this.followers_cursor = -1;

        // MongoDB connection
        this.mongoClient = new MongoClient(Configuration.DB_SERVER, Configuration.DB_PORT);
        this.soarf17 = this.mongoClient.getDatabase(Configuration.DB_NAME);

        // TwitterAPI authentication
        this.twitter = TwitterFactory.getSingleton();
        this.twitter.setOAuthConsumer(Configuration.CONSUMER_KEY, Configuration.CONSUMER_SECRET);
        AccessToken accessToken = new AccessToken(Configuration.ACCESS_TOKEN_KEY, Configuration.ACCESS_TOKEN_SECRET);
        this.twitter.setOAuthAccessToken(accessToken);
    }

    void help() {
        System.out.println("Use: java " + GetFollowers.class + " <screen_name>\n");
    }

    void updateProfile() throws TwitterException {
        log.info("in updateProfile()");
        MongoCollection usersCollection = this.soarf17.getCollection("users");
        String strQuery = "{\"screen_name\": \"" + this.screenName + "\"}";
        BasicDBObject queryObj = (BasicDBObject) JSON.parse(strQuery);
        log.info("-> querying DB for user " + this.screenName);
        FindIterable<Document> result = usersCollection.find(queryObj);
        MongoCursor cursor = result.iterator();
        boolean firstTimeUser = true;
        if (cursor.hasNext()) {
            log.info("-> " + this.screenName + " was found!");
            Document doc = (Document) cursor.next();
            this.followers_cursor = (Long) doc.get("followers_cursor");
            firstTimeUser = false;
        }
        log.info("-> querying Twitter for user " + this.screenName);
        User user = this.twitter.showUser(this.screenName);
        if (firstTimeUser) {
            log.info("-> firstTimeUser: inserting new user into DB");
            String strInsert = "{" +
                    "\"_id\": " + user.getId() + ", " +
                    "\"screen_name\": \"" + user.getScreenName()  + "\", " +
                    "\"description\": \"" + user.getDescription() + "\", " +
                    "\"followers\": [], " +
                    "\"followers_cursor\": " + this.followers_cursor + "}";
            log.info(strInsert);
            Document doc = Document.parse(strInsert);
            usersCollection.insertOne(doc);
        }
        else {
            log.info("-> NOT firstTimeUser: updating key attributes...");
            /*String strUpdate = "{" +
                    "$set: {" +
                    "\"followers_cursor\": " + this.followers_cursor + "}}";
            log.info(strUpdate);
            BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
            usersCollection.updateOne(queryObj, updateObj);*/
        }
        log.info("done updateProfile()");
    }

    void run() throws TwitterException {
        log.info("in run()");
        updateProfile();

        MongoCollection usersCollection = this.soarf17.getCollection("users");
        String strQuery = "{\"screen_name\": \"" + this.screenName + "\"}";
        BasicDBObject queryObj = (BasicDBObject) JSON.parse(strQuery);

        PagableResponseList<User> users;
        while (true) {
            log.info("-> getFollowersList called");
            users = twitter.getFollowersList(this.screenName, this.followers_cursor);
            log.info("-> updating followers");
            for (User user : users) {
                String strUpdate = "{" +
                        "$addToSet: {" +
                        "\"followers\": \"" + user.getScreenName() + "\"}}";
                BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                usersCollection.updateOne(queryObj, updateObj);
            }
            this.followers_cursor = users.getNextCursor();
            log.info("-> new followers_cursor is " + this.followers_cursor);
            if (this.followers_cursor == 0) {
                log.info("-> now more followers so leaving run()");
                break;
            }
            // update MongoDB
            log.info("-> updating followers_cursor");
            String strUpdate = "{" +
                    "$set: {" +
                    "\"followers_cursor\": " + this.followers_cursor + "}}";
            BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
            usersCollection.updateOne(queryObj, updateObj);
            try {
                log.info("-> sleeping for 5s now...");
                Thread.sleep(5000);
            }
            catch (InterruptedException ex) {

            }
        }
    }

    public static void main(String[] args) {
        try {
            GetFollowers getFollowers = new GetFollowers(args);
            getFollowers.run();
        }
        catch (Exception ex) {
            System.out.println("Ops, something went wrong: " + ex);
        }
    }
}
