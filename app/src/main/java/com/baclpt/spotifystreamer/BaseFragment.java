package com.baclpt.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *
 * Created by Bruno on 10-06-2015.
 */
public class BaseFragment extends Fragment {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // store UI state
        outState.putSerializable(CURRENT_STATE_KEY, fState);
        // store UI info message
        outState.putString(CURRENT_MSG_KEY, viewHolder.mMessage.getText().toString());

        super.onSaveInstanceState(outState);
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
     * @param msg  The messsage to display
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
     * Display the listview with the queried results
     */
    public void showResultsList() {
        hideMessage();
        hideLoadingSpinner();

        viewHolder.mResultsList.setVisibility(View.VISIBLE);
        fState = FragmentState.LIST_RESULTS;
    }

    /**
     * Hides the listview with the queried results
     */
    public void hideResultsList() {
        viewHolder.mResultsList.setVisibility(View.GONE);
    }

    /**
     * Auxiliary View Holder
     */
    protected class ViewHolder {
        public ProgressBar mLoadingSpinner;
        public TextView mMessage;
        public ListView mResultsList;

        public ViewHolder(View view) {
            mLoadingSpinner = (ProgressBar) view.findViewById(R.id.loadingSpinner);
            mMessage = (TextView) view.findViewById(R.id.message_textView);
            mResultsList = (ListView) view.findViewById(R.id.results_listView);
        }
    }
}
