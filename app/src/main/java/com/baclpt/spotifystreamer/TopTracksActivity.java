package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class TopTracksActivity extends ActionBarActivity {
    public final static String EXTRA_SPOTIFY_ID = "com.baclpt.spotifystreamer.SPOTIFY_ID";
    public final static String EXTRA_ARTIST_Name = "com.baclpt.spotifystreamer.ARTIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // get artist name and set it as sub title on the action bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = getIntent();
            String artistName = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_Name);
            if (artistName != null) {
                ActionBar ab = getSupportActionBar();
                ab.setSubtitle(artistName);
            }
        }

    }

    @Override
    public Intent getParentActivityIntent() {
        // UP/HOME button fix
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
