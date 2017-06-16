/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zachary Balga
 * Description: obtain followers of a given account
 */

import com.mongodb.util.JSON;
import twitter4j.*;
import twitter4j.auth.*;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.*;

public class GetFollowers {
    // implements RateLimitStatusListener

    private Twitter twitter;

    private MongoClient   mongoClient;
    private MongoDatabase soarf17;

    private String screenName;
    private long   nextFollowersCursor;

    private static Logger log = Logger.getLogger(GetFollowers.class);

    GetFollowers(String args[]) {
        // command-line validation
        if (args.length == 0) {
            help();
            throw new IllegalArgumentException();
        }
        this.screenName = args[0].toLowerCase();
        this.nextFollowersCursor = -1;
    }

    private void connectMongoDB() {
        log.info("in connectMongoDB()");

        this.mongoClient = new MongoClient(Configuration.DB_SERVER, Configuration.DB_PORT);
        this.soarf17 = this.mongoClient.getDatabase(Configuration.DB_NAME);
    }

    private void connectTwitter() {
        log.info("in connectTwitter()");

        this.twitter = TwitterFactory.getSingleton();
        this.twitter.setOAuthConsumer(Configuration.CONSUMER_KEY, Configuration.CONSUMER_SECRET);
        AccessToken accessToken = new AccessToken(Configuration.ACCESS_TOKEN_KEY, Configuration.ACCESS_TOKEN_SECRET);
        this.twitter.setOAuthAccessToken(accessToken);
        // this.twitter.addRateLimitStatusListener(this);
    }

    void help() {
        System.out.println("Use: java " + GetFollowers.class + " <screen_name>\n");
    }

    private void updateProfile() throws TwitterException {
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
            this.nextFollowersCursor = doc.getLong("next_followers_cursor");
            firstTimeUser = false;
        }
        log.info("-> querying Twitter for user " + this.screenName);
        User user = this.twitter.showUser(this.screenName);
        if (firstTimeUser) {
            log.info("-> firstTimeUser: inserting new user into DB");
            String strInsert = "{" +
                    "\"_id\": " + user.getId() + ", " +
                    "\"screen_name\": \"" + this.screenName  + "\", " +
                    "\"description\": \"" + user.getDescription() + "\", " +
                    "\"location\": \"" + user.getLocation() + "\", " +
                    "\"protected\": " + user.isProtected() + ", " +
                    "\"followers\": [], " +
                    "\"next_followers_cursor\": " + this.nextFollowersCursor + "}";
            //log.info(strInsert);
            Document doc = Document.parse(strInsert);
            usersCollection.insertOne(doc);
        }
        else {
            log.info("-> NOT firstTimeUser: updating key attributes in DB");
            String strUpdate = "{" +
                    "$set: {" +
                    "\"description\": \"" + user.getDescription() + "\", " +
                    "\"location\": \"" + user.getLocation() + "\", " +
                    "\"protected\": " + user.isProtected() + "}}";
            //log.info(strUpdate);
            BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
            usersCollection.updateOne(queryObj, updateObj);
        }
        log.info("updateProfile() is done");
    }

    void run() throws TwitterException {
        log.info("in run()");
        connectMongoDB();
        connectTwitter();
        updateProfile();

        // prepare basic user query
        MongoCollection usersCollection = this.soarf17.getCollection("users");
        String strQuery = "{\"screen_name\": \"" + this.screenName + "\"}";
        BasicDBObject queryObj = (BasicDBObject) JSON.parse(strQuery);

        // variables declaration
        PagableResponseList<User> users;

        // loop until you get ALL of the followers
        while (this.nextFollowersCursor != 0) {
            try {
                // get the next page of followers
                log.info("-> getFollowersList called");
                users = twitter.getFollowersList(this.screenName, this.nextFollowersCursor);
                log.info("-> updating followers in DB");
                for (User user : users) {
                    String strUpdate = "{" +
                            "$addToSet: {" +
                            "\"followers\": \"" + user.getScreenName().toLowerCase() + "\"}}";
                    BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                    usersCollection.updateOne(queryObj, updateObj);
                }
                this.nextFollowersCursor = users.getNextCursor();

                // update MongoDB with the new nextFollowersCursor
                log.info("-> updating next_followers_cursor in DB");
                String strUpdate = "{" +
                        "$set: {" +
                        "\"next_followers_cursor\": " + this.nextFollowersCursor + "}}";
                BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                usersCollection.updateOne(queryObj, updateObj);
            }
            catch (TwitterException twitterEx) {
                if (twitterEx.exceededRateLimitation()) {
                    RateLimitStatus rateLimitStatus = twitterEx.getRateLimitStatus();
                    int secondsUntilReset = rateLimitStatus.getSecondsUntilReset();
                    log.info("-> Rate limit exceeded: " + secondsUntilReset + "s until reset...");
                    try {
                        Thread.sleep((secondsUntilReset + 5) * 1000);
                    }
                    catch (InterruptedException interruptedEx) {

                    }
                }
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