/*
 * SOAR Project Fall 2017
 * Advisor: Thyago Mota
 * Student:
 * Description: Simple tests to learn Twitter4J and Twitter's Search API
 */

import twitter4j.*;
import twitter4j.auth.*;

public class HelloTwitter {

    private Twitter twitter;

    HelloTwitter() {
        setCredentials();
    }

    void setCredentials() {
        this.twitter = TwitterFactory.getSingleton();
        this.twitter.setOAuthConsumer(Configuration.CONSUMER_KEY, Configuration.CONSUMER_SECRET);
        AccessToken accessToken = new AccessToken(Configuration.ACCESS_TOKEN_KEY, Configuration.ACCESS_TOKEN_SECRET);
        this.twitter.setOAuthAccessToken(accessToken);
    }

    void printHomeTimeLine() throws TwitterException {
        ResponseList<Status> statusResponseList = this.twitter.getHomeTimeline();
        for (Status status: statusResponseList)
            System.out.println(status.getText());
    }


    public static void main(String[] args) {
        try {
            HelloTwitter helloTwitter = new HelloTwitter();
            helloTwitter.printHomeTimeLine();
        }
        catch (Exception ex) {
            System.out.println("Ops, something went wrong: " + ex);
        }
    }
}