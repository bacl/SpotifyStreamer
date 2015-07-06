package com.baclpt.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Bruno on 23-06-2015.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    public RemoteControlReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Intent intentToService = new Intent(context, PlayerService.class);

                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_PLAY);

                } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_NEXT);

                }else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS== event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_PREVIOUS);

                }else if (KeyEvent.KEYCODE_MEDIA_PAUSE== event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_PAUSE);

                }else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE== event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_PLAY_PAUSE);

                }else if (KeyEvent.KEYCODE_MEDIA_STOP== event.getKeyCode()) {
                    intentToService.setAction(PlayerService.ACTION_STOP);

                }

                if (intentToService.getAction() != null)
                    context.startService(intentToService);

                Log.v("RemoteControlReceiver##", "onReceive  " + event.getKeyCode());
            }
        }

    }

}