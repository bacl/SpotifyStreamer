package com.baclpt.spotifystreamer.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.baclpt.spotifystreamer.BaseFragment;
import com.baclpt.spotifystreamer.R;
import com.baclpt.spotifystreamer.Utility;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.RetrofitError;

/**
 * Created by Bruno on 11-06-2015.
 */
public class BaseAsyncTask<T> extends AsyncTask<String, Void, ArrayList<T>> {

    protected ArrayAdapter<T> lvAdapter;
    protected BaseFragment mFragment;
    protected boolean isConnected;
    protected String errorMSG;

    public BaseAsyncTask() {
    }

    /**
     * @param baseFragment the Fragment containing the UI to update
     * @param lvAdapter    The List view adapter to add the search results
     */
    public BaseAsyncTask(BaseFragment baseFragment, ArrayAdapter<T> lvAdapter) {
        this.mFragment = baseFragment;
        this.lvAdapter = lvAdapter;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        errorMSG = null;

        // show loading spinner  on UI
        mFragment.showLoadingSpinner();
    }


    @Override
    protected ArrayList<T> doInBackground(String... params) {
        // check if has internet access
        ConnectivityManager cm = (ConnectivityManager) mFragment.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            isConnected = ni.isConnected();
        } else {
            isConnected = false;
        }
        if (!isConnected) return null;


        // init Spotify Api
        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        ArrayList<T> resultList;

        try {
            resultList = doSearch(spotify,params);

        } catch (RetrofitError error) {
            errorMSG = Utility.spotifyErrorFriendlyCustomizer(SpotifyError.fromRetrofitError(error), mFragment.getActivity());
            resultList = null;
        }
        return resultList;
    }

    protected ArrayList<T> doSearch(SpotifyService spotify, String... params) throws RetrofitError {
        throw new UnsupportedOperationException();
    }


    @Override
    protected void onPostExecute(ArrayList<T> result) {

        // add found results to the list view adapter, if any.
        lvAdapter.clear();
        if (result != null) {
            for (T r : result)
                lvAdapter.add(r);
        }

        // changes the UI to display the task result
        if (isConnected) {
            if (result == null || lvAdapter.getCount() == 0) {
                if (errorMSG != null) {
                    // an error occurred
                    mFragment.showMessage(errorMSG);
                } else {
                    // no results found
                    mFragment.showMessage(mFragment.getString(R.string.error_msg_no_results));
                }
            } else {
                // everything is ok, show the list of results
                mFragment.showResultsList();
            }
        } else {
            // has no internet
            mFragment.showMessage(mFragment.getString(R.string.error_msg_no_internet));
        }

    }

}
