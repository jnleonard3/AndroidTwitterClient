/*
 * Copyright (c) 2013, Jon Leonard
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */
package eva.twitter.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonParser {

    private final static String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    private static SimpleDateFormat generateDateParser() {
        
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        
        return sf;
    }

    public static TweetList parseTweetList(String json) throws TwitterException {
        
        List<Tweet> tweets = new ArrayList<Tweet>();

        Object obj = JSONValue.parse(json);
        
        if(obj instanceof JSONArray) {
            
            JSONArray array = (JSONArray) obj;
        
            SimpleDateFormat dateParser = generateDateParser();
            
            for (int i = 0; i < array.size(); i += 1) {
    
                try {
    
                    JSONObject tweetJsonObj = (JSONObject) array.get(i);
    
                    tweets.add(parseTweet(dateParser, tweetJsonObj));
    
                } catch (ParseException e) {
                    
                    throw new RuntimeException("Caught a parse exception while creating a tweet", e);
                }
            }

        } else if (obj instanceof JSONObject) {

            JSONObject jsonObj = (JSONObject) obj;

            JSONArray errorsArray = (JSONArray) jsonObj.get("errors");

            if (errorsArray != null) {

                throw new TwitterException(new String[] {});

            } else {

                throw new RuntimeException("Unknown JSON Object: " + json);
            }

        } else {

            throw new RuntimeException("Unknown JSON Object: " + json);
        }

        return new TweetList(tweets);
    }

    private static Tweet parseTweet(SimpleDateFormat formatter, JSONObject json) throws ParseException {

        JSONObject userObj = (JSONObject) json.get("user");

        String createdAt = (String) json.get("created_at");
        
        Date createdAtDate = formatter.parse(createdAt);

        return new Tweet((Long)json.get("id"), createdAtDate, (String) userObj.get("profile_image_url"), (String) userObj.get("screen_name"), (String) json.get("text"));
    }

    private JsonParser() {
    }

}
