package eva.twitter;

public class ParameterEnum {
    
    public static final ParameterEnum OAUTH_CALLBACK = new ParameterEnum("oauth_callback");
	
	public static final ParameterEnum OAUTH_CONSUMER_KEY = new ParameterEnum("oauth_consumer_key");
	
	public static final ParameterEnum OAUTH_NONCE = new ParameterEnum("oauth_nonce");
	
	public static final ParameterEnum OAUTH_SIGNATURE = new ParameterEnum("oauth_signature");
	
	public static final ParameterEnum OAUTH_SIGNATURE_METHOD = new ParameterEnum("oauth_signature_method");
	
	public static final ParameterEnum OAUTH_TIMESTAMP = new ParameterEnum("oauth_timestamp");
	
	public static final ParameterEnum OAUTH_TOKEN = new ParameterEnum("oauth_token");
	
	public static final ParameterEnum OAUTH_VERSION = new ParameterEnum("oauth_version");
	
	public static final ParameterEnum STATUS = new ParameterEnum("status");
	
	private static int nextId = 0;
	
	private String key;
	
	private int id;
	
	private ParameterEnum(String key) {
		
		this.key = key;
		this.id = nextId;
		
		nextId += 1;
	}
	
	public String getKey() {
		
		return key;
	}
	
	@Override
	public int hashCode() {
		
		return id;
	}

}
