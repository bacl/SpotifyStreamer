package com.baclpt.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.baclpt.spotifystreamer.data.TrackInfo;

/**
 * Created by Bruno on 22-06-2015.
 */

public abstract class PlayerNotification implements PlayerService.CallbackPlayerUiUpdate {
    public static final int NOTIFICATION_ID = 123;
    public static final int NOTIFICATION_AUTO_DISMISS = 4000; //ms
    protected TrackInfo currentTrackInfo;
    protected Context context;
    protected Handler notificationHandler;
    protected final Runnable notificationAutoDismiss = new Runnable() {
        @Override
        public void run() {
            cancelNotifications();
        }
    };

    public PlayerNotification() {
    }

    public PlayerNotification(Context context) {
        this.context = context;
        notificationHandler = new Handler();
    }

    public static PlayerNotification buildPlayerNotification(Context cx) {
        if (Build.VERSION.SDK_INT >= 21) { // Load the API V21 class only if the OS can load it.
            return new PlayerNotificationV21(cx);
        }
        return new PlayerNotificationV16(cx);
    }

    public void setCurrentTrack(TrackInfo currentTrack) {
        this.currentTrackInfo = currentTrack;
    }

    public abstract void initMediaSessionStuff();

    public abstract void updateMetaData();
    public abstract Notification buildNotification(PlayerService.PlayerInternalState mPlayerInternalState, String tickerText);

    public NotificationCompat.Builder getNotificationCompatBuilder(PlayerService.PlayerInternalState playerInternalState, String tickerText) {

        Intent toLaunch = new Intent(context, PlayerActivity.class);
        toLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentShowPlayer = PendingIntent.getActivity(context, 0, toLaunch, PendingIntent.FLAG_UPDATE_CURRENT);


        Bitmap contactPicLarge   = Utility.getBitmapFromURLAsync(context, currentTrackInfo.getAlbumArtSmallURL());


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);


        if (playerInternalState == PlayerService.PlayerInternalState.ON_PLAYING) {
            builder.setSmallIcon(android.R.drawable.ic_media_play);
        } else {
            builder.setSmallIcon(R.drawable.ic_notification);
        }


        builder.setContentTitle(currentTrackInfo.getTrackName())
                .setContentText(currentTrackInfo.getSpotifyArtistName())
                .setContentIntent(pendingIntentShowPlayer);

        if (tickerText != null) builder.setTicker(tickerText);


        if (contactPicLarge != null) {
            builder.setLargeIcon(contactPicLarge);
        } else {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.thumbnail_fail));
        }


        builder.addAction(generateAction(android.R.drawable.ic_media_previous, context.getString(R.string.button_label_previous), PlayerService.ACTION_PREVIOUS));
        boolean isOngoing = false;
        switch (playerInternalState) {
            case ON_PLAYING:
                isOngoing = true;
                builder.addAction(generateAction(android.R.drawable.ic_media_pause, context.getString(R.string.button_label_pause), PlayerService.ACTION_PAUSE));
                break;
            case ON_PLAYER_PAUSE:
                builder.addAction(generateAction(android.R.drawable.ic_media_play, context.getString(R.string.button_label_play), PlayerService.ACTION_PLAY));
                break;
            case ON_START_PREPARE:
                isOngoing = true;
                builder.addAction(generateAction(R.mipmap.ic_media_stop, context.getString(R.string.button_label_stop), PlayerService.ACTION_STOP));
                break;
            default:
                builder.addAction(generateAction(android.R.drawable.ic_media_play, context.getString(R.string.button_label_play), PlayerService.ACTION_PLAY));
                break;
        }

        builder.addAction(generateAction(android.R.drawable.ic_media_next, context.getString(R.string.button_label_next), PlayerService.ACTION_NEXT));

        builder.setAutoCancel(isOngoing);
        builder.setShowWhen(false);

        return builder;
    }


    protected void updateNotification(PlayerService.PlayerInternalState mPlayerInternalState, String tickerText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, buildNotification(mPlayerInternalState, tickerText));
    }

    protected NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    public void cancelNotifications() {
        Intent intentToService = new Intent(context, PlayerService.class);
        intentToService.setAction(PlayerService.ACTION_NOTIFICATION_CANCEL);
        context.startService(intentToService);
    }

    public void notificationAutoDismiss() {
        notificationHandler.postDelayed(notificationAutoDismiss, NOTIFICATION_AUTO_DISMISS);
    }

