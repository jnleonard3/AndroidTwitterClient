package eva.twitter.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import eva.twitter.api.OauthRequestToken;
import eva.twitter.api.ParameterEnum;
import eva.twitter.api.TwitterApi;
import eva.twitter.api.TwitterClientSession;
import eva.twitter.api.TwitterProps;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
            
            if(!viewingWebpage) {
            
                setContentView(R.layout.activity_splash_screen);
                
                statusText = (TextView) findViewById(R.id.statusText);
            }
            
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

            if (!viewingWebpage) {

                viewingWebpage = true;

                setContentView(R.layout.activity_login);

                webView = (WebView) findViewById(R.id.webview);
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
    }
    
    private class TwitterWebViewClient extends WebViewClient {
        
        private OauthRequestToken token;
        
        public TwitterWebViewClient(OauthRequestToken token) {
            
            this.token = token;
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            try {

                URL urlObj = new URL(url);

                String host = urlObj.getHost();

                if (!host.equals("api.twitter.com")) {

                    String verifier = twitterApi.parseAuthorizationCallback(url);

                    if (verifier != null) {

                        TwitterGenerateTokenTask loginTask = new TwitterGenerateTokenTask(token);

                        loginTask.execute(verifier);

                    } else {
                        displayErrorDialog(3);
                    }
                }

            } catch (MalformedURLException e) {

                displayErrorDialog(3);
            }
        }
    }

    private TwitterApi twitterApi = null;
    
    private TextView statusText;
    
    private WebView webView;
    
    private boolean viewingWebpage = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState != null) {
            
            onRestoreInstanceState(savedInstanceState);
        }
        
        if(!TwitterProps.exists()) {
            
            setContentView(R.layout.activity_splash_screen);
            
            statusText = (TextView) findViewById(R.id.statusText);

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
        
        SharedPreferences settings = getSharedPreferences("SimpleTwitterClient", 0);
        
        String token = settings.getString(ParameterEnum.OAUTH_TOKEN.getKey(), null);
        
        String tokenSecret = settings.getString(ParameterEnum.OAUTH_TOKEN_SECRET.getKey(), null);
        
        if(token == null || tokenSecret == null) {
        
            TwitterLoginTask loginTask = new TwitterLoginTask();
            
            loginTask.execute((Void[]) null);
            
        } else {
            
            twitterApi = new TwitterApi(TwitterProps.instance().getConsumerKey(), TwitterProps.instance().getConsumerSecret());
            
            TwitterClientSession clientSession = twitterApi.createClientSession(token, tokenSecret);
            
            startUserSession(clientSession);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if(webView != null) {
            
            webView.saveState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        
        if(webView != null) {
            
            webView.restoreState(savedInstanceState);
        }
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
        
        if(viewingWebpage) {
            
            viewingWebpage = false;
            
            updateStatus("Getting Access Token!");
        
            TwitterClientSession clientSession = twitterApi.getAccessToken(token, verifier);
            
            startUserSession(clientSession);
        }
        
    }
    
    private void startUserSession(TwitterClientSession clientSession) {
        
        SharedPreferences settings = getSharedPreferences("SimpleTwitterClient", 0);
        
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString(ParameterEnum.OAUTH_TOKEN.getKey(), clientSession.getToken());
        editor.putString(ParameterEnum.OAUTH_TOKEN_SECRET.getKey(), clientSession.getTokenSecret());
        
        editor.commit();
        
        updateStatus("Success!");
        
        Intent intent = new Intent(this, ViewTweetsActivity.class);
        Bundle twitterSessionBundle = new Bundle();
        twitterSessionBundle.putSerializable("userSession", clientSession);
        intent.putExtras(twitterSessionBundle);
        startActivity(intent);
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
