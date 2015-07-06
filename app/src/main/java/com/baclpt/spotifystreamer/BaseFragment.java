package com.baclpt.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baclpt.spotifystreamer.data.TrackInfo;

/**
 * Created by Bruno on 10-06-2015.
 */
public class BaseFragment extends Fragment {

    private static final String LOG_TAG = BaseFragment.class.getSimpleName();

    // Fragment UI state
    protected enum FragmentState {
        LIST_RESULTS, // show a list view with the queried results
        LOADING_RESULTS, // show a loading spinner while performing a task
        INFO_MSG // show a text message with information to the user
    }

    // Flag to determine the UI state
    protected FragmentState fState;

    protected static final String CURRENT_STATE_KEY = "current_state";
    protected static final String CURRENT_MSG_KEY = "current_msg";

    protected ViewHolder viewHolder;
    protected boolean useTwoPaneLayout = false;


    private MenuItem menuItemNowPlaying;
    private MenuItem menuItemNowPlayingShare;
    private ShareActionProvider mShareActionProvider;
    protected Handler nowPlayingHandler;
    protected final Runnable nowPlayingAutoDismiss = new Runnable() {
        @Override
        public void run() {
            if (menuItemNowPlayingShare != null) menuItemNowPlayingShare.setVisible(false);
            if (menuItemNowPlaying != null) menuItemNowPlaying.setVisible(false);
        }
    };


    //service
    protected PlayerService playerService;
    protected boolean isConnectedToPlayerService;
    protected boolean isBindingToPlayerServiceEnabled = true;


    protected final PlayerService.CallbackPlayerUiUpdate callbackActions = new PlayerService.CallbackPlayerUiUpdate() {
        @Override
        public void onPrepared() {
        }

        @Override
        public void onPlayerStop() {
            autoDisableNowPlayingMenuItems();
        }

        @Override
        public void onStartPrepare(TrackInfo mTrackInfo) {
        }

        @Override
        public void onPlayerPause() {
            autoDisableNowPlayingMenuItems();
        }

        @Override
        public void onPlaying() {
            nowPlayingHandler.removeCallbacks(nowPlayingAutoDismiss);
            enableNowPlayingMenuItems(true);
        }

        @Override
        public void onPlayerCompletion() {
            autoDisableNowPlayingMenuItems();
        }

        @Override
        public void onError(int resID) {

        }
    };

    private void autoDisableNowPlayingMenuItems() {
        nowPlayingHandler.removeCallbacks(nowPlayingAutoDismiss);
        nowPlayingHandler.postDelayed(nowPlayingAutoDismiss, PlayerNotification.NOTIFICATION_AUTO_DISMISS);
    }

    //connect to the service
    protected final ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerServiceBinder binder = (PlayerService.PlayerServiceBinder) service;
            //get service
            playerService = binder.getService();

            if (callbackActions != null) playerService.registerCallbackObserver(callbackActions);

            isConnectedToPlayerService = true;

            enableNowPlayingMenuItems(playerService.isPlayerPlaying());

            Log.d(LOG_TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (callbackActions != null && playerService != null)
                playerService.removeCallbackObserver(callbackActions);
            Log.d(LOG_TAG, "onServiceDisconnected");
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        nowPlayingHandler = new Handler();
        useTwoPaneLayout = getResources().getBoolean(R.bool.large_layout);
        isConnectedToPlayerService = false;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // tow pane layout case, if it exists don't inflate again
        menuItemNowPlaying = menu.findItem(R.id.action_nowPlaying);

        if (menuItemNowPlaying == null) {
            inflater.inflate(R.menu.menu_now_playing, menu);

            // Retrieve the   menu item
            menuItemNowPlaying = menu.findItem(R.id.action_nowPlaying);
            menuItemNowPlaying.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    showPlayerActivity();
                    return true;
                }
            });
        }
        // Retrieve the share menu item
        menuItemNowPlayingShare = menu.findItem(R.id.action_share);
        if (menuItemNowPlayingShare != null) {

            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemNowPlayingShare);
