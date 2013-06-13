package eva.twitter.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import biz.source_code.base64Coder.Base64Coder;

public class TwitterApi implements Serializable {
    
    private String consumerKey;
    
    private String consumerSecret;

	public TwitterApi(String consumerKey, String consumerSecret) {

	    this.consumerKey = consumerKey;
	    this.consumerSecret = consumerSecret;
	}
	
	public String getConsumerKey() {
	    
	    return consumerKey;
	}
	
	public String getConsumerSecret() {
	    
        return consumerSecret;
    }

    public String read(String url, String httpMethod, Parameters requestParameters) {

        return read(url, httpMethod, consumerKey, consumerSecret, null, null, requestParameters);
    }

    public String read(String url, String httpMethod, String token, Parameters requestParameters) {

        return read(url, httpMethod, consumerKey, consumerSecret, token, null, requestParameters);
    }
    
    public String read(String url, String httpMethod, String token, String token_secret, Parameters requestParameters) {

        return read(url, httpMethod, consumerKey, consumerSecret, token, token_secret, requestParameters);
    }

    public static String read(String url, String httpMethod, String consumerKey, String consumerSecret, String token, String tokenSecret, Parameters requestParameters) {

        StringBuffer buffer = new StringBuffer();

        String header;

        try {
            /**
             * get the time - note: value below zero the millisecond value is used for oauth_nonce later on
             */
            long seconds = System.currentTimeMillis() / 1000;
            long nonce = System.currentTimeMillis();

            /**
             * Listing of all parameters necessary to retrieve a token (sorted lexicographically as demanded)
             */
            Parameters oAuthParameters = new Parameters();

            oAuthParameters.setParameter(ParameterEnum.OAUTH_CONSUMER_KEY, consumerKey);
            oAuthParameters.setParameter(ParameterEnum.OAUTH_NONCE, String.valueOf(nonce));
            oAuthParameters.setParameter(ParameterEnum.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_TIMESTAMP, String.valueOf(seconds));
            if (token != null) {

                oAuthParameters.setParameter(ParameterEnum.OAUTH_TOKEN, token);
            }
            oAuthParameters.setParameter(ParameterEnum.OAUTH_VERSION, "1.0");

            Parameters mergedParameters = new Parameters();

            if (requestParameters != null) {

                for (ParameterEnum parameter : requestParameters.getOauthParameterKeys()) {

                    oAuthParameters.setParameter(parameter, requestParameters.getParameter(parameter));
                }

                mergedParameters.addAll(requestParameters);
            }

            mergedParameters.addAll(oAuthParameters);

            StringBuilder parameterString = new StringBuilder();

            for (ParameterEnum parameter : mergedParameters.getParameters()) {

                String value = mergedParameters.getParameter(parameter);

                if (value != null) {

                    parameterString.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                    parameterString.append("=");
                    parameterString.append(URLEncoder.encode(value, "UTF-8").replace("+", "%20"));
                    parameterString.append("&");
                }
            }

            if (parameterString.length() > 0) {

                parameterString.deleteCharAt(parameterString.length() - 1);
            }

            /**
             * Generation of the signature base string
             */
            String signature_base_string = httpMethod + "&" + URLEncoder.encode(url, "UTF-8") + "&" + URLEncoder.encode(parameterString.toString(), "UTF-8");

            String signing_key = URLEncoder.encode(consumerSecret, "UTF-8") + "&";

            if (tokenSecret != null) {

                signing_key = signing_key + URLEncoder.encode(tokenSecret, "UTF-8");
            }

            /**
             * Sign the request
             */
            Mac m = Mac.getInstance("HmacSHA1");
            m.init(new SecretKeySpec(signing_key.getBytes(), "HmacSHA1"));
            byte[] res = m.doFinal(signature_base_string.getBytes());
            String sig = URLEncoder.encode(String.valueOf(Base64Coder.encode(res)), "UTF-8");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_SIGNATURE, sig);

            /**
             * Create the header for the request
             */
            header = "OAuth ";
            for (ParameterEnum parameter : oAuthParameters.getParameters()) {

                String value = oAuthParameters.getParameter(parameter);

                if (value != null) {

                    header += parameter.getKey() + "=\"" + value + "\", ";
                }
            }
            // cut off last appended comma
            header = header.substring(0, header.length() - 2);

            System.out.println(signature_base_string);

        } catch (Exception e) {

            throw new RuntimeException("Caught exception while creating a HTTP Request", e);
        }
        
        URLConnection connection = null;

