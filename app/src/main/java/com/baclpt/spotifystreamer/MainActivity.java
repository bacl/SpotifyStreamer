package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.baclpt.spotifystreamer.data.ArtistInfo;

import java.util.Locale;


public class MainActivity extends ActionBarActivity implements SearchArtistFragment.CallbackSearchArtist {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TOP_TRACKS_FRAGMENT_TAG = "TTFTAG";


    private String mCountry;
    private boolean mTwoPane;
    private TopTracksActivityFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTwoPane = getResources().getBoolean(R.bool.large_layout);

        if (!mTwoPane) getSupportActionBar().setElevation(0f);

        loadPreferences();
    }

    /**
     * Load Preferences
     */
    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String country = prefs.getString(getString(R.string.settings_country_key), null);

        // if doesn't exist sets the current country to device Locale
        // (appends on first time the user runs the app)
        if (country == null || country.isEmpty()) {
            country = Locale.getDefault().getCountry();
            if (country.equals("")) {
                country = getString(R.string.settings_country_default);
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(getString(R.string.settings_country_key), country);
            editor.apply();
        }
        mCountry = country;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update the search results if the preferred country changed
        String country = Utility.getPreferredCountry(this);
        if (country != null && !country.equals(mCountry)) {
            SearchArtistFragment frag = (SearchArtistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search_container);
            if (frag != null) {
                frag.doSearch();
            }
            mCountry = country;
        }
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
    public void onBackPressed() {
        stopService(new Intent(this, PlayerService.class));
        super.onBackPressed();
    }


    @Override
    public void onArtistSelected(ArtistInfo selectedArtistInfo) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (fragment == null) {
                fragment = new TopTracksActivityFragment();
                fragment.setArtistInfo(selectedArtistInfo);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_top_tracks_container, fragment, TOP_TRACKS_FRAGMENT_TAG)
                        .commit();
            } else {
                fragment.setArtistInfo(selectedArtistInfo);
                fragment.fetchArtistTracks();
            }


        } else {

            Intent intent = new Intent(this, TopTracksActivity.class);
            Bundle bundleArtistInfo = new Bundle();
            bundleArtistInfo.putParcelable(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO, selectedArtistInfo);
            intent.putExtra(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO, bundleArtistInfo);
            startActivity(intent);

        }
    }
}
