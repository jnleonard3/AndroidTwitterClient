package eva.twitter.android;

import java.util.List;

import eva.twitter.api.Tweet;
import eva.twitter.api.TwitterClientSession;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewTweetsActivity extends Activity {
    
    private TwitterClientSession clientSession;
    
    private LinearLayout tweetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_tweets);
        
        Bundle extras = getIntent().getExtras();
        
        clientSession = (TwitterClientSession) extras.getSerializable("userSession");
        
        tweetList = (LinearLayout)findViewById(R.id.tweetList);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        List<Tweet> myTweets = clientSession.getMyTweets();
        
        for(Tweet tweet : myTweets) {
            
            TextView tweetView = new TextView(this);
            tweetView.setText(tweet.getText());
            
            tweetList.addView(tweetView);
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_tweets, menu);
        return true;
    }

}
