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
package eva.twitter.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import eva.twitter.api.ParameterEnum;
import eva.twitter.api.Tweet;
import eva.twitter.api.TweetList;
import eva.twitter.api.TwitterClientSession;
import eva.twitter.api.TwitterServiceException;

public class ViewTweetsActivity extends ListActivity {
    
    private TwitterClientSession clientSession;
    
    private TweetArrayAdapter adapter;
    
    private Tweet lastTweet = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        
        clientSession = (TwitterClientSession) extras.getSerializable("userSession");
        
        adapter = (new TweetArrayAdapter(this, new ArrayList<Tweet>()));
        
        setListAdapter(adapter);
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        try {

            TweetList myTweets = clientSession.getNewsFeed();

            for (Tweet tweet : myTweets.getTweets()) {

                adapter.add(tweet);
            }

            lastTweet = myTweets.getLastTweet();

            this.getListView().setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScroll(AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount) {
                        getNextTweets();
                    }
                }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // TODO Auto-generated method stub

                }
            });

        } catch (TwitterServiceException e) {
            
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Application Error");
            alertDialog.setMessage("There was problem communicating with Twitter");
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
    }

    private void getNextTweets() {

        try {

            TweetList nextTweets = clientSession.getNewsFeed(null, lastTweet.getId());

            for (Tweet tweet : nextTweets.getTweets()) {

                if (tweet.getId() != lastTweet.getId()) {

                    adapter.add(tweet);
                }
            }

            lastTweet = nextTweets.getLastTweet();

        } catch (TwitterServiceException e) {

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Application Error");
            alertDialog.setMessage("There was problem communicating with Twitter");
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_tweets, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_exit:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void logout() {
        
        SharedPreferences settings = getSharedPreferences("SimpleTwitterClient", 0);
        
        SharedPreferences.Editor editor = settings.edit();
        
        editor.remove(ParameterEnum.OAUTH_TOKEN.getKey());
        editor.remove(ParameterEnum.OAUTH_TOKEN_SECRET.getKey());
        
        editor.commit();
        
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    
    private void exit() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
