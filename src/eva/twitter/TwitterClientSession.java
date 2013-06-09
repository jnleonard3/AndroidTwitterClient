package eva.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import biz.source_code.base64Coder.Base64Coder;

public class TwitterClientSession {
    
    private TwitterApi api;
    
    private String token;
    
    private String tokenSecret;
    
    public TwitterClientSession(TwitterApi api, String token, String tokenSecret) {
        
        this.api = api;
        
        this.token = token;
        
        this.tokenSecret = tokenSecret;
    }
    
    public String authenticatedRead(String url, String httpMethod) {
        
        return authenticatedRead(url, httpMethod, null);
    }
    
    public String authenticatedRead(String url, String httpMethod, Parameters requestParameters) {
        
        return authenticatedRead(url, httpMethod, api.getConsumerKey(), api.getConsumerSecret(), token, tokenSecret, requestParameters);
    }

    private static String authenticatedRead(String url, String httpMethod, String consumerKey, String consumerSecret, String token, String tokenSecret, Parameters requestParameters) {
        
        StringBuffer buffer = new StringBuffer();
        try {
            /**
             * get the time - note: value below zero the millisecond value is
             * used for oauth_nonce later on
             */
            long seconds = System.currentTimeMillis() / 1000;
            long nonce = System.currentTimeMillis();

            /**
             * Listing of all parameters necessary to retrieve a token (sorted
             * lexicographically as demanded)
             */

            Parameters oAuthParameters = new Parameters();
            
            oAuthParameters.setParameter(ParameterEnum.OAUTH_CONSUMER_KEY, consumerKey);
            oAuthParameters.setParameter(ParameterEnum.OAUTH_NONCE, String.valueOf(nonce));
            oAuthParameters.setParameter(ParameterEnum.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_TIMESTAMP, String.valueOf(seconds));
            oAuthParameters.setParameter(ParameterEnum.OAUTH_TOKEN, token);
            oAuthParameters.setParameter(ParameterEnum.OAUTH_VERSION, "1.0");

            StringBuilder parameterString = new StringBuilder();
            
            Parameters mergedParameters = new Parameters();
            
            if(requestParameters != null) {
                
                mergedParameters.addAll(requestParameters);
            }
            
            mergedParameters.addAll(oAuthParameters);

            for (ParameterEnum parameter : mergedParameters.getParameters()) {

                String value = mergedParameters.getParameter(parameter);

                if (parameter != ParameterEnum.OAUTH_SIGNATURE && value != null) {

                    parameterString.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                    parameterString.append("=");
                    parameterString.append(URLEncoder.encode(value, "UTF-8").replace("+", "%20"));
                    parameterString.append("&");
                }
            }

            parameterString.deleteCharAt(parameterString.length() - 1);
            
            /**
             * Generation of the signature base string
             */
            String signature_base_string = httpMethod + "&" + URLEncoder.encode(url, "UTF-8") + "&" + URLEncoder.encode(parameterString.toString(), "UTF-8");

            String signingKey = URLEncoder.encode(consumerSecret, "UTF-8") + "&" + URLEncoder.encode(tokenSecret, "UTF-8");

            /**
             * Sign the request
             */
            Mac m = Mac.getInstance("HmacSHA1");
            m.init(new SecretKeySpec(signingKey.getBytes(), "HmacSHA1"));
            byte[] res = m.doFinal(signature_base_string.getBytes());
            String sig = URLEncoder.encode(String.valueOf(Base64Coder.encode(res)), "UTF-8");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_SIGNATURE, sig);

            /**
             * Create the header for the request
             */
            String header = "OAuth ";
            for (ParameterEnum parameter : oAuthParameters.getParameters()) {

                String value = oAuthParameters.getParameter(parameter);

                if (value != null) {

                    header += parameter.getKey() + "=\"" + value + "\", ";
                }
            }
            // cut off last appended comma
            header = header.substring(0, header.length() - 2);
            
            System.out.println(signature_base_string);

            String charset = "UTF-8";
            URLConnection connection = new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            connection.setRequestProperty("Authorization", header);
            
            if (httpMethod.equals("POST")) {
                
                StringBuilder bodyBuilder = new StringBuilder();
                
                if(requestParameters != null && !requestParameters.getParameters().isEmpty()) {
                
                    for(ParameterEnum parameter : requestParameters.getParameters()) {
                        
                        String value = requestParameters.getParameter(parameter);
                        
                        if (value != null) {
                            
                            bodyBuilder.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                            bodyBuilder.append("=");
                            bodyBuilder.append(URLEncoder.encode(value, "UTF-8").replace("+", "%20"));
                            bodyBuilder.append("&");
                        }
                    }
                    
                    bodyBuilder.deleteCharAt(bodyBuilder.length() - 1);
                }

                OutputStream output = connection.getOutputStream();
                output.write(bodyBuilder.toString().getBytes(charset));
            }

            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String read;
                while ((read = reader.readLine()) != null) {
                    buffer.append(read);
                }

            } catch (IOException e) {

                BufferedReader reader = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream()));

                System.out.println("Error:");
                String read;
                while ((read = reader.readLine()) != null) {
                    System.out.println(read);
                }

                return null;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }
    
    public static Tweet parseTweet(JSONObject json) {
        
        JSONObject userObj = (JSONObject) json.get("user");
        
        return new Tweet((String)userObj.get("screen_name"), (String)json.get("text"));
    }

    public List<Tweet> getMyTweets() {

        String tweetsJson = authenticatedRead("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET");

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
        
        String newsFeed = authenticatedRead("https://api.twitter.com/1.1/statuses/home_timeline.json", "GET");

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
        
        authenticatedRead("https://api.twitter.com/1.1/statuses/update.json", "POST", parameters);
    }

}
