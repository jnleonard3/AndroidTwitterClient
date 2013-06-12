package eva.twitter.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to read properties from twitterkeys.properties
 */
public class TwitterProps {
    
    static TwitterProps instance = null;

    /**
     * Get an instance of the properties object
     * 
     * @return The singleton instance of the TwitterProps object.
     */
    public static TwitterProps instance() {
        
        if(instance == null) {
            
            try {
            
                instance = new TwitterProps();
            
            } catch(Exception e) {
                
                throw new RuntimeException("Caught exception getting properties instance", e);
            }
        }
        
        return instance;
    }
    
    public static TwitterProps initialize(InputStream stream) throws Exception {
        
        if(instance != null) {
            
            throw new RuntimeException("Cannot initialize, already been initialized");
        }
        
        instance = new TwitterProps(stream);
        
        return instance;
    }
    
    public static boolean exists() {
        
        return instance != null;
    }

    private final Properties properties;

    private TwitterProps() throws Exception {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("twitterkeys.properties");
        if (stream == null) {
            throw new RuntimeException(("Failed to find 'twitterkeys.properties'"));
        }
        loadStream(stream);
    }
    
    private TwitterProps(InputStream stream) throws Exception {
        properties = new Properties();
        
        loadStream(stream);
    }
    
    private void loadStream(InputStream stream) throws Exception {

        try {

            properties.load(stream);

        } catch (IOException e) {

            throw new RuntimeException("Failed to load properties", e);
        }
        
        String key = getConsumerKey();

        if (key == null || key.length() == 0) {

            throw new IllegalArgumentException("The key cannot be null or empty");
        }
        
        String secret = getConsumerSecret();

        if (secret == null || secret.length() == 0) {

            throw new IllegalArgumentException("The secret cannot be null or empty");
        }
    }

    public String getConsumerKey() {
        return properties.getProperty("key");
    }

    public String getConsumerSecret() {
        return properties.getProperty("secret");
    }

    public static void main(String[] args) {
        TwitterProps props = TwitterProps.instance();

        System.out.println("The Values: ");
        System.out.println("Key: " + props.getConsumerKey());
        System.out.println("Secret: " + props.getConsumerSecret());
    }
}
