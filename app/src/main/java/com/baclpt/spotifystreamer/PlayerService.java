package com.baclpt.spotifystreamer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.baclpt.spotifystreamer.data.TrackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruno on 15-06-2015.
 */


public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_NOTIFICATION_CANCEL = "action_notification_cancel";
    public static final String ACTION_NOTIFICATION_SHOW = "action_notification_show";
    public static final String ACTION_NOTIFICATION_UPDATE = "action_notification_update";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";

    public enum PlayerInternalState {
        ON_PREPARED,
        ON_START_PREPARE,
        ON_PLAYER_PAUSE,
        ON_PLAYER_STOP,
        ON_PLAYING,
        ON_PLAYER_COMPLETION,
        ON_ERROR,
        ON_INIT
    }


    //media player
    private MediaPlayer mMediaPlayer;
    private PlayerInternalState mPlayerInternalState;
    private boolean playMusicWhenPrepared;
    private int duration;


    //binder
    private final IBinder psBind = new PlayerServiceBinder();

    // observers
    private List<CallbackPlayerUiUpdate> observersPlayerUI;
    private int errorMsgID;
    private PlayerNotification playerNotification = null;

    // playlist
    private String playListID;
    private ArrayList<TrackInfo> playList;
    private TrackInfo mTrackInfo = null;
    private int currentPlayingTrackIndex;


    @Override
    public void onCreate() {
        //create the service
        super.onCreate();

        //create player
        mMediaPlayer = new MediaPlayer();
        observersPlayerUI = new ArrayList<>();

        playerNotification = PlayerNotification.buildPlayerNotification(this);
        playerNotification.initMediaSessionStuff();

        initMusicPlayer();
        setPlayerInternalState(PlayerInternalState.ON_INIT);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopPlayer();
        //stop service
        stopSelf();
    }

    public void initMusicPlayer() {
        //set player properties
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setNotificationActive(boolean isActive) {
        if (isActive) {
            playerNotification.setCurrentTrack(mTrackInfo);
            registerCallbackObserver(playerNotification);
            playerNotification.updateMetaData();
            startForeground(PlayerNotification.NOTIFICATION_ID, playerNotification.buildNotification(mPlayerInternalState, null));
        } else {
            removeCallbackObserver(playerNotification);
            stopForeground(true);
        }
    }

    public void registerCallbackObserver(CallbackPlayerUiUpdate observer) {
        observersPlayerUI.add(observer);
    }

    public void removeCallbackObserver(CallbackPlayerUiUpdate observer) {
        observersPlayerUI.remove(observer);
    }

    public void notifyObserversUI(PlayerInternalState state) {
        setPlayerInternalState(state);
        switch (state) {
            case ON_PREPARED:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onPrepared();
                }
                break;
            case ON_START_PREPARE:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onStartPrepare(mTrackInfo);
                }
                break;
            case ON_PLAYER_PAUSE:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onPlayerPause();
                }
                break;
            case ON_PLAYING:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onPlaying();
                }
                break;
            case ON_PLAYER_COMPLETION:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onPlayerCompletion();
                }
                break;
            case ON_PLAYER_STOP:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onPlayerStop();
                }
                break;
            case ON_ERROR:
                for (CallbackPlayerUiUpdate observer : observersPlayerUI) {
                    observer.onError(errorMsgID);
                }
                break;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {

            String action = intent.getAction();
            Log.d(LOG_TAG, "onStartCommand   action " + action);
            if (action.equalsIgnoreCase(PlayerService.ACTION_PLAY)) {
                playTrack();
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_PAUSE)) {
                pausePlayer();
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_PLAY_PAUSE)) {
                if (isPlayerPlaying())
                    pausePlayer();
                else
                    playTrack();
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_PREVIOUS)) {
                playPreviousTrack();
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_NEXT)) {
                playNextTrack();
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_NOTIFICATION_SHOW)) {
                setNotificationActive(true);
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_NOTIFICATION_UPDATE)) {
                setNotificationActive(false);
                setNotificationActive(true);
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_NOTIFICATION_CANCEL)) {
                setNotificationActive(false);
            } else if (action.equalsIgnoreCase(PlayerService.ACTION_STOP)) {
                stopPlayer();
            }
        }

        return START_STICKY;
    }


    //play a song
    private void startPrepare(TrackInfo trackInfo) {


        discardPrepare();

        mTrackInfo = trackInfo;

        notifyObserversUI(PlayerInternalState.ON_START_PREPARE);


        // check if has internet access
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {// if not connected
            errorMsgID = R.string.error_msg_no_internet;
            notifyObserversUI(PlayerInternalState.ON_ERROR);
        } else {

            //set the data source
            try {
                mMediaPlayer.setDataSource(mTrackInfo.getPreviewURL());

                mMediaPlayer.prepareAsync();

            } catch (Exception e) {
                //Log.e(LOG_TAG, "Error setting data source", e);
                if (mTrackInfo.getPreviewURL() == null)
                    errorMsgID = R.string.msg_buffering_error;
                else
                    errorMsgID = R.string.error_msg_unknown_error;

                notifyObserversUI(PlayerInternalState.ON_ERROR);
            }
        }
    }

    public void discardPrepare() {
        mPlayerInternalState = PlayerInternalState.ON_INIT;
        mMediaPlayer.reset();
        duration = 0;
    }


    public void playTrack() {

        if (isPrepared()) {
            playTrackNow();
        } else if (isPreparing()) {
            playMusicWhenPrepared = true;
        } else {
            startPrepare(mTrackInfo);
        }
    }

    private void playTrackNow() {
        mMediaPlayer.start();
        playMusicWhenPrepared = false;
        notifyObserversUI(PlayerInternalState.ON_PLAYING);
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
        playMusicWhenPrepared = false;
        notifyObserversUI(PlayerInternalState.ON_PLAYER_PAUSE);
    }

    public void stopPlayer() {
        mMediaPlayer.stop();
        notifyObserversUI(PlayerInternalState.ON_PLAYER_STOP);
        setNotificationActive(false);
    }

    public boolean isPlayerPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void seekTo(int a) {
        if (isPrepared())
            mMediaPlayer.seekTo(a);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
//        errorMsgID = R.string.error_msg_unknown_error;
//        notifyObserversUI(PlayerInternalState.ON_ERROR);
//        return true;
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        duration = mp.getDuration();
        notifyObserversUI(PlayerInternalState.ON_PREPARED);
        if (playMusicWhenPrepared) {
            playTrackNow();
            playMusicWhenPrepared = false;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //mp.seekTo(0);
        notifyObserversUI(PlayerInternalState.ON_PLAYER_COMPLETION);

        if (Utility.isPreferredAutoPlayEnabled(this)) {
            playNextTrack();
        } else {
            discardPrepare();
            setPlayerInternalState(PlayerInternalState.ON_INIT);
        }
    }

    public void setPlayerInternalState(PlayerInternalState state) {
        mPlayerInternalState = state;
        Log.d(LOG_TAG, "setPlayerInternalState   " + state);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return psBind;
    }


    @Override
    public void onDestroy() {
        releaseResources();
        Log.d(LOG_TAG, "onDestroy   noooo!!  ");
    }

    //release resources
    public void releaseResources() {
        mMediaPlayer.release();
        // Cancel the persistent notification.
        if (playerNotification != null) {
            removeCallbackObserver(playerNotification);
        }
        setNotificationActive(false);
    }


    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getTrackDuration() {
        return duration;
    }

    public boolean isPrepared() {
        return mPlayerInternalState == PlayerInternalState.ON_PREPARED
                || mPlayerInternalState == PlayerInternalState.ON_PLAYING
                || mPlayerInternalState == PlayerInternalState.ON_PLAYER_PAUSE
                || mPlayerInternalState == PlayerInternalState.ON_PLAYER_COMPLETION;
    }


    public void setPlayList(ArrayList<TrackInfo> playList, String playListID) {
        currentPlayingTrackIndex = 0;
        // just clone the arrayList not its content
        this.playList = new ArrayList<>(playList.size());
        for (TrackInfo item : playList) this.playList.add(item);

        this.playListID = playListID;
    }

    public ArrayList<TrackInfo> getPlayList() {
        return this.playList;
    }

    /**
     * Return the next track playlist index, -1 if there is no more tracks
     *
     * @return track position index
     */
    public int getNextTrackPosition() {
        int index = currentPlayingTrackIndex + 1;
        if (index < playList.size())
            return index;
        return -1;
    }

    /**
     * Return the previous track playlist index, -1 if there is no more tracks
     *
     * @return track position index
     */
    public int getPreviousTrackPosition() {
        int index = currentPlayingTrackIndex - 1;
        if (index >= 0)
            return index;
        return -1;
    }

    /**
     * Sets the selected index of for the current track.
     *
     * @param index index of the track to select in the current playlist
     * @return {@code TrackInfo} of the selected track, null if index doesn't exists.
     */
    public TrackInfo setSelectedTrack(int index) {
        if (index >= 0 && index < playList.size()) {
            currentPlayingTrackIndex = index;
            return playList.get(currentPlayingTrackIndex);
        }
        return null;
    }

    /**
     * Returns the current playing track information
     *
     * @return {@code TrackInfo} of the selected track, null if playlist not set.
     */
    public TrackInfo getCurrentPlayingTrack() {
        if (playList != null)
            return playList.get(currentPlayingTrackIndex);
        else return null;
    }

    public int getCurrentPlayingTrackIndex() {
        return currentPlayingTrackIndex;
    }

    public void setCurrentPlayingTrackIndex(int currentPlayingTrackIndex) {
        this.currentPlayingTrackIndex = currentPlayingTrackIndex;
    }

    public boolean hasNextTrack() {
        return (currentPlayingTrackIndex + 1) < playList.size();
    }

    public boolean hasPreviousTrack() {
        return (currentPlayingTrackIndex - 1) >= 0;
    }

    public void playNextTrack() {
        if (hasNextTrack()) {
            boolean wasPlaying = isPlayerPlaying() || Utility.isPreferredAutoPlayEnabled(this);
            TrackInfo newTrackInfo = setSelectedTrack(getNextTrackPosition());
            if (newTrackInfo != null) {
                startPrepare(newTrackInfo);
                if (wasPlaying) playTrack(); // continue to last player state
            }
        }
    }

    public void playPreviousTrack() {
        if (hasPreviousTrack()) {
            boolean wasPlaying = isPlayerPlaying();
            TrackInfo newTrackInfo = setSelectedTrack(getPreviousTrackPosition());
            if (newTrackInfo != null) {
                startPrepare(newTrackInfo);
                if (wasPlaying) playTrack();// continue to last player state
            }
        }
    }

    public void startPrepareCurrentTrack() {
        startPrepare(getCurrentPlayingTrack());
    }

    public boolean isPreparing() {
        return mPlayerInternalState == PlayerInternalState.ON_START_PREPARE;
    }


    public String getPlayListID() {
        return playListID;
    }


    //binder
    public class PlayerServiceBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    // callback interface
    public interface CallbackPlayerUiUpdate {

        void onStartPrepare(TrackInfo mTrackInfo);

        void onPrepared();

        void onPlaying();

        void onPlayerPause();

        void onPlayerStop();

        void onPlayerCompletion();

        void onError(int resID);
    }

}