//
//
//
//
//

    @TargetApi(21)
    public static class PlayerNotificationV21 extends PlayerNotification {
        private static final String MEDIA_SESSION_TAG = "SpotifyStreamerMediaSession";
        private MediaSessionCompat mSession;

        public PlayerNotificationV21(Context cx) {
            super(cx);
        }

        public void initMediaSessionStuff() {
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(Intent.ACTION_MEDIA_BUTTON), 0);
            ComponentName eventReceiver = new ComponentName(context.getPackageName(), RemoteControlReceiver.class.getName());
            mSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG, eventReceiver, pi);
            mSession.setActive(true);
        }


        public Notification buildNotification(PlayerService.PlayerInternalState playerInternalState, String tickerText) {


            NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

            style.setMediaSession(mSession.getSessionToken());


            PendingIntent pendingIntentStopPlayer = PendingIntent.getService(context, 1, new Intent(context, PlayerService.class).setAction(PlayerService.ACTION_STOP), 0);
            style.setCancelButtonIntent(pendingIntentStopPlayer);
            style.setShowCancelButton(true);

            style.setShowActionsInCompactView(0, 1, 2);

            NotificationCompat.Builder builder = getNotificationCompatBuilder(playerInternalState, tickerText);
            builder.setStyle(style);

            if (Utility.isPreferredLockScreenEnabled(context)) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            } else {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            }


            return builder.build();
        }


        public void updateMetaData() {
            if (mSession != null) {
                MediaMetadataCompat.Builder mMetaBuilder = new MediaMetadataCompat.Builder();
                if (Utility.isPreferredLockScreenEnabled(context)) {
                    mMetaBuilder.putText(MediaMetadata.METADATA_KEY_TITLE, currentTrackInfo.getTrackName());
                    mMetaBuilder.putText(MediaMetadata.METADATA_KEY_ALBUM, currentTrackInfo.getAlbumName());
                    mMetaBuilder.putText(MediaMetadata.METADATA_KEY_ARTIST, currentTrackInfo.getSpotifyArtistName());
                    Bitmap contactPicLarge = Utility.getBitmapFromURLAsync(context, currentTrackInfo.getAlbumArtLargeURL());
                    if (contactPicLarge == null) {
                        contactPicLarge = Utility.getBitmapFromURLAsync(context, currentTrackInfo.getAlbumArtSmallURL());
                    }
                    if (contactPicLarge != null) {
                        mMetaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, contactPicLarge);
                    } else {
                        mMetaBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, (BitmapFactory.decodeResource(context.getResources(), R.mipmap.thumbnail_fail)));
                    }
                }
                mSession.setMetadata(mMetaBuilder.build());
                mSession.setActive(true);

            }
        }


        @Override
        public void onPrepared() {
        }

        @Override
        public void onPlayerStop() {
            notificationAutoDismiss();
        }

        @Override
        public void onStartPrepare(TrackInfo mTrackInfo) {
            notificationHandler.removeCallbacks(notificationAutoDismiss);
            currentTrackInfo = mTrackInfo;
            updateMetaData();
            updateNotification(PlayerService.PlayerInternalState.ON_START_PREPARE, context.getString(R.string.msg_buffering));
        }

        @Override
        public void onPlayerPause() {
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYER_PAUSE, context.getString(R.string.msg_paused));
            notificationAutoDismiss();
        }

        @Override
        public void onPlaying() {
            notificationHandler.removeCallbacks(notificationAutoDismiss);
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYING, context.getString(R.string.msg_playing, currentTrackInfo.getTrackName(), currentTrackInfo.getSpotifyArtistName()));
        }


        @Override
        public void onPlayerCompletion() {
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYER_COMPLETION, null);
            notificationAutoDismiss();
        }

        @Override
        public void onError(int resID) {
            updateNotification(PlayerService.PlayerInternalState.ON_ERROR, context.getString(resID));
        }

    }
