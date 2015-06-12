package com.baclpt.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyError;

/**
 * Created by Bruno on 09-06-2015.
 */
public class Utility {

    /**
     * Returns the two letter code of the preferred country.
     *
     * @param context The context of the activity.
     * @return  two letter string
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
     * @return  Returns the name
     */
    public static String getPreferredCountryLabel(Context context) {
        return (new Locale("", getPreferredCountry(context))).getDisplayCountry();
    }


    /**
     * Returns a more clarified message, for the user, about the error message thrown by  SpotifyService.
     *
     * @param spotifyError  Exception object thrown by SpotifyService
     * @param context  The context of the activity.
     * @return text message to display
     */
    public static String spotifyErrorFriendlyCustomizer(SpotifyError spotifyError, Context context) {
        if(spotifyError!=null) {
            String error = spotifyError.getErrorDetails().message;

            if (error.equalsIgnoreCase(context.getString(R.string.error_msg_unlaunched_market_api))) {
                return context.getString(R.string.error_msg_unlaunched_market) + getPreferredCountryLabel(context);
            }
            if (error.equalsIgnoreCase(context.getString(R.string.error_msg_unavailable_country_api))) {
                return context.getString(R.string.error_msg_unavailable_country) + getPreferredCountryLabel(context);
            }
            if (error.length() == 0) {
                return  context.getString(R.string.error_msg_remote_error);
            }
            return error;
        }
        return context.getString(R.string.error_msg_unknown_error);
    }


}
