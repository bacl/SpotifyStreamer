package com.baclpt.spotifystreamer.tasks;

import android.widget.ArrayAdapter;

import com.baclpt.spotifystreamer.BaseFragment;
import com.baclpt.spotifystreamer.R;
import com.baclpt.spotifystreamer.Utility;
import com.baclpt.spotifystreamer.data.ArtistInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.RetrofitError;


/**
 * Asynctask to search spotify for an artist name
 * Created by Bruno on 04-06-2015.
 */
public class TaskSearchArtist extends BaseAsyncTask<ArtistInfo> {

    private final int minimumThumbWidth = 200; // minimum thumbnail width
    private final int numberOfResutlsPerQuery = 20;

    private String artistNameQuery;



    public TaskSearchArtist(BaseFragment baseFragment, ArrayAdapter<ArtistInfo> lvAdapter) {
        super(baseFragment, lvAdapter);
    }


    @Override
    protected ArrayList<ArtistInfo> doSearch(SpotifyService spotify, String... params) throws RetrofitError {
        // check if has params
        if (params.length == 0) return null;
        artistNameQuery = params[0];

        // query configuration
        HashMap<String, Object> config = new HashMap<>();
        // only artists with content playable in the selected market will be returned.
        config.put(SpotifyService.MARKET, Utility.getPreferredCountry(mFragment.getActivity()));

        // TODO: add the remaining results
//        config.put(SpotifyService.OFFSET, 0);
        config.put(SpotifyService.LIMIT, numberOfResutlsPerQuery);



        // send query  to spotify service
        ArtistsPager spotifyResponse = spotify.searchArtists(artistNameQuery, config);

        // array list to store the processed results
        ArrayList<ArtistInfo> resultList = new ArrayList<ArtistInfo>();

        // for each artist found extract need info and add to resultList ArrayList
        Iterator<Artist> foundArtistsIterator = spotifyResponse.artists.items.iterator();
        while (foundArtistsIterator.hasNext()) {
            Artist artist = foundArtistsIterator.next();
            ArtistInfo artistInfo = new ArtistInfo();

            artistInfo.setName(artist.name);
            artistInfo.setSotifyId(artist.id);

            if (artist.images.size() > 0) {
                Iterator<Image> imageIterator = artist.images.iterator();

                int smallImgWidthFound = 1640;// a big mumber

                while (imageIterator.hasNext()) {
                    Image image = imageIterator.next();

                    //select the image closest to the minimumThumbWidth
                    if (image.width >= minimumThumbWidth && smallImgWidthFound > image.width) {
                        artistInfo.setImageURL(image.url);
                        smallImgWidthFound = image.width;
                    }

                    //select the smallest
//                        if (smallImgWidthFound > image.width) {
//                            artistInfo.setImageURL(image.url);
//                            smallImgWidthFound = image.width;
//                        }

                }
            }
            // add to the ArrayList
            resultList.add(artistInfo);
        }

        return resultList;
    }


    @Override
    protected void onPostExecute(ArrayList<ArtistInfo> result) {

        // add found results to the list view adapter, if any.
        lvAdapter.clear();
        if (result != null) {
            for (ArtistInfo r : result)
                lvAdapter.add(r);
        }

        // changes the UI to display the task result
        if (isConnected) {
            if (result == null || lvAdapter.getCount() == 0) {
                if (errorMSG != null) {
                    // an error occurred
                    mFragment.showMessage(errorMSG);
                } else {
                    // no artists found
                    mFragment.showMessage(mFragment.getString(R.string.error_msg_no_results_artist, artistNameQuery));
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
