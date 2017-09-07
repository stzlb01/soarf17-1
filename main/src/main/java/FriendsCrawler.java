/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student: Zachary Balga
 * Description: a crawler script that will implement a BFS (Breadth First Search) algorithm to sample the friendship network of a given Twitter user up to a pre-defined friendship distance
 */
// change

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class FriendsCrawler {

    private Twitter twitter;
    private String screenName;
    private MongoClient mongoClient;
    private MongoDatabase soarf17;


    FriendsCrawler(String[] args) throws TwitterException {
        if (args[0] != null) {
            screenName = args[0];
        }
        else {
            screenName = twitter.getScreenName();
        }

        this.mongoClient = new MongoClient(Configuration.DB_SERVER, Configuration.DB_PORT);
        this.soarf17 = this.mongoClient.getDatabase(Configuration.DB_NAME);

        assignKey();

    }

    private void assignKey() {
        String CONSUMER_KEY;
        String CONSUMER_SECRET;
        String ACCESS_TOKEN_KEY;
        String ACCESS_TOKEN_SECRET;
        MongoCollection keys = this.soarf17.getCollection("twitterKeyset");
        MongoCursor<Document> cursor = keys.find().iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            boolean availability = doc.getBoolean("available");
            if (availability) {
                CONSUMER_KEY = doc.getString("consumerKey");
                CONSUMER_SECRET = doc.getString("consumerSecret");
                ACCESS_TOKEN_KEY = doc.getString("accessTokenKey");
                ACCESS_TOKEN_SECRET = doc.getString("accessTokenSecret");

                //testing
                System.out.println(CONSUMER_KEY);
                System.out.println(CONSUMER_SECRET);
                System.out.println(ACCESS_TOKEN_KEY);
                System.out.println(ACCESS_TOKEN_SECRET);

                //twitter credentials
                this.twitter = TwitterFactory.getSingleton();
                this.twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                AccessToken accessToken = new AccessToken(ACCESS_TOKEN_KEY, ACCESS_TOKEN_SECRET);
                this.twitter.setOAuthAccessToken(accessToken);

                break;
            }
        }
    }

    public static void main(String args[]) throws TwitterException {
        FriendsCrawler friendsCrawler = new FriendsCrawler(args);
        System.out.println("It works!");
    }
}
