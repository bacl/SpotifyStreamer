package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baclpt.spotifystreamer.adapters.TopTrackLvAdapter;
import com.baclpt.spotifystreamer.data.ArtistInfo;
import com.baclpt.spotifystreamer.data.TrackInfo;
import com.baclpt.spotifystreamer.tasks.TaskFetchTopTracks;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends BaseFragment {
    private static final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();
    private static final String CURRENT_TOP_TRACKS_KEY = "current_top_tracks_list";


    private TopTrackLvAdapter lvAdapter;
    private ArrayList<TrackInfo> tracksList;

    // flag to determine if there is info stored
    private boolean hasTracksInfoSaved;

    private ArtistInfo artistInfo;

    private TaskFetchTopTracks task;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        isBindingToPlayerServiceEnabled = true;


        Intent intent = getActivity().getIntent();
        Bundle bundleExtra = intent.getBundleExtra(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO);
        if (bundleExtra != null) {
            artistInfo = bundleExtra.getParcelable(TopTracksActivity.EXTRA_SPOTIFY_ARTIST_INFO);
        }


        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_TOP_TRACKS_KEY)) {
            tracksList = savedInstanceState.getParcelableArrayList(CURRENT_TOP_TRACKS_KEY);
            hasTracksInfoSaved = true;
        } else {
            hasTracksInfoSaved = false;
            tracksList = new ArrayList<>();
        }

        lvAdapter = new TopTrackLvAdapter(getActivity(), R.layout.list_item_track, tracksList);

        // Get a reference to the ListView, and attach the adapter to it.
        ListView resultsListView = (ListView) rootView.findViewById(R.id.results_listView);
        resultsListView.setAdapter(lvAdapter);
        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                sendPlayListToPlayerService();

                playerService.setCurrentPlayingTrackIndex(position);
                playerService.startPrepareCurrentTrack();

                showPlayerActivity();

            }
        });
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (tracksList != null && tracksList.isEmpty()) {
            fetchArtistTracks();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tracksList != null) {
            outState.putParcelableArrayList(CURRENT_TOP_TRACKS_KEY, tracksList);
        }
    }


    public void setArtistInfo(ArtistInfo newArtistInfo) {
        this.artistInfo = newArtistInfo;
    }

    public void fetchArtistTracks() {
        if (!hasTracksInfoSaved) {
            fetchArtistTracksNow();
        }
    }
    public void fetchArtistTracksNow() {
            // if first time the fragment is being displayed, fetch tracks data
            task = new TaskFetchTopTracks(this, lvAdapter);

            if (this.artistInfo != null) {
                task.execute(this.artistInfo.getSpotifyId(), this.artistInfo.getName());
            } else {
                lvAdapter.clear();
            }
    }
    private void sendPlayListToPlayerService() {
        if (playerService != null && tracksList != null && artistInfo != null) {
            if (playerService.getPlayList() == null || !getPlaylistID().equals(playerService.getPlayListID())) {
                playerService.setPlayList(tracksList, getPlaylistID());
                Log.d(LOG_TAG, "  sendPlayListToPlayerService ");
            }
        } else {
            Log.d(LOG_TAG, "not sendPlayListToPlayerService");
        }
    }

    private String getPlaylistID() {
        return artistInfo.getSpotifyId();
    }

}
