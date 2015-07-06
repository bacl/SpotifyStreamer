package com.baclpt.spotifystreamer;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class PlayerActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlayerActivityFragment playerFragment = new PlayerActivityFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, playerFragment, PlayerActivityFragment.FRG_TAG_PLAYER);
        ft.commit();

    }

    @Override
    public Intent getParentActivityIntent() {
        // UP/HOME button fix
            return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
