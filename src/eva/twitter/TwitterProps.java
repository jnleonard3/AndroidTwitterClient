package eva.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class to read properties from twitterkeys.properties
 */
public class TwitterProps {

    private static final class SingletonHolder {
        static final TwitterProps instance = new TwitterProps();
    }

    /**
     * Get an instance of the properties object
     * 
     * @return The singleton instance of the TwitterProps object.
     */
    public static TwitterProps instance() {
        return SingletonHolder.instance;
    }

    private final Properties properties;

    private TwitterProps() {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("twitterkeys.properties");
        if (stream == null) {
            throw new RuntimeException(("Failed to find 'twitterkeys.properties'"));
        }
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
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
