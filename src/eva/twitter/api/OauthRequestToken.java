package eva.twitter.api;

public class OauthRequestToken {
    
    private String token;
    
    private String token_secret;
    
    public OauthRequestToken(String token, String token_secret) {
        
        this.token = token;
        this.token_secret = token_secret;
    }
    
    public String getToken() {
        
        return token;
    }
    
    public String getTokenSecret() {
        
        return token_secret;
    }

}
