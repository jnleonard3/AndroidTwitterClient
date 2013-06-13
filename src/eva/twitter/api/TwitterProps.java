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
