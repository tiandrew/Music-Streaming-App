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
import java.util.Random;

import static poc.cevt.hmi.com.musicplayerapp.MainActivity.allSongs;
import static poc.cevt.hmi.com.musicplayerapp.MainActivity.queue;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {

    private MediaPlayer player = new MediaPlayer();

    private final IBinder binder = new MusicBinder();

    // current song's position within list
    private int songPosition = 0;

    // options
    private boolean repeat = false;
    private boolean shuffle = false;
    private Random random;

    // constructor
    public MusicService() {}

    /*
    OVERRIDES
     */

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        Log.i(Const.MUSIC_SERVICE, "onCreate");
        super.onCreate();

        initializeMusicPlayer();
    }

    /**
     * When a song finishes
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(Const.MUSIC_SERVICE, "onCompletion");

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

    /**
     * onError
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(Const.MUSIC_SERVICE, "MusicService onError");

        mp.reset();
        return false;
    }

    /**
     * onPrepared
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(Const.MUSIC_SERVICE, "onPrepared");

        mp.start();
    }

    /**
     * Required for service binding
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Const.MUSIC_SERVICE, "onBind");

        return binder;
    }

    /**
     * Stop service when user exits app
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(Const.MUSIC_SERVICE, "onUnbind");

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

    /*
    PUBLIC
     */

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
     * Turn on and off repeat option
     */
    public void setRepeat() {
        if (repeat) {
            repeat = false;
            Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
        } else {
            repeat = true;
            Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Turn on and off shuffle option
     */
    public void setShuffle() {
        if (shuffle) {
            shuffle = false;
            Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
        } else {
            shuffle = true;
            Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets listeners and prepares MediaPlayer
     */
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
     * Main music playing method
     */
    public void playSong() {
        player.reset();
        // get song
        Song playSong;
        if (!allSongs.getSongs().isEmpty()) {
            playSong = allSongs.getSongs().get(getSongPosition());
        } else {
            Toast.makeText(getApplicationContext(), "No songs", Toast.LENGTH_SHORT).show();
            return;
        }

        // set URI
        Uri uri = Uri.parse("http://" + Const.IP + ":8080/api/music/playSong" +
                            "?track=" + playSong.getTrack());
        // play song
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        // update title in MainActivity via broadcast
        allSongs.setCurrentSong(playSong);

        // update metaData
        Intent mainIntent = new Intent(Const.TITLE_UPDATED);

        mainIntent.putExtra(Const.TITLE, playSong.getTitle());
        mainIntent.putExtra(Const.ARTISTS, playSong.getArtist());
        mainIntent.putExtra(Const.ALBUMS, playSong.getAlbumArt());

        sendBroadcast(mainIntent);

        // update queue
        Intent queueIntent = new Intent(Const.QUEUE);
        sendBroadcast(queueIntent);
    }

    /**
     * Plays next song; called in MainActivity
     */
    public void playNext() {
        allSongs.setPrevSong(allSongs.getSongs().get(getSongPosition()));

        if (repeat) {
            playSong();
        } else if (!queue.getQueue().isEmpty()) {
            // set front of queue to be played next
            setSongPosition(allSongs.getSongs().indexOf(queue.getQueue().get(0)));
            queue.popFront();

            playSong();
        } else if (shuffle) {
            int newSong = getSongPosition();

            // prevent same song from playing again
            while (newSong == getSongPosition())
                newSong = random.nextInt(allSongs.getSongs().size());

            setSongPosition(newSong);
            playSong();
        } else {
            songPosition++;

            if (getSongPosition() == allSongs.getSongs().size())
                setSongPosition(0);

            playSong();
        }
    }

    /**
     * Plays previous song; called in MainActivity
     */
    public void playPrev() {
        if (repeat) {
            playSong();
        } else if (allSongs.getPrevSong() != null) {
            // remember the next song
            queue.addFront(allSongs.getCurrentSong());

            int prevIndex = allSongs.getSongs().indexOf(allSongs.getPrevSong());
            setSongPosition(prevIndex);

            if (getSongPosition() < 0)
                setSongPosition(allSongs.getSongs().size() - 1);

            playSong();
        } else {
            songPosition--;

            if (getSongPosition() == -1)
                setSongPosition(allSongs.getSongs().size() - 1);

            playSong();
        }
    }

    /**
     * Utilities
     */
    public void setSongPosition(int songIndex) {
        songPosition = songIndex;
    }

    public int getSongPosition() {
        return songPosition;
    }

    public int getSongProgress() {
        return player.getCurrentPosition();
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
}
