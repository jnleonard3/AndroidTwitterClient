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

public class ParameterEnum implements Comparable<ParameterEnum> {
    
    public static final ParameterEnum COUNT = new ParameterEnum("count");
    
    public static final ParameterEnum MAX_ID = new ParameterEnum("max_id");

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