//                if (playerService != null && playerService.getCurrentPlayingTrack()!=null;)
//                    mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(playerService.getCurrentPlayingTrack()));
        }


        if (playerService != null)
            enableNowPlayingMenuItems(playerService.isPlayerPlaying());
        else
            enableNowPlayingMenuItems(false);


    }

    public void showPlayerActivity() {
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        startActivity(intent);
    }

    public void enableNowPlayingMenuItems(boolean isEnabled) {
        if (menuItemNowPlaying != null) menuItemNowPlaying.setVisible(isEnabled);
        if (menuItemNowPlayingShare != null) {
            if (isEnabled) {
                if (playerService != null && playerService.getCurrentPlayingTrack() != null) {
                    mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(playerService.getCurrentPlayingTrack()));
                } else {
                    // else don't enable share menu item
                    isEnabled = false;
                }
            }
            menuItemNowPlayingShare.setVisible(isEnabled);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //start and bind the service when the activity starts
        if (isBindingToPlayerServiceEnabled && !isConnectedToPlayerService) {
            Intent playerServiceIntent = new Intent(getActivity(), PlayerService.class);
            getActivity().startService(playerServiceIntent);
            getActivity().bindService(playerServiceIntent, playerServiceConnection, Context.BIND_IMPORTANT);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isConnectedToPlayerService) {
            if (playerService != null) playerService.removeCallbackObserver(callbackActions);
            if (playerServiceConnection != null)
                getActivity().unbindService(playerServiceConnection);
        }playerService = null;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // store UI state
        outState.putSerializable(CURRENT_STATE_KEY, fState);
        // store UI info message
        outState.putString(CURRENT_MSG_KEY, viewHolder.mMessage.getText().toString());

    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        viewHolder = new ViewHolder(getView());

        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_STATE_KEY)) {
            // restore UI state
            fState = (FragmentState) savedInstanceState.get(CURRENT_STATE_KEY);
            switch (fState) {
                case LOADING_RESULTS:
                    showLoadingSpinner();
                    break;
                case INFO_MSG:
                    if (savedInstanceState.containsKey(CURRENT_MSG_KEY)) {
                        showMessage(savedInstanceState.getString(CURRENT_MSG_KEY));
                    } else {
                        fState = FragmentState.LIST_RESULTS;
                    }
                    break;
                default:
                    fState = FragmentState.LIST_RESULTS;
            }
        } else {
            fState = FragmentState.LIST_RESULTS;
        }

    }


    /**
     * Display the Loading Spinner.
     * Hides the Results ListView and the message
     */
    public void showLoadingSpinner() {
        hideResultsList();
        hideMessage();

        viewHolder.mLoadingSpinner.setVisibility(View.VISIBLE);
        fState = FragmentState.LOADING_RESULTS;
    }

    /**
     * Hides the Loading Spinner.
     */
    public void hideLoadingSpinner() {
        viewHolder.mLoadingSpinner.setVisibility(View.GONE);
    }

    /**
     * Display a message on the UI
     *
     * @param msg The message to display
     */
    public void showMessage(String msg) {
        hideResultsList();
        hideLoadingSpinner();

        viewHolder.mMessage.setText(msg);
        viewHolder.mMessage.setVisibility(View.VISIBLE);
        fState = FragmentState.INFO_MSG;
    }

    /**
     * Hides the message
     */
    public void hideMessage() {
        viewHolder.mMessage.setVisibility(View.GONE);
    }

    /**
     * Display the listView with the queried results
     */
    public void showResultsList() {
        hideMessage();
        hideLoadingSpinner();

        viewHolder.mResultsList.setVisibility(View.VISIBLE);
        fState = FragmentState.LIST_RESULTS;
    }

    /**
     * Hides the listView with the queried results
     */
    public void hideResultsList() {
        viewHolder.mResultsList.setVisibility(View.GONE);
    }

    /**
     * Auxiliary View Holder
     */
    protected class ViewHolder {
        public final ProgressBar mLoadingSpinner;
        public final TextView mMessage;
        public final ListView mResultsList;

        public ViewHolder(View view) {
            mLoadingSpinner = (ProgressBar) view.findViewById(R.id.loadingSpinner);
            mMessage = (TextView) view.findViewById(R.id.message_textView);
            mResultsList = (ListView) view.findViewById(R.id.results_listView);
        }
    }
}
