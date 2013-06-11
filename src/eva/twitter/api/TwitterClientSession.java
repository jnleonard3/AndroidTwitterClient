package eva.twitter.api;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TwitterClientSession {
    
    private TwitterApi api;
    
    private String token;
    
    private String tokenSecret;
    
    public TwitterClientSession(TwitterApi api, String token, String tokenSecret) {
        
        this.api = api;
        
        this.token = token;
        
        this.tokenSecret = tokenSecret;
    }
    
    public String read(String url, String httpMethod) {
        
        return read(url, httpMethod, null);
    }
    
    public String read(String url, String httpMethod, Parameters requestParameters) {
        
        return api.read(url, httpMethod, token, tokenSecret, requestParameters);
    }
    
    public static Tweet parseTweet(JSONObject json) {
        
        JSONObject userObj = (JSONObject) json.get("user");
        
        return new Tweet((String)userObj.get("screen_name"), (String)json.get("text"));
    }

    public List<Tweet> getMyTweets() {

        String tweetsJson = read("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET");

        if (tweetsJson != null) {

            List<Tweet> tweets = new ArrayList<Tweet>();

            JSONArray array = (JSONArray) JSONValue.parse(tweetsJson);

            for (int i = 0; i < array.size(); i += 1) {

                JSONObject tweetJsonObj = (JSONObject) array.get(i);

                tweets.add(parseTweet(tweetJsonObj));
            }

            return tweets;
            
        } else {
            
            return null;
        }
    }

    public List<Tweet> getNewsFeed() {
        
        String newsFeed = read("https://api.twitter.com/1.1/statuses/home_timeline.json", "GET");

        if (newsFeed != null) {

            List<Tweet> tweets = new ArrayList<Tweet>();

            JSONArray array = (JSONArray) JSONValue.parse(newsFeed);

            for (int i = 0; i < array.size(); i += 1) {

                JSONObject tweetJsonObj = (JSONObject) array.get(i);

                tweets.add(parseTweet(tweetJsonObj));
            }

            return tweets;
            
        } else {
            
            return null;
        }
    }
    
    public void postTweet(String text) {
        
        Parameters parameters = new Parameters();
        parameters.setParameter(ParameterEnum.STATUS, text);
        
        read("https://api.twitter.com/1.1/statuses/update.json", "POST", parameters);
    }
}
