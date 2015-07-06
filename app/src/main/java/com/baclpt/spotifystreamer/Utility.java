package com.baclpt.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.baclpt.spotifystreamer.data.TrackInfo;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyError;

/**
 * Created by Bruno on 09-06-2015.
 */
public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    /**
     * Returns boolean value of the preference to automatically play the next track in the playlist
     *
     * @param context The context of the activity.
     * @return boolean value of the preference
     */
    public static boolean isPreferredAutoPlayEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.settings_auto_play_key),
                Boolean.parseBoolean(context.getString(R.string.settings_auto_play_default)));
    }


    /**
     * Returns boolean value of the preference to show player controls on the lock screen
     *
     * @param context The context of the activity.
     * @return boolean value of the preference
     */
    public static boolean isPreferredLockScreenEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.settings_notification_key),
                Boolean.parseBoolean(context.getString(R.string.settings_notification_default)));
    }


    /**
     * Returns the two letter code of the preferred country.
     *
     * @param context The context of the activity.
     * @return two letter string
     */
    public static String getPreferredCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.settings_country_key),
                context.getString(R.string.settings_country_default));
    }

    /**
     * Returns the name of the preferred country.
     *
     * @param context The context of the activity.
     * @return Returns the name
     */
    public static String getPreferredCountryLabel(Context context) {
        return (new Locale("", getPreferredCountry(context))).getDisplayCountry();
    }


    /**
     * Returns a more clarified message, for the user, about the error message thrown by  SpotifyService.
     *
     * @param spotifyError Exception object thrown by SpotifyService
     * @param context      The context of the activity.
     * @return text message to display
     */
    public static String spotifyErrorFriendlyCustomizer(SpotifyError spotifyError, Context context) {
        if (spotifyError != null && spotifyError.getErrorDetails() != null) {
            String error = spotifyError.getErrorDetails().message;

            if (error.equalsIgnoreCase(context.getString(R.string.error_msg_unlaunched_market_api))) {
                return context.getString(R.string.error_msg_unlaunched_market) + getPreferredCountryLabel(context);
            }
            if (error.equalsIgnoreCase(context.getString(R.string.error_msg_unavailable_country_api))) {
                return context.getString(R.string.error_msg_unavailable_country) + getPreferredCountryLabel(context);
            }
            if (error.trim().isEmpty()) {
                return context.getString(R.string.error_msg_remote_error);
            }
            return error;
        }
        return context.getString(R.string.error_msg_unknown_error);
    }

    /**
     * Return a formated string with time
     *
     * @param timeInSeconds  Time in seconds
     * @return formated string
     */
    public static String formatTime(final int timeInSeconds) {
        final int hours = timeInSeconds / 3600;
        final int minutes = (timeInSeconds % 3600) / 60;
        final int seconds = timeInSeconds % 60;

        if (hours == 0)
            return String.format("%02d:%02d", minutes, seconds);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    }

    /**
     *  Return a {@code Bitmap} from a url. This is done on an async task.
     *
     * @param cx  Application context
     * @param source  Image URL to load
     * @return the bitmap of the image
     */
    public static Bitmap getBitmapFromURLAsync(final Context cx, final String source) {
        Bitmap contactPic = null;

        try {
            contactPic = new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        return Picasso.with(cx).load(source)
                                .placeholder(R.mipmap.thumbnail_fail)
                                .error(R.mipmap.thumbnail_fail)
                                .get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBitmapFromURLAsync ", e);
        }

        return contactPic;
    }

    /**
     * Creates a share Intent for a given track
     * @param mTrackInfo
     * @return
     */
    public static Intent createShareTrackIntent(TrackInfo mTrackInfo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= 21) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            //noinspection deprecation
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mTrackInfo.getSpotifyArtistName() + " - " + mTrackInfo.getTrackName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrackInfo.getExternalURL());
        return shareIntent;
    }

    /**
     * Hide the soft keyboard
     * @param activity current activity
     */

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
