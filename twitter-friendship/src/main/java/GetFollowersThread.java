import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.bson.Document;
import twitter4j.*;

import java.util.logging.Logger;

/**
 * Created by meotm01 on 6/19/17.
 */
public class GetFollowersThread implements Runnable {

    private static Logger log = Logger.getLogger(GetFollowersThread.class.toString());

    private MongoDatabase soarf17;
    private Twitter twitter;
    private String screenName;

    GetFollowersThread(MongoDatabase soarf17, Twitter twitter, String screenName) {
        this.soarf17 = soarf17;
        this.twitter = twitter;
        this.screenName = screenName;
    }

    public void run() {
        // prepare basic user query
        MongoCollection users = this.soarf17.getCollection("users");
        String strQuery = "{\"screen_name\": \"" + this.screenName + "\"}";
        BasicDBObject queryObj = (BasicDBObject) JSON.parse(strQuery);

        // get nextFollowersCursor
        BasicDBObject findQuery = (BasicDBObject) JSON.parse("{\"screen_name\": \"" + this.screenName + "\"}");
        FindIterable<Document> result = users.find(findQuery);
        MongoCursor cursor = result.iterator();
        if (cursor.hasNext()) {
            Document doc = (Document) cursor.next();
            long nextFollowersCursor = doc.getLong("next_followers_cursor");

            // variables declaration
            PagableResponseList<User> usersPage;

            // loop until you get ALL of the followers
            while (nextFollowersCursor != 0) {
                try {
                    // get the next page of followers
                    log.info("-> getFollowersList called");
                    usersPage = twitter.getFollowersList(this.screenName, nextFollowersCursor);
                    log.info("-> updating followers in DB");
                    for (User user : usersPage) {
                        String strUpdate = "{" +
                                "$addToSet: {" +
                                "\"followers\": \"" + user.getScreenName().toLowerCase() + "\"}}";
                        BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                        users.updateOne(queryObj, updateObj);
                    }
                    nextFollowersCursor = usersPage.getNextCursor();

                    // update MongoDB with the new nextFollowersCursor
                    log.info("-> updating next_followers_cursor in DB");
                    String strUpdate = "{" +
                            "$set: {" +
                            "\"next_followers_cursor\": " + nextFollowersCursor + "}}";
                    BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                    users.updateOne(queryObj, updateObj);
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
    } // end of run()
}
