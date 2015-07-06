package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baclpt.spotifystreamer.data.ArtistInfo;


public class TopTracksActivity extends ActionBarActivity {
    public final static String EXTRA_SPOTIFY_ARTIST_INFO = "com.baclpt.spotifystreamer.SPOTIFY_ARTIST_INFO";

    private String mCountry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // get artist name and set it as sub title on the action bar
        Bundle bundleExtra = getIntent().getBundleExtra(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO);
        if (bundleExtra != null) {
            ArtistInfo artistInfo = bundleExtra.getParcelable(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO);
            if (artistInfo != null) {
                ActionBar ab = getSupportActionBar();
                if (ab != null) ab.setSubtitle(artistInfo.getName());
            }
        }
        mCountry=Utility.getPreferredCountry(this);

    }

    @Override
    public Intent getParentActivityIntent() {
        // UP/HOME button fix
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update the top tracks results if the preferred country changed
        String country = Utility.getPreferredCountry(this);
        if (country != null && !country.equals(mCountry)) {
            TopTracksActivityFragment frag = (TopTracksActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_top_tracks_container);
            if (frag != null) {
                frag.fetchArtistTracksNow();
            }
        }
    }
}
