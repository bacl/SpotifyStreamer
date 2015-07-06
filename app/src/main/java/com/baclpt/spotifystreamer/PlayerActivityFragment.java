package com.baclpt.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baclpt.spotifystreamer.data.TrackInfo;
import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends DialogFragment {
    private static final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();
    protected static final String FRG_TAG_PLAYER = "frg_tag_player";
    protected static final int TIME_ONE_MS = 1000;

    // flag to check if PlayerActivityFragment is being displayed as a dialog or a framed activity
    private boolean isDialog;

    // current track details
    private TrackInfo mTrackInfo;

    // Handler to update seek bar progress periodically
    private Handler seekHandler = new Handler();
    private boolean isSeekUpdateRunning;
    private final Runnable seekUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            seekUpdate();
        }
    };

    // ui
    private TextView tvArtistName;
    private TextView tvAlbumName;
    private TextView tvTrackName;
    private ImageButton btPlay;
    private ImageButton btNext;
    private ImageButton btPrev;
    private ImageView ivThumb;
    private SeekBar mSeekBar;
    private TextView tvTimeCurrent;
    private TextView tvTimeTotal;


    // player service
    private PlayerService playerService;
    private boolean isConnectedToPlayerService;

    //connect to the service
    private final ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerServiceBinder binder = (PlayerService.PlayerServiceBinder) service;
            //get service
            playerService = binder.getService();
            isConnectedToPlayerService = true;

            playerService.registerCallbackObserver(callbackActions);

            refreshPlayerInfo();

            playerService.setNotificationActive(false);

            Log.d(LOG_TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService.removeCallbackObserver(callbackActions);
            isConnectedToPlayerService = false;
            Log.d(LOG_TAG, "onServiceDisconnected");
        }
    };

    // callback actions to update UI
  private   final PlayerService.CallbackPlayerUiUpdate callbackActions = new PlayerService.CallbackPlayerUiUpdate() {
        @Override
        public void onStartPrepare(TrackInfo newTrackInfo) {
            mTrackInfo = newTrackInfo;
            displayTrackInfo();
        }

        @Override
        public void onPlayerCompletion() {
            btPlay.setImageResource(android.R.drawable.ic_media_play);
            stopSeekUpdateThread();
            mSeekBar.setProgress(0);
        }

        @Override
        public void onPlayerStop() {
            btPlay.setImageResource(android.R.drawable.ic_media_play);
            stopSeekUpdateThread();
            mSeekBar.setProgress(0);
        }

        @Override
        public void onPlayerPause() {
            btPlay.setImageResource(android.R.drawable.ic_media_play);
            stopSeekUpdateThread();
        }

        @Override
        public void onPlaying() {
            btPlay.setImageResource(android.R.drawable.ic_media_pause);
            showSeekBarTrackDuration();
            startSeekUpdateThread();
        }

        @Override
        public void onPrepared() {
            showSeekBarTrackDuration();
            btPlay.setImageResource(android.R.drawable.ic_media_play);

        }

        @Override
        public void onError(int resID) {
            btPlay.setImageResource(android.R.drawable.ic_media_play);
            // show error msg
            showSeekBarMsg(getString(resID));
        }
    };


    public PlayerActivityFragment() {
    }

    /**
     * Update the information being displayed (track info and player status)
     */
    private void refreshPlayerInfo() {
        if (playerService != null) {

            mTrackInfo = playerService.getCurrentPlayingTrack();
            if (mTrackInfo != null)
                displayTrackInfo();

            // if it is currently playing
            if (playerService.isPlayerPlaying()) {
                showSeekBarTrackDuration();
                startSeekUpdateThread();
                btPlay.setImageResource(android.R.drawable.ic_media_pause);

            } else { //if player is not ready and  not being buffered , start buffering current track
                if (!playerService.isPrepared() && !playerService.isPreparing()) {
                    playerService.startPrepareCurrentTrack();
                }
            }
        }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // if it is a large layout it will be displayed as a dialog
        isDialog = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        tvArtistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        tvAlbumName = (TextView) rootView.findViewById(R.id.player_album_name);
        tvTrackName = (TextView) rootView.findViewById(R.id.player_track_name);
        btPlay = (ImageButton) rootView.findViewById(R.id.player_play_button);
        btNext = (ImageButton) rootView.findViewById(R.id.player_next_button);
        btPrev = (ImageButton) rootView.findViewById(R.id.player_previous_button);
        ivThumb = (ImageView) rootView.findViewById(R.id.player_thumbnail);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seekBar);
        tvTimeCurrent = (TextView) rootView.findViewById(R.id.player_track_time_past);
        tvTimeTotal = (TextView) rootView.findViewById(R.id.player_track_time_total);


        // set listeners

        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPauseMusic();
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextMusic();
            }
        });
        btPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousMusic();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (playerService != null && fromUser) {
                    playerService.seekTo(progress);
                }

                tvTimeCurrent.setText(Utility.formatTime(progress / TIME_ONE_MS));
            }
        });


        return rootView;
    }


    /**
     * Configure seek bar parameters and time labels (current playing position and track duration) to the current track
     */
    private void showSeekBarTrackDuration() {
        mSeekBar.setProgress(playerService.getCurrentPosition());
        mSeekBar.setMax(playerService.getTrackDuration());
        mSeekBar.setEnabled(true);

        tvTimeCurrent.setText(Utility.formatTime(playerService.getCurrentPosition() / TIME_ONE_MS));
        tvTimeTotal.setText(Utility.formatTime(playerService.getTrackDuration() / TIME_ONE_MS));
    }

    /**
     * Reset seekBar and show a text msg of buffering
     */
    private void showSeekBarBuffering() {
        // show buffering msg
        mSeekBar.setProgress(0);
        mSeekBar.setMax(1);
        mSeekBar.setEnabled(true);

        tvTimeCurrent.setText(R.string.msg_buffering);
        tvTimeTotal.setText("");
    }

    /**
     * Show a text msg and disable SeekBar
     */
    private void showSeekBarMsg(String msg) {
        // show buffering msg
        mSeekBar.setEnabled(false);
        tvTimeCurrent.setText(msg);
        tvTimeTotal.setText("");
    }

    /**
     * Populate the UI with the current track information
     */
    private void displayTrackInfo() {

        tvArtistName.setText(mTrackInfo.getSpotifyArtistName());
        tvAlbumName.setText(mTrackInfo.getAlbumName());
        tvTrackName.setText(mTrackInfo.getTrackName());

        //  set artist thumbnail
        if (mTrackInfo.getAlbumArtLargeURL() != null) {
            Picasso.with(getActivity()).load(mTrackInfo.getAlbumArtLargeURL()).into(ivThumb);
        } else {
            // if doesn't have a large thumbnail try to set the small one
            if (mTrackInfo.getAlbumArtSmallURL() != null) {
                Picasso.with(getActivity()).load(mTrackInfo.getAlbumArtSmallURL()).into(ivThumb);
            } else {
                // if doesn't have a thumbnail sets a default fail one
                ivThumb.setImageResource(R.mipmap.thumbnail_fail);
            }
        }


        if (playerService != null) {
            // configure seekBar
            if (playerService.isPrepared()) {
                showSeekBarTrackDuration();
            } else {
                // show buffering msg
                showSeekBarBuffering();
            }

            // enable / disable next/previous buttons
            btNext.setEnabled(playerService.hasNextTrack());
            btPrev.setEnabled(playerService.hasPreviousTrack());

        } else {
            mSeekBar.setProgress(0);
            mSeekBar.setMax(0);
            tvTimeCurrent.setText("");
            tvTimeTotal.setText("");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        //start and bind the service when the activity starts
        Intent playerServiceIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(playerServiceIntent);
        getActivity().bindService(playerServiceIntent, playerServiceConnection, Context.BIND_IMPORTANT);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerService != null) {
            if (playerService.isPlayerPlaying()) {
                // if it is playing show a notification with controls of the current playing track
                playerService.setNotificationActive(true);
            } else {
                // if it is not playing discard current loaded track
                playerService.discardPrepare();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPlayerInfo();
    }


    @Override
    public void onStop() {
        stopSeekUpdateThread();

        // Unbind from the service
        if (isConnectedToPlayerService) {
            getActivity().unbindService(playerServiceConnection);
            isConnectedToPlayerService = false;
            playerService.removeCallbackObserver(callbackActions);
            playerService = null;
        }

        super.onStop();
    }


    public void playOrPauseMusic() {
        if (playerService.isPlayerPlaying()) {
            playerService.pausePlayer();
        } else {
            playerService.playTrack();
        }
    }

    /**
     * Starts a helper runnable (under UI thread) to update seek bar progress periodically
     */
    private void startSeekUpdateThread() {
        stopSeekUpdateThread();
        isSeekUpdateRunning = true;
        seekUpdate();
    }

    private void stopSeekUpdateThread() {
        isSeekUpdateRunning = false;
        seekHandler.removeCallbacks(seekUpdateRunnable);
    }


    /**
     * Update seek bar progress
     */
    public void seekUpdate() {
        if (playerService != null) {
            int mCurrentPosition = playerService.getCurrentPosition();
            mSeekBar.setProgress(mCurrentPosition);
        }
        if (isSeekUpdateRunning) seekHandler.postDelayed(seekUpdateRunnable, 1000);
    }

    public void playNextMusic() {
        if (playerService.hasNextTrack()) {
            stopSeekUpdateThread();
            playerService.playNextTrack();
        }
    }

    public void playPreviousMusic() {
        if (playerService.hasPreviousTrack()) {
            stopSeekUpdateThread();
            playerService.playPreviousTrack();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // if it is a framed window a menu item to share current track
        if (!isDialog) {
            inflater.inflate(R.menu.menu_player, menu);
            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);
            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (mTrackInfo != null)
                mShareActionProvider.setShareIntent(Utility.createShareTrackIntent(mTrackInfo));
        }
    }
}
