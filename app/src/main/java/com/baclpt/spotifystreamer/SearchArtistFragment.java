package com.baclpt.spotifystreamer;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ArrayList<ArtistInfo> artistInfoList;
    private ListView mListView;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // if it is on a phone invoke parent method  BaseFrame.onStart() to bind to the service to receive callback updates (show/hide NowPLaying button)
        // else do nothing, because TopTracksFragment will be doing this work.

        isBindingToPlayerServiceEnabled = !useTwoPaneLayout;

        View rootView = inflater.inflate(R.layout.fragment_search_artist, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_SEARCH_RESULTS_KEY)) {
            artistInfoList = savedInstanceState.getParcelableArrayList(CURRENT_SEARCH_RESULTS_KEY);
        } else {
            artistInfoList = new ArrayList<>();

        }

        lvAdapter = new ArtistLvAdapter(getActivity(), R.layout.list_item_artist, artistInfoList);


        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.results_listView);
        mListView.setAdapter(lvAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistInfo selectedArtist = lvAdapter.getItem(position);

                CallbackSearchArtist act = (CallbackSearchArtist) getActivity();
                act.onArtistSelected(selectedArtist);


            }
        });
        mListView.requestFocus();

        SearchView searchView = (SearchView) rootView.findViewById(R.id.query_editText);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    cleanSearchResults();
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (artistInfoList != null) {
            outState.putParcelableArrayList(CURRENT_SEARCH_RESULTS_KEY, artistInfoList);

        }

    }


    public void doSearch() {
        doSearch(((SearchView) getActivity().findViewById(R.id.query_editText)).getQuery().toString());
    }

    private void doSearch(String query) {

        // check if query is empty
        if (!query.trim().isEmpty()) {

            // starts executing the search on an AsyncTask
            TaskSearchArtist task = new TaskSearchArtist(this, lvAdapter);
            task.execute(query);

            // scroll to top
            mListView.smoothScrollToPosition(0);
            mListView.clearChoices();

            cleanTopTracksResults();


            // this is a quick fix: to hide soft keyboard to show the results, otherwise would over lap the list view
            Utility.hideKeyboard(getActivity());
        } else {
            showMessage(getString(R.string.error_msg_empty_query));
        }

    }

    private void cleanSearchResults() {
        mListView.clearChoices();
        lvAdapter.clear();
        if (useTwoPaneLayout)
            cleanTopTracksResults();
    }

    private void cleanTopTracksResults() {
        if (useTwoPaneLayout) {
            CallbackSearchArtist act = (CallbackSearchArtist) getActivity();
            act.onArtistSelected(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!artistInfoList.isEmpty()) {
            mListView.requestFocus();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface CallbackSearchArtist {
        /**
         * Callback for when an item has been selected.
         */
        void onArtistSelected(ArtistInfo selectedArtistInfo);
    }
}
