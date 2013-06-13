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

import java.io.Serializable;

public class TwitterClientSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private TwitterApi api;
    
    private String token;
    
    private String tokenSecret;
    
    public TwitterClientSession(TwitterApi api, String token, String tokenSecret) {
        
        this.api = api;
        
        this.token = token;
        
        this.tokenSecret = tokenSecret;
    }
    
    public String getToken() {
        
        return token;
    }
    
    public String getTokenSecret() {
        
        return tokenSecret;
    }
    
    public String read(String url, String httpMethod) {
        
        return read(url, httpMethod, null);
    }
    
    public String read(String url, String httpMethod, Parameters requestParameters) {
        
        return api.read(url, httpMethod, token, tokenSecret, requestParameters);
    }
    
    public TweetList getMyTweets() throws TwitterServiceException {

        String tweetsJson = read("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET");

        if (tweetsJson != null) {
            
            try {

                return JsonParser.parseTweetList(tweetsJson);
            
            } catch (TwitterException e) {
                
                throw new TwitterServiceException(e);
            }
            
        } else {
            
            return null;
        }
    }

    public TweetList getNewsFeed(Integer count, Long maxId) throws TwitterServiceException {

        Parameters parameters = new Parameters();

        if (count != null) {

            parameters.setParameter(ParameterEnum.COUNT, Integer.toString(count + 1));
        }

        if (maxId != null) {

            parameters.setParameter(ParameterEnum.MAX_ID, Long.toString(maxId));
        }

        String newsFeed = read("https://api.twitter.com/1.1/statuses/home_timeline.json", "GET", parameters);

        if (newsFeed != null) {
            
            try {

                return JsonParser.parseTweetList(newsFeed);

            } catch (TwitterException e) {

                throw new TwitterServiceException(e);
            }

        } else {

            return null;
        }
    }

    public TweetList getNewsFeed() throws TwitterServiceException {

        return getNewsFeed(null, null);
    }

    public TweetList getUserTweets(String userName) throws TwitterServiceException {

        Parameters parameters = new Parameters();

        if (userName != null) {

            parameters.setParameter(ParameterEnum.SCREEN_NAME, userName);
        }

        String tweetsJson = read("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET", parameters);

        if (tweetsJson != null) {

            try {

                return JsonParser.parseTweetList(tweetsJson);

            } catch (TwitterException e) {

                throw new TwitterServiceException(e);
            }

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
