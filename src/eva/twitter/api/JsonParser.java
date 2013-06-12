package eva.twitter.api;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonParser {

    private JsonParser() {
    }

    public static List<Tweet> parseTweetList(String json) {
        List<Tweet> tweets = new ArrayList<Tweet>();

        JSONArray array = (JSONArray) JSONValue.parse(json);

        for (int i = 0; i < array.size(); i += 1) {

            JSONObject tweetJsonObj = (JSONObject) array.get(i);

            tweets.add(parseTweet(tweetJsonObj));
        }

        return tweets;
    }

    private static Tweet parseTweet(JSONObject json) {

        JSONObject userObj = (JSONObject) json.get("user");

        return new Tweet((String) userObj.get("screen_name"), (String) json.get("text"));
    }

}
