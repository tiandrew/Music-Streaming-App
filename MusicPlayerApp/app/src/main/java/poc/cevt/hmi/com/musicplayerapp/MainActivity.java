package poc.cevt.hmi.com.musicplayerapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import poc.cevt.hmi.com.musicplayerapp.MusicService.MusicBinder;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends Activity implements Runnable {

    // main menu listView
    private ListView menuListView;

    // music service objects
    public static AllSongs allSongs = new AllSongs();
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;

    // seekBar objects
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private TextView currentDuration;
    private TextView totalDuration;
    public static boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(Const.TAG, "onCreate");

        initializeMenuListView();

        initializeSeekBar();

        new ServerHandler().execute();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("test", allSongs.getSongs().size() + "");
                for (Song song : allSongs.getSongs()) {
                    Log.d("test", song.getTitle());
                }
            }
        }, 7000);


//        getSongList();
//        Log.d("Test", AllSongs.getSongs().size() + "");

//        updateMetaData();
    }

    /**
     * SeekBar setup and handling
     * TODO: Bug where seekBar sets to 0 when paused. Retains position when resumed.
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
     * Thread that updates seekBar as song plays
     */
    @Override
    public void run() {
        updateMetadataDisplay();

        if (musicBound) {
            long totalTime = getDuration();
            long currentTime = getCurrentPosition();

//            if(started && currentTime == 0) {
//                return;
//            }

            seekBar.setMax(getDuration() / 1000);
            seekBar.setProgress(getCurrentPosition() / 1000);

            // timers
            totalDuration.setText(convertedTime(totalTime));
            currentDuration.setText(convertedTime(currentTime));
        }
        handler.postDelayed(this, 1000);
    }

    /**
     * Updates currently playing song and artist texts
     */
    public void updateMetadataDisplay() {
        TextView songTitle = (TextView) findViewById(R.id.trackTitle);
        TextView songArtist = (TextView) findViewById(R.id.trackArtist);
        // update title and artist
        songTitle.setText(AllSongs.currentTitle);
        songArtist.setText(AllSongs.currentArtist);
    }

    /**
     * Convert millis to timer for seeker
     */
    private String convertedTime(long millis) {
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
     * Turns repeat and shuffle on and off
     */
    public void repeat(View view) {
        musicService.setRepeat();
    }

    public void shuffle(View view) {
        musicService.setShuffle();
    }

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
     * Start service when activity starts
     */
    @Override
    protected void onStart() {
        super.onStart();
        // create new intent if not existing already
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    /**
     * Receive songs from Raw folder
     * TODO: Get metadata
     */
    public void getSongList() {
        try {
            Field[] fields = R.raw.class.getFields();
            Log.d("Test", fields.length + "");

            if (fields.length != 0) {
                // start at 1 and end at length - 1 to avoid $change & serialVersionUID files
                for (int i = 1; i < fields.length; ++i) {
                    Log.d("Test", i + "");

                    // ID for reference
                    int resourceID = fields[i].getInt(fields[i]);

                    allSongs.addSong(new Song(resourceID, fields[i].getName(), "", ""));
                    Log.d("Test", fields[i].getName());
                    // TODO: retrieve other metadata
                }
            }
        } catch (Exception ignored) {}

        // sort songs alphabetically
        sortSongList();
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
     * Calls all metadata update methods
     */
    public void updateMetaData() {
        updateArtists();
        updateAlbums();
    }

    /**
     * Update artist list
     */
    public void updateArtists() {
        for (Song song : allSongs.getSongs()) {
            // add artist if not existing
            if (!allSongs.getArtists().contains(song.getArtist())) {
                allSongs.getArtists().add(song.getArtist());
            }
        }
    }

    /**
     * Update album list
     */
    public void updateAlbums() {
        for (Song song : allSongs.getSongs()) {
            // add album if not existing
            if (!allSongs.getAlbums().contains(song.getAlbum())) {
                allSongs.getAlbums().add(song.getAlbum());
            }
        }
    }

    /**
     * OnClick method for song list item
     */
    public void songPicked(View view) {
        musicService.setSongPosition(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }

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
                        SongListFragment songListFragment = new SongListFragment();
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

    /**
     * OnClick method for play/pause button
     */
    public void playPause(View view) {
        if (isPlaying()) {
            pause();
            paused = true;
        } else if (paused && !isPlaying()) {
            resume();
            paused = false;
        } else if (!paused && !isPlaying()) {
            start();
            started = true;
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
     * OnClick methods for next and previous buttons
     */
    public void playNext(View view) {
        musicService.playNext();
    }

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
     * Utilities
     */
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getDuration();
        } else {
            return 0;
        }
    }

    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getSongPosition();
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
