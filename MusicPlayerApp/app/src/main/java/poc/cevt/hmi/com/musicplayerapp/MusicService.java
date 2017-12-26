package poc.cevt.hmi.com.musicplayerapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    // media player objects
    private MediaPlayer player = new MediaPlayer();

    // current song's position within list
    private int songPosition = 0;

    // service binder
    private final IBinder binder = new MusicBinder();

    // options
    private boolean repeat = false;
    private boolean shuffle = false;
    private Random random;

    public MusicService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        initializeMusicPlayer();
    }

    public void setRepeat() {
        if (repeat) {
            repeat = false;
            Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
        } else {
            repeat = true;
            Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show();
        }
    }

    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
            Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
        } else {
            shuffle = true;
            Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
        }
    }

    public void initializeMusicPlayer() {
        random = new Random();

        // allows playback when device becomes idle
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        // set stream type to music
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    /**
     * Main play method
     *
     * NOTE: Application device MUST be connected to same wifi network as server
     */
    public void playSong() {
        Log.d("Test", "playSong");
        MainActivity.started = true;

        player.reset();
        // get song
        Song playSong = MainActivity.allSongs.getSongs().get(songPosition);
        // get id
        long currentSong = playSong.getId();
        // set uri

        // for local raw MP3 files
//        Uri trackUri = Uri.parse("android.resource://poc.cevt.hmi.com.musicplayerapp/" + currentSong);

        // server
        // change IP address as needed
        // TODO: Replace IP with getIP() method when completed
        Uri trackUri =  Uri.parse("http://" + "172.31.113.22" + ":8080/api/music/song1");

        AllSongs.currentId = currentSong;
        AllSongs.currentTitle = playSong.getTitle();
        AllSongs.currentArtist = playSong.getArtist();

        try {
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
    }

    public void playNext() {
        MainActivity.started = false;

        if (repeat) {
            playSong();
        } else if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = random.nextInt(MainActivity.allSongs.getSongs().size());
            }
            songPosition = newSong;
            playSong();
        } else {
            songPosition++;
            if (songPosition == MainActivity.allSongs.getSongs().size())
                songPosition = 0;
            playSong();
        }
    }

    public void playPrev() {
        MainActivity.started = false;

        if (repeat) {
            playSong();
        } else {
            songPosition--;
            if (songPosition < 0)
                songPosition = MainActivity.allSongs.getSongs().size() - 1;
            playSong();
        }
    }

    /**
     * Mute and unMute for manual seekBar dragging
     */
    public void mutePlayer() {
        player.setVolume(0, 0);
    }

    public void unMutePlayer() {
        player.setVolume(1, 1);
    }

    /**
     * Utilities
     */
    public void setSongPosition(int songIndex) {
        songPosition = songIndex;
    }

    public int getSongPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int position) {
        player.seekTo(position);
    }

    public void resume() {
        player.start();
    }

    public void go() {
        playSong();
    }

    /**
     * When a song finishes
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        MainActivity.started = false;

        // repeat on
        if (repeat) {
            mp.reset();
            playSong();
        } else {
            if (player.getCurrentPosition() > 1) {
                mp.reset();
                playNext();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    /**
     * Required for service binding
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Stop service when user exits app
     */
    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    /**
     * Required binder class
     */
    class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
