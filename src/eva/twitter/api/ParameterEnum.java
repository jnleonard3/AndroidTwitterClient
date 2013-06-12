package eva.twitter.api;

public class ParameterEnum implements Comparable<ParameterEnum> {

    public static final ParameterEnum OAUTH_CALLBACK = new ParameterEnum("oauth_callback", true);

    public static final ParameterEnum OAUTH_CONSUMER_KEY = new ParameterEnum("oauth_consumer_key", true);

    public static final ParameterEnum OAUTH_NONCE = new ParameterEnum("oauth_nonce", true);

    public static final ParameterEnum OAUTH_SIGNATURE = new ParameterEnum("oauth_signature", true);

    public static final ParameterEnum OAUTH_SIGNATURE_METHOD = new ParameterEnum("oauth_signature_method", true);

    public static final ParameterEnum OAUTH_TIMESTAMP = new ParameterEnum("oauth_timestamp", true);

    public static final ParameterEnum OAUTH_TOKEN = new ParameterEnum("oauth_token", true);

    public static final ParameterEnum OAUTH_TOKEN_SECRET = new ParameterEnum("oauth_token_secret", true);

    public static final ParameterEnum OAUTH_VERIFIER = new ParameterEnum("oauth_verifier");

    public static final ParameterEnum OAUTH_VERSION = new ParameterEnum("oauth_version", true);

    public static final ParameterEnum SCREEN_NAME = new ParameterEnum("screen_name");

    public static final ParameterEnum STATUS = new ParameterEnum("status");

    private static int nextId = 0;

    private String key;

    private boolean isOauth;

    private int id;

    private ParameterEnum(String key, boolean isOauth) {

        this.key = key;
        this.isOauth = isOauth;
        this.id = nextId;

        nextId += 1;
    }

    private ParameterEnum(String key) {
        this(key, false);
    }

    public String getKey() {

        return key;
    }

    public boolean isOauthKey() {

        return isOauth;
    }

    @Override
    public int hashCode() {

        return id;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {

            return true;
        }

        if (other instanceof ParameterEnum) {

            ParameterEnum otherEnum = (ParameterEnum) other;

            if (this.id == otherEnum.id) {

                return true;
            }

            return getKey().equals(otherEnum.getKey());
        }

        return false;
    }

    @Override
    public int compareTo(ParameterEnum other) {

        return this.id - other.id;
    }
}
