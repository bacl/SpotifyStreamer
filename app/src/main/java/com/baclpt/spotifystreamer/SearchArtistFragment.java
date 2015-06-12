package com.baclpt.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baclpt.spotifystreamer.adapters.ArtistLvAdapter;
import com.baclpt.spotifystreamer.data.ArtistInfo;
import com.baclpt.spotifystreamer.tasks.TaskSearchArtist;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchArtistFragment extends BaseFragment {

    private static final String CURRENT_SEARCH_RESULTS_KEY = "current_search_results";

    private ArtistLvAdapter lvAdapter;
    private ArrayList<ArtistInfo> artistInfos;
    private ListView mListView;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_SEARCH_RESULTS_KEY)) {
            artistInfos = savedInstanceState.getParcelableArrayList(CURRENT_SEARCH_RESULTS_KEY);
        } else {
            artistInfos = new ArrayList<ArtistInfo>();
        }

        lvAdapter = new ArtistLvAdapter(getActivity(), R.layout.list_item_artist, artistInfos);


        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.results_listView);
        mListView.setAdapter(lvAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistInfo selectedArtist = lvAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), TopTracksActivity.class);
                intent.putExtra(TopTracksActivity.EXTRA_SPOTIFY_ID, selectedArtist.getSotifyId());
                intent.putExtra(TopTracksActivity.EXTRA_ARTIST_Name, selectedArtist.getName());
                startActivity(intent);

            }
        });


        EditText editText = (EditText) rootView.findViewById(R.id.query_editText);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() != KeyEvent.ACTION_UP)/*fix to the event handler is being called twice */
                        ) {
                    doSearch(textView);
                    return true;
                }
                return false;
            }
        });


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (artistInfos != null) {
            outState.putParcelableArrayList(CURRENT_SEARCH_RESULTS_KEY, artistInfos);
        }
        super.onSaveInstanceState(outState);
    }

    public void doSearch() {
        doSearch((TextView) getActivity().findViewById(R.id.query_editText));
    }

    private void doSearch(TextView queryTextView) {
        // starts executing the search on an asynctask
        TaskSearchArtist task = new TaskSearchArtist(this, lvAdapter);
        task.execute(queryTextView.getText().toString());

        // scroll to top
        mListView.smoothScrollToPosition(0);

        // this is a quick fix: to hide soft keyboard to show the results, otherwise would over lap the list view
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(queryTextView.getWindowToken(), 0);
    }


}
