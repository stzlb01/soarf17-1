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
public class GetFriendsThread implements Runnable {

    private static Logger log = Logger.getLogger(GetFriendsThread.class.toString());

    private MongoDatabase soarf17;
    private Twitter twitter;
    private String screenName;

    GetFriendsThread(MongoDatabase soarf17, Twitter twitter, String screenName) {
        this.soarf17 = soarf17;
        this.twitter = twitter;
        this.screenName = screenName;
    }

    public void run() {
        // prepare basic user query
        MongoCollection users = this.soarf17.getCollection("users");
        String strQuery = "{\"screen_name\": \"" + this.screenName + "\"}";
        BasicDBObject queryObj = (BasicDBObject) JSON.parse(strQuery);

        // get nextFriendsCursor
        BasicDBObject findQuery = (BasicDBObject) JSON.parse("{\"screen_name\": \"" + this.screenName + "\"}");
        FindIterable<Document> result = users.find(findQuery);
        MongoCursor cursor = result.iterator();
        if (cursor.hasNext()) {
            Document doc = (Document) cursor.next();
            long nextFriendsCursor = doc.getLong("next_friends_cursor");

            // variables declaration
            PagableResponseList<User> usersPage;

            // loop until you get ALL of the friends
            while (nextFriendsCursor != 0) {
                try {
                    // get the next page of friends
                    log.info("-> getFriendsList called");
                    usersPage = twitter.getFriendsList(this.screenName, nextFriendsCursor);
                    log.info("-> updating friends in DB");
                    for (User user : usersPage) {
                        String strUpdate = "{" +
                                "$addToSet: {" +
                                "\"friends\": \"" + user.getScreenName().toLowerCase() + "\"}}";
                        BasicDBObject updateObj = (BasicDBObject) JSON.parse(strUpdate);
                        users.updateOne(queryObj, updateObj);
                    }
                    nextFriendsCursor = usersPage.getNextCursor();

                    // update MongoDB with the new nextFriendsCursor
                    log.info("-> updating next_friends_cursor in DB");
                    String strUpdate = "{" +
                            "$set: {" +
                            "\"next_friends_cursor\": " + nextFriendsCursor + "}}";
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
