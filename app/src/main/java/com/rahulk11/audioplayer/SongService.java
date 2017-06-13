package com.rahulk11.audioplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rahul on 6/9/2017.
 */

public class SongService extends Service {
    public final static String ACTION_PLAY = "PLAY";
    public final static String ACTION_PAUSE = "PAUSE";
    public final static String ACTION_RESUME = "RESUME";
    public final static String ACTION_STOP = "STOP";
    private AudioManager audioManager;
    private PhoneStateListener phoneStateListener;
    private static MediaPlayer player;
    private static Context mContext;
    String title="", artist = "", album = "";
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PlaybackManager.playNext(true);
            }
        });
        try {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        if (player.isPlaying()) {
                            player.pause();
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {

                    } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mgr != null) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(ACTION_PLAY)){
            String data = intent.getStringExtra("path");
            title = intent.getStringExtra("songTitle");
            artist = intent.getStringExtra("songArtist");
            album = intent.getStringExtra("songAlbum");
            try {
                player.reset();
                player.setDataSource(data);
                player.prepare();
                player.start();
                new NotificationHandler(mContext, title, artist, album, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (action.equals(ACTION_PAUSE)){
            if(player.isPlaying()){
                player.pause();
                new NotificationHandler(mContext, title, artist, album, false);
            }
        } else if (action.equals(ACTION_RESUME)){
            if(player!=null){
                player.start();
                new NotificationHandler(mContext, title, artist, album, true);
            }
        }else if (action.equals(ACTION_STOP)){
            if(player!=null){
                stopSelf();
            }
        }
        return Service.START_NOT_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {

    }
    public void onPause() {

    }
    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        player = null;
        stopSelf();
    }

    @Override
    public void onLowMemory() {

    }


    public static boolean isPlaying(){
        if(player!=null)
            return player.isPlaying();
        return false;
    }

}