        try {

            String charset = "UTF-8";
            connection = new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            connection.setRequestProperty("Authorization", header);

            if (httpMethod.equals("POST")) {

                StringBuilder bodyBuilder = new StringBuilder();

                if (requestParameters != null && !requestParameters.getParameters().isEmpty()) {

                    for (ParameterEnum parameter : requestParameters.getParameters()) {

                        if (!parameter.isOauthKey()) {

                            String value = requestParameters.getParameter(parameter);

                            if (value != null) {

                                bodyBuilder.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
                                bodyBuilder.append("=");
                                bodyBuilder.append(URLEncoder.encode(value, "UTF-8").replace("+", "%20"));
                                bodyBuilder.append("&");
                            }
                        }
                    }

                    if (bodyBuilder.length() > 0) {

                        bodyBuilder.deleteCharAt(bodyBuilder.length() - 1);
                    }
                }

                OutputStream output = connection.getOutputStream();
                output.write(bodyBuilder.toString().getBytes(charset));
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String read;
            while ((read = reader.readLine()) != null) {
                buffer.append(read);
            }

        } catch (Exception e) {
            
            StringBuilder connectionError = new StringBuilder();
            
            if(connection != null) {
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream()));
                
                try {

                    String read;
                    while ((read = reader.readLine()) != null) {
                        connectionError.append(read);
                    }
                    
                } catch (IOException ioE) {
                    // Ignore this for now
                }                
            }

            throw new RuntimeException("Caught exception while making a HTTP Request (" + connectionError + ")" , e);
        }

        return buffer.toString();
    }
    
    public OauthRequestToken getRequestToken(String callback_url) {
        
        Parameters requestTokenParameters = new Parameters();
        
        requestTokenParameters.setParameter(ParameterEnum.OAUTH_CALLBACK, callback_url);
        
        String value = read("https://api.twitter.com/oauth/request_token", "POST", requestTokenParameters);
        
        if(value == null || value.length() == 0) {
            
            throw new IllegalArgumentException("Request token cannot be null or empty");
        }
        
        String[] propertiesArray = value.split("&");
        
        Map<String, String> properties = new HashMap<String, String>();
        
        for(String property : propertiesArray) {
            
            String[] propertyEntry = property.split("=");
            
            if(propertyEntry.length == 2) {
            
                properties.put(propertyEntry[0], propertyEntry[1]);
                
            } else {
                
                throw new IllegalArgumentException("Could not parse property: " + property + "(" + value + ")");
            }
        }
        
        String token = properties.get(ParameterEnum.OAUTH_TOKEN.getKey());
        
        String token_secret = properties.get(ParameterEnum.OAUTH_TOKEN_SECRET.getKey());
        
        return new OauthRequestToken(token, token_secret);
    }
    
    public String getAuthorizationUrl(OauthRequestToken requestToken) {
        
        return "https://api.twitter.com/oauth/authenticate?oauth_token=" + requestToken.getToken();
    }
    
    public String parseAuthorizationCallback(String urlString) {
        
        try {
            
            URL url = new URL(urlString);
            
            String query = url.getQuery();
            
            String[] propertiesArray = query.split("&");
            
            Map<String, String> properties = new HashMap<String, String>();
            
            for(String property : propertiesArray) {
                
                String[] propertyEntry = property.split("=");
                
                properties.put(propertyEntry[0], propertyEntry[1]);
            }
            
            String verifier = properties.get(ParameterEnum.OAUTH_VERIFIER.getKey());
            
            return verifier;
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public TwitterClientSession getAccessToken(OauthRequestToken requestToken, String verifier) {
        
        Parameters requestTokenParameters = new Parameters();
        
        requestTokenParameters.setParameter(ParameterEnum.OAUTH_VERIFIER, verifier);
        
        String value = read("https://api.twitter.com/oauth/access_token", "POST", requestToken.getToken(), requestTokenParameters);
        
        String[] propertiesArray = value.split("&");
        
        Map<String, String> properties = new HashMap<String, String>();
        
        for(String property : propertiesArray) {
            
            String[] propertyEntry = property.split("=");
            
            properties.put(propertyEntry[0], propertyEntry[1]);
        }
        
        String token = properties.get(ParameterEnum.OAUTH_TOKEN.getKey());
        
        String token_secret = properties.get(ParameterEnum.OAUTH_TOKEN_SECRET.getKey());
        
        return createClientSession(token, token_secret);
    }
	
	public TwitterClientSession createClientSession(String token, String tokenSecret) {
	    
	    return new TwitterClientSession(this, token, tokenSecret);
	}
	
	public static void main(String[] args) {

	    TwitterApi api = new TwitterApi(TwitterProps.instance().getConsumerKey(), TwitterProps.instance().getConsumerSecret());
	    
	    OauthRequestToken value = api.getRequestToken("http://localhost/sign_in_with_twitter");
	    
	    String url = api.getAuthorizationUrl(value);
	    
	    System.out.println(url);

	    System.out.println("Enter callback URL value");
	    Scanner keyboard = new Scanner(System.in);
	    String input = keyboard.nextLine();
	    
	    String verifier = api.parseAuthorizationCallback(input);
	    
	    System.out.println(verifier);
	    
	    TwitterClientSession clientSession = api.getAccessToken(value, verifier);
	    
	    System.out.println(clientSession.getMyTweets().size());
	}
}
