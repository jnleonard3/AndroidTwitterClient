package eva.twitter.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import eva.twitter.api.OauthRequestToken;
import eva.twitter.api.TwitterApi;
import eva.twitter.api.TwitterClientSession;
import eva.twitter.api.TwitterProps;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class LoginActivity extends Activity {
    
    private class TwitterLoginTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            
            createTwitterApi();
            
            return (Void) null;
        }
    }
    
    private class TwitterGenerateTokenTask extends AsyncTask<String, Void, Void> {
        
        private OauthRequestToken token;
        
        public TwitterGenerateTokenTask(OauthRequestToken token) {
            
            this.token = token;
        }

        @Override
        protected Void doInBackground(String... params) {
            
            if(params.length == 0) {
                
                throw new IllegalArgumentException("Requires at least one argument");
            }
            
            getOauthToken(token, params[0]);
            
            return (Void) null;
        }
    }
    
    private class UpdateStatusText implements Runnable {
        
        String newStatusText;
        
        public UpdateStatusText(String newStatusText) {
            
            this.newStatusText = newStatusText;
        }

        @Override
        public void run() {
            
            statusText.setText(newStatusText);            
        }
    }
    
    private class DisplayTwitterTask implements Runnable {
        
        String url;
        
        private OauthRequestToken token;
        
        public DisplayTwitterTask(String url, OauthRequestToken token) {
            
            this.url = url;
            this.token = token;
        }

        @Override
        public void run() {

            setContentView(R.layout.activity_login);
            
            WebView webView = (WebView) findViewById(R.id.webview);
            webView.loadUrl(url);
            TwitterWebViewClient webViewClient = new TwitterWebViewClient(token);
            webView.setWebViewClient(webViewClient);
            webView.requestFocus(View.FOCUS_DOWN);
            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                            break;
                    }
                    return false;
                }
            });
            
            
        }        
    }
    
    private class TwitterWebViewClient extends WebViewClient {
        
        private OauthRequestToken token;
        
        public TwitterWebViewClient(OauthRequestToken token) {
            
            this.token = token;
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            
            try {
                
                URL urlObj = new URL(url);
                
                String host = urlObj.getHost();
                
                boolean equals = host.equals("api.twitter.com");
                
                if(equals) {
                    
                    return false;
                }
                
                String verifier = twitterApi.parseAuthorizationCallback(url);
                
                if(verifier == null) {
                    
                    displayErrorDialog(2);
                }
                
                TwitterGenerateTokenTask loginTask = new TwitterGenerateTokenTask(token);
                
                loginTask.execute(verifier);
                
                return true;
                
            } catch (MalformedURLException e) {
                // Ignore for now
            }
            
            displayErrorDialog(3);
            
            return true;
        }
    }

    private TwitterApi twitterApi = null;
    
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        statusText = (TextView) findViewById(R.id.statusText);
        
        if(!TwitterProps.exists()) {

            InputStream properties;
            
            try {
                
                updateStatus("Loading Properties");
                
                properties = getAssets().open("twitterkeys.properties");
    
                try {
    
                    TwitterProps.initialize(properties);
    
                } catch (Exception e) {
                    
                    Log.e("TwitterLogin", "Caught exception authorizing properties", e);
    
                    displayErrorDialog(1);
                }
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        TwitterLoginTask loginTask = new TwitterLoginTask();
        
        loginTask.execute((Void[]) null);
    }
    
    private void createTwitterApi() {
                
        twitterApi = new TwitterApi(TwitterProps.instance().getConsumerKey(), TwitterProps.instance().getConsumerSecret());
        
        updateStatus("Getting Request Token");
        
        OauthRequestToken token = twitterApi.getRequestToken("http://localhost/sign_in_with_twitter");
        
        updateStatus("Authenticating");
        
        String authUrl = twitterApi.getAuthorizationUrl(token);
        
        displayTwitterWebpage(authUrl, token);
    }
    
    private void getOauthToken(OauthRequestToken token, String verifier) {
        
        TwitterClientSession clientSession = twitterApi.getAccessToken(token, verifier);
        
        updateStatus("Success!");
        
    }
    
    private void updateStatus(String status) {
        
        UpdateStatusText runnable = new UpdateStatusText(status);
        
        this.runOnUiThread(runnable);
    }
    
    private void displayTwitterWebpage(String authorizationUrl, OauthRequestToken token) {
        
        DisplayTwitterTask runnable = new DisplayTwitterTask(authorizationUrl, token);
        
        this.runOnUiThread(runnable);
    }
    
    private void displayErrorDialog(int errorCode) {
        
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Application Error");
        alertDialog.setMessage("There was problem get Twitter authorization (Error code: " + errorCode + ")");
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

}
