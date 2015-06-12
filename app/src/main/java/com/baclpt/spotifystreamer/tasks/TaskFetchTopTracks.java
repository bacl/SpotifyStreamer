package com.baclpt.spotifystreamer.tasks;

import android.widget.ArrayAdapter;

import com.baclpt.spotifystreamer.BaseFragment;
import com.baclpt.spotifystreamer.R;
import com.baclpt.spotifystreamer.Utility;
import com.baclpt.spotifystreamer.data.TrackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * Asynctask to search spotify for an artist name
 * Created by Bruno on 04-06-2015.
 */
public class TaskFetchTopTracks extends BaseAsyncTask<TrackInfo> {

    private final int minimumThumbSmallWidth = 200; // minimum width for small thumbnail
    private final int minimumThumbLargeWidth = 640; // minimum  width for large thumbnail

    private String sotifyArtistID;
    private String sotifyArtistName;


    public TaskFetchTopTracks(BaseFragment baseFragment, ArrayAdapter<TrackInfo> lvAdapter) {
        super(baseFragment, lvAdapter);
    }


    @Override
    protected ArrayList<TrackInfo> doSearch(SpotifyService spotify, String... params) throws RetrofitError {
        // check if has params
        if (params.length == 0) return null;
        sotifyArtistID = params[0];
        sotifyArtistName = params[1];


        // query configuration
        HashMap<String, Object> config = new HashMap<>();
        config.put(SpotifyService.COUNTRY, Utility.getPreferredCountry(mFragment.getActivity()));

        // send query  to spotify service
        Tracks results = spotify.getArtistTopTrack(sotifyArtistID, config);

        // array list to store the processed results
        ArrayList<TrackInfo> resultList = new ArrayList<TrackInfo>();

        // for each track found extract need info and add to resultList ArrayList
        Iterator<Track> foundTrackIterator = results.tracks.iterator();
        while (foundTrackIterator.hasNext()) {
            Track t = foundTrackIterator.next();
            TrackInfo trackInfo = new TrackInfo(sotifyArtistID);

            trackInfo.setAlbumName(t.album.name);
            trackInfo.setTrackName(t.name);
            trackInfo.setPreviewURL(t.preview_url);

            if (t.album.images.size() > 0) {
                Iterator<Image> imageIterator = t.album.images.iterator();


                int largeImgWidthFound = Integer.MAX_VALUE;
                int smallImgWidthFound = minimumThumbLargeWidth;

                while (imageIterator.hasNext()) {
                    Image image = imageIterator.next();

                    //select the image closest to the minimumThumbSmallWidth but smaller then minimumThumbLargeWidth
                    if (image.width >= minimumThumbSmallWidth && smallImgWidthFound > image.width) {
                        trackInfo.setAlbumArtSmallURL(image.url);
                        smallImgWidthFound = image.width;
                    }
                    //select the image closest to the minimumThumbLargeWidth
                    if (image.width >= minimumThumbLargeWidth && largeImgWidthFound > image.width) {
                        trackInfo.setAlbumArtLargeURL(image.url);
                        largeImgWidthFound = image.width;
                    }
                }
            }

            resultList.add(trackInfo);
        }

        return resultList;
    }

    @Override
    protected void onPostExecute(ArrayList<TrackInfo> result) {

        // add found results to the list view adapter, if any.
        lvAdapter.clear();
        if (result != null) {
            for (TrackInfo r : result)
                lvAdapter.add(r);
        }

        // changes the UI to display the task result
        if (isConnected) {
            if (result == null || lvAdapter.getCount() == 0) {
                if (errorMSG != null) {
                    // an error occurred
                    mFragment.showMessage(errorMSG);
                } else {
                    // no tracks found
                    mFragment.showMessage(mFragment.getActivity().getString(R.string.error_msg_no_results_top_track, sotifyArtistName));
                }
            } else {
                // everything is ok, show the list of results
                mFragment.showResultsList();
            }
        } else {
            // has no internet
            mFragment.showMessage(mFragment.getActivity().getString(R.string.error_msg_no_internet));
        }

    }

}
