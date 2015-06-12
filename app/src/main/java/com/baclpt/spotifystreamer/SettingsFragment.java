package com.baclpt.spotifystreamer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by Bruno on 09-06-2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //
        String defaultCountry = Locale.getDefault().getCountry();
        if (defaultCountry.equals("")) {
            defaultCountry = getString(R.string.settings_country_default);
        }

        ListPreference countryList = (ListPreference) findPreference(getString(R.string.settings_country_key));
        populateCountryList(countryList);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.settings_country_key)));
    }

    /**
     * Populate the ListPreference with country names and corresponding two letter codes as defined by ISO 3166-1.
     * @param lp The ListPreference to populate
     */
    private void populateCountryList(ListPreference lp) {

        // get country list(2 letter code) from the OS library
        String[] countryCodes = Locale.getISOCountries();

        // make an arraylist of the country name and its code
        ArrayList<Country> list = new ArrayList<Country>(countryCodes.length);
        for (String code : countryCodes) {
            list.add(new Country(code, (new Locale("", code)).getDisplayCountry()));
        }
        // sort alphabetically by the country's name
        Collections.sort(list);

        // covert the sorted countries to string arrays
        String[] countryLabels = new String[list.size()];
        countryCodes = new String[list.size()];
        int i = 0;
        for (Country c : list) {
            countryLabels[i] = c.getLabel();
            countryCodes[i] = c.getCode();
            i++;
        }

        // add data to ListPreference
        lp.setEntries(countryLabels);
        lp.setEntryValues(countryCodes);
    }


    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

    /**
     *  Auxiliary class to help sorting alphabetically the country name  (and retaining its 2 letter code)
     */
    public class Country implements Comparable<Country> {
        private String code;
        private String label;

        public Country(String code, String label) {
            super();
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public int compareTo(Country o) {
            return this.label.compareTo(o.label);
        }
    }
}