//
//
//
//
//
//
//
//

    @TargetApi(16)
    @SuppressWarnings("deprecation")
    public static class PlayerNotificationV16 extends PlayerNotification {


        private RemoteControlClient mRemoteControlClient;
        private AudioManager mAudioManager;

        public PlayerNotificationV16(Context cx) {
            super(cx);
        }


        public void initMediaSessionStuff() {

            if (Utility.isPreferredLockScreenEnabled(context)) {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                ComponentName rec = new ComponentName(context.getPackageName(), RemoteControlReceiver.class.getName());
                mAudioManager.registerMediaButtonEventReceiver(rec);

                Intent intentMediaButtons = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intentMediaButtons.setComponent(rec);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentMediaButtons, 0);
                mRemoteControlClient = new RemoteControlClient(pendingIntent);

                mAudioManager.registerRemoteControlClient(mRemoteControlClient);

                int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                        | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                        | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                        | RemoteControlClient.FLAG_KEY_MEDIA_STOP;

                mRemoteControlClient.setTransportControlFlags(flags);

                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            } else {

                if (mAudioManager != null) { // clean lock screen
                    RemoteControlClient.MetadataEditor mEditor = mRemoteControlClient.editMetadata(true);
                    mEditor.apply();
                    mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
                    mRemoteControlClient = null;
                    mAudioManager = null;
                }
            }

        }


        public Notification buildNotification(PlayerService.PlayerInternalState playerInternalState, String tickerText) {

            NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();


            PendingIntent pendingIntentStopPlayer = PendingIntent.getService(context, 1, new Intent(context, PlayerService.class).setAction(PlayerService.ACTION_STOP), 0);
            style.setCancelButtonIntent(pendingIntentStopPlayer);
            style.setShowCancelButton(true);

            style.setShowActionsInCompactView(0, 1, 2);

            NotificationCompat.Builder builder = getNotificationCompatBuilder(playerInternalState, tickerText);
            builder.setStyle(style);


            return builder.build();
        }


        public void updateMetaData() {
            //get lock
            if (mAudioManager == null)
                initMediaSessionStuff();
            else
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (Utility.isPreferredLockScreenEnabled(context)) {
                if (mRemoteControlClient != null) {
                    RemoteControlClient.MetadataEditor mEditor = mRemoteControlClient.editMetadata(true);
                    mEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrackInfo.getSpotifyArtistName());
                    mEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrackInfo.getTrackName());
                    mEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentTrackInfo.getAlbumName());
                    Bitmap img = Utility.getBitmapFromURLAsync(context, currentTrackInfo.getAlbumArtSmallURL());
                    mEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, img);

                    mEditor.apply();

                }
            }
        }


        @Override
        public void onPrepared() {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);

        }

        @Override
        public void onStartPrepare(TrackInfo mTrackInfo) {
            if (mRemoteControlClient != null) {
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_BUFFERING);
            }
            currentTrackInfo = mTrackInfo;
            updateMetaData();
            notificationHandler.removeCallbacks(notificationAutoDismiss);
            updateNotification(PlayerService.PlayerInternalState.ON_START_PREPARE, context.getString(R.string.msg_buffering));
        }

        @Override
        public void onPlayerPause() {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYER_PAUSE, context.getString(R.string.msg_paused));
            notificationAutoDismiss();
        }

        @Override
        public void onPlaying() {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            notificationHandler.removeCallbacks(notificationAutoDismiss);
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYING, context.getString(R.string.msg_playing, currentTrackInfo.getTrackName(), currentTrackInfo.getSpotifyArtistName()));
        }

        @Override
        public void onPlayerStop() {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            notificationAutoDismiss();
        }

        @Override
        public void onPlayerCompletion() {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            updateNotification(PlayerService.PlayerInternalState.ON_PLAYER_COMPLETION, null);
            notificationAutoDismiss();
        }

        @Override
        public void onError(int resID) {
            if (mRemoteControlClient != null)
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_ERROR);
            updateNotification(PlayerService.PlayerInternalState.ON_ERROR, context.getString(resID));
        }


    }
}

