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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eva.twitter.api.ImageRetriever;
import eva.twitter.api.Tweet;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> implements ImageRetriever {
    
    private class SetImageTask  implements Runnable {
        
        private ImageView imageView;
        
        private Uri uri;
        
        public SetImageTask(ImageView imageView, Uri uri) {
            
            this.imageView = imageView;
            
            this.uri = uri;
        }

        @Override
        public void run() {
            
            imageView.setImageURI(uri);            
        }
        
    }
    
    private class DownloadAvatarTask extends AsyncTask<Void, Void, Void> {
        
        private ImageView imageView;
        
        private Tweet tweet;
        
        public DownloadAvatarTask(ImageView imageView, Tweet tweet) {
            
            this.imageView = imageView;
            
            this.tweet = tweet;
        }

        @Override
        protected Void doInBackground(Void... params) {

            String url = tweet.getUserAvatar(TweetArrayAdapter.this);
            
            Uri uri = Uri.fromFile(new File(url));
            
            activity.runOnUiThread(new SetImageTask(imageView, uri));
                        
            return (Void) null;
        }
    }
    
    private final Activity activity;
    
    public TweetArrayAdapter(Activity activity, List<Tweet> values) {
        super(activity, R.layout.tweet_view, values);
        this.activity = activity;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
        View rowView = inflater.inflate(R.layout.tweet_view, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.userAvatar);
        
        TextView textView = (TextView) rowView.findViewById(R.id.tweetText);
        
        TextView userTextView = (TextView) rowView.findViewById(R.id.user);
        
        Tweet tweet = getItem(position);
        
        textView.setText(tweet.getText());
        
        userTextView.setText(tweet.getScreenName());
        
        if(tweet.getUserAvatar() != null) {
            
            String url = tweet.getUserAvatar();
            
            Uri uri = Uri.fromFile(new File(url));
            
            activity.runOnUiThread(new SetImageTask(imageView, uri));
            
        } else {
        
            DownloadAvatarTask downloadTask = new DownloadAvatarTask(imageView, tweet);
            
            downloadTask.execute((Void[]) null);
        }
 
        return rowView;
    }

    @Override
    public String getImage(String urlString) {
        
        try {
            
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            
            File urlFile = new File(url.getFile());
            
            File outputFile = new File(activity.getCacheDir() + "/atc" + urlFile.getName());
            
            OutputStream output = new FileOutputStream(outputFile);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            
            return outputFile.getPath();
            
        } catch (MalformedURLException e) {

        } catch (IOException e) {
            Log.e("DownloadImage", "Caught an IOException while downloading an image", e);

        }
        
        return null;
    }

}
