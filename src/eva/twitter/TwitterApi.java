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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import biz.source_code.base64Coder.Base64Coder;

public class TwitterApi {
    
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
	
	public String read(String url, String httpMethod) {
	    
	    return read(url, httpMethod, consumerKey, consumerSecret);
	}
	
    public static String read(String url, String httpMethod, String consumerKey, String consumerSecret) {
        
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
            
            oAuthParameters.setParameter(ParameterEnum.OAUTH_CALLBACK, "oob");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_CONSUMER_KEY, consumerKey);
            oAuthParameters.setParameter(ParameterEnum.OAUTH_NONCE, String.valueOf(nonce));
            oAuthParameters.setParameter(ParameterEnum.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1");
            oAuthParameters.setParameter(ParameterEnum.OAUTH_TIMESTAMP, String.valueOf(seconds));
            oAuthParameters.setParameter(ParameterEnum.OAUTH_VERSION, "1.0");

            StringBuilder parameterString = new StringBuilder();

            for (ParameterEnum parameter : oAuthParameters.getParameters()) {

                String value = oAuthParameters.getParameter(parameter);

                if (value != null) {

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

            String signing_key = URLEncoder.encode(consumerSecret, "UTF-8") + "&";
            
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

                OutputStream output = connection.getOutputStream();
                output.write(bodyBuilder.toString().getBytes(charset));
            }
            
            try {
                
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                
                System.out.println("Response Code: " + httpConnection.getResponseCode());

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
	
	public TwitterClientSession createClientSession(String token, String tokenSecret) {
	    
	    return new TwitterClientSession(this, token, tokenSecret);
	}

	public static void main(String[] args) {

	    TwitterApi api = new TwitterApi(TwitterProps.instance().getConsumerKey(), TwitterProps.instance().getConsumerSecret());
	    
	    String value = api.read("https://api.twitter.com/oauth/request_token", "POST");
	    
	    System.out.println(value);	    
	}
}
