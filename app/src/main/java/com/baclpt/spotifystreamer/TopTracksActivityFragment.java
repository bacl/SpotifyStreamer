package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baclpt.spotifystreamer.adapters.TopTrackLvAdapter;
import com.baclpt.spotifystreamer.data.TrackInfo;
import com.baclpt.spotifystreamer.tasks.TaskFetchTopTracks;
import com.baclpt.spotifystreamer.tasks.TaskSearchArtist;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends BaseFragment {

    private static final String CURRENT_TOP_TRACKS_KEY = "current_top_tracks";

    private TopTrackLvAdapter lvAdapter;
    private ArrayList<TrackInfo> trackInfo;

    // flag to determine if there is info stored
    private boolean hasTracksInfoSaved;

    private String artistId;
    private String artistName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Intent intent = getActivity().getIntent();
        artistId = intent.getStringExtra(TopTracksActivity.EXTRA_SPOTIFY_ID);
        artistName = intent.getStringExtra(TopTracksActivity.EXTRA_ARTIST_Name);


        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_TOP_TRACKS_KEY)) {
            trackInfo = savedInstanceState.getParcelableArrayList(CURRENT_TOP_TRACKS_KEY);
            hasTracksInfoSaved = true;
        } else {
            trackInfo = new ArrayList<TrackInfo>();
        }

        lvAdapter = new TopTrackLvAdapter(getActivity(), R.layout.list_item_track, trackInfo);

        // Get a reference to the ListView, and attach the adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.results_listView);
        listView.setAdapter(lvAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });


        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (trackInfo != null) {
            outState.putParcelableArrayList(CURRENT_TOP_TRACKS_KEY, trackInfo);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!hasTracksInfoSaved) {
            // if first time the fragment is being displayed, fetch tracks data
            TaskFetchTopTracks task = new TaskFetchTopTracks(this, lvAdapter);
            task.execute(artistId, artistName);
        }
    }
}
