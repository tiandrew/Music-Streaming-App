package poc.cevt.hmi.com.musicplayerapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import poc.cevt.hmi.com.musicplayerapp.MusicService.MusicBinder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends Activity implements Runnable {
    // all songs
    public static AllSongs allSongs = new AllSongs();

    // song queue
    public static Queue queue = new Queue();

    // left-side main menu
    private ListView menuListView;

    // music service objects
    private static MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;

    // seekBar objects
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private TextView currentDuration;
    private TextView totalDuration;
    private long totalTime = 0;

    // volume seekBar objects
    private SeekBar volumeSeekBar;
    private AudioManager audioManager;

    /*
    OVERRIDES
     */

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Const.TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(metaUpdate, new IntentFilter(Const.TITLE_UPDATED));

        new GetSongsAsyncTask().execute();

        initializeMenuListView();

        initializeSeekBar();

        initializeVolumeSeekBar();
    }

    /**
     * Unregister broadcast receiver
     */
    @Override
    protected void onDestroy() {
        Log.i(Const.TAG, "onDestroy");
        super.onDestroy();

        unregisterReceiver(metaUpdate);
        unbindService(musicConnection);
    }

    /*
    PRIVATE
     */

    /**
     * SeekBar setup and handling
     */
    private void initializeSeekBar() {
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        totalDuration = new TextView(this);
        totalDuration = (TextView) findViewById(R.id.totalDuration);
        currentDuration = new TextView(this);
        currentDuration = (TextView) findViewById(R.id.currentDuration);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicBound) {
                    // update current time on change
                    currentDuration.setText(convertedTime(progress * 1000));
                    // update seekBar
                    seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicService.mutePlayer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.unMutePlayer();
                seekTo(getCurrentPosition());
            }
        });
        // start seekBar updating thread
        run();
    }

    /**
     * Volume seekBar setup and handling
     */
    private void initializeVolumeSeekBar() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        try {
            volumeSeekBar = (SeekBar)findViewById(R.id.volumeSeekBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekBar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {}

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {}

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start service when activity starts
     */
    @Override
    protected void onStart() {
        Log.i(Const.TAG, "onStart");
        super.onStart();
        // create new intent if not existing already
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    /**
     * BroadcastReceiver to receive updates on current song display metadata
     */
    private BroadcastReceiver metaUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] byteArray = intent.getExtras().getByteArray(Const.ALBUMS);

            // set track album art
            if (byteArray != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                ImageView albumArt = (ImageView) findViewById(R.id.albumArt);
                albumArt.setImageBitmap(Bitmap.createScaledBitmap(bmp, albumArt.getWidth(),
                        albumArt.getHeight(), false));
            }

            // set track title and artist
            TextView trackTitle = (TextView) findViewById(R.id.trackTitle);
            trackTitle.setText(intent.getExtras().getString(Const.TITLE));
            TextView trackArtist = (TextView) findViewById(R.id.trackArtist);
            trackArtist.setText(intent.getExtras().getString(Const.ARTISTS));
        }
    };

    /**
     * Connect to the music service
     */
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    /**
     * Initializes left main menu listView
     */
    private void initializeMenuListView() {
        ArrayList<String> menuItems = new ArrayList<>();
        menuItems.add(Const.PLAYLISTS);
        menuItems.add(Const.SONGS);
        menuItems.add(Const.ALBUMS);
        menuItems.add(Const.ARTISTS);

        menuListView = (ListView) findViewById(R.id.menuListView);
        MenuListAdapter menuListAdapter = new MenuListAdapter(this, menuItems);
        menuListView.setAdapter(menuListAdapter);

        setMenuItemListeners();
    }

    /**
     * Sets menu listView item OnClick listeners
     */
    private void setMenuItemListeners() {
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                Object itemAtPosition = menuListView.getItemAtPosition(position);
                String item = itemAtPosition.toString();

                Bundle bundle = new Bundle();
                ListFragment listFragment;

                switch (item) {
                    case Const.PLAYLISTS: // playlists
                        bundle.putString(Const.TYPE, Const.PLAYLISTS);
                        listFragment = new ListFragment();
                        listFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, listFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case Const.SONGS: // songs
                        bundle.putString(Const.SONGS, Const.ALL);
                        SongListFragment songListFragment = new SongListFragment();
                        songListFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, songListFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case Const.ALBUMS: // albums
                        bundle.putString(Const.TYPE, Const.ALBUMS);
                        listFragment = new ListFragment();
                        listFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, listFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    case Const.ARTISTS: // artist
                        bundle.putString(Const.TYPE, Const.ARTISTS);
                        listFragment = new ListFragment();
                        listFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, listFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                }
            }
        });
    }

    /*
    PUBLIC
     */

    /**
     * Thread that updates seekBar as song plays
     */
    @Override
    public void run() {
        if (musicBound) {
            if (allSongs.getCurrentSong() != null)
                totalTime = allSongs.getCurrentSong().getDuration();

            int currentTime = getCurrentPosition();

            seekBar.setMax((int) (totalTime / 1000));
            seekBar.setProgress(getCurrentPosition() / 1000);

            // timers
            totalDuration.setText(convertedTime(totalTime));
            currentDuration.setText(convertedTime(currentTime));
        }
        handler.postDelayed(this, 1000);
    }

    /**
     * OnClick method turns repeat option on and off
     */
    public void repeat(View view) {
        musicService.setRepeat();
    }

    /**
     * OnClick method turns shuffle option on and off
     */
    public void shuffle(View view) {
        musicService.setShuffle();
    }

    /**
     * Sorts songs by alphabetical order
     */
    public void sortSongList() {
        Collections.sort(allSongs.getSongs(), new Comparator<Song>() {
            // custom Comparator
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    /**
     * OnClick method for picking song list item
     */
    public static void songPicked(Song song) {
        musicService.setSongPosition(allSongs.getSongs().indexOf(song));
//        musicService.setSongPosition(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }


    /**
     * OnClick methods for play/pause button
     */
    public void playPause(View view) throws IOException {
        if (isPlaying()) {
            pause();
            paused = true;
        } else if (paused && !isPlaying()) {
            resume();
            paused = false;
        } else if (!paused && !isPlaying()) {
            start();
        }
    }


    public void start() {
        musicService.go();
    }

    public void pause() {
        musicService.pausePlayer();
    }

    public void resume() {
        musicService.resume();
    }

    /**
     * OnClick methods for next button
     */
    public void playNext(View view) {
        musicService.playNext();
    }

    /**
     * OnClick method for previous button
     */
    public void playPrev(View view) {
        // go to beginning of song only if more than 5 seconds into song
        if (getCurrentPosition() > 5000) {
            musicService.playSong();
        } // go to previous song
        else {
            musicService.playPrev();
        }
    }

    /**
     * OnClick method for queue button
     */
    public void showQueue(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();

        bundle.putString(Const.QUEUE, Const.ALL);
        SongListFragment songListFragment = new SongListFragment();
        songListFragment.setArguments(bundle);
        ft.replace(R.id.fragmentLayout, songListFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Convert millis to timer
     */
    public static String convertedTime(long millis) {
        Date date = new Date(millis);
        DateFormat format;
        // less than 10 minutes
        if (millis < 600000) {
            format = new SimpleDateFormat("m:ss");
        } // more than 10 minutes, less than 1 hour
        else if (millis >= 600000 && millis < 3600000) {
            format = new SimpleDateFormat("mm:ss");
        }  // 1 hour or more
        else {
            format = new SimpleDateFormat("HH:mm:ss");
        }
        return format.format(date);
    }

    /**
     * Utilities
     */
    public int getCurrentPosition() {
        if (musicService != null && musicBound) {
            return musicService.getSongProgress();
        } else {
            return 0;
        }
    }

    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    public boolean isPlaying() {
        return musicService != null && musicBound && musicService.isPlaying();
    }
}
