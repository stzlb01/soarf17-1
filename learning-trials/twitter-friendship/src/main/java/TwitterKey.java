import java.io.Serializable;

/**
 * Created by meotm01 on 6/16/17.
 */
public class TwitterKey implements Serializable {

    private int     _id;
    private String  consumerKey;
    private String  consumerSecret;
    private String  accessToken;
    private String  accessTokenSecret;

    public TwitterKey(int _id, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        this._id = _id;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

    public int getId() {
        return this._id;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    @Override
    public String toString() {
        String str = "_id: " + this.getId();
        str += "\nConsumer key: " + this.getConsumerKey();
        str += "\nConsumer secret: " + this.getConsumerSecret();
        str += "\nAccess token key: " + this.getAccessToken();
        str += "\nAccess token secret: " + this.getAccessTokenSecret();
        return str;
    }
}
