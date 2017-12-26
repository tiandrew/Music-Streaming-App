package poc.cevt.hmi.com.musicplayerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;

import static poc.cevt.hmi.com.musicplayerapp.MainActivity.allSongs;
import static poc.cevt.hmi.com.musicplayerapp.MainActivity.queue;
import static poc.cevt.hmi.com.musicplayerapp.MainActivity.songPicked;

public class SongListFragment extends Fragment {
    QueueAdapter queueAdapter = null;

    // required empty constructor
    public SongListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

        getActivity().registerReceiver(queueUpdate, new IntentFilter(Const.QUEUE));

        String showSongs = this.getArguments().getString(Const.SONGS);
        String showArtist = this.getArguments().getString(Const.ARTISTS);
        String showAlbum = this.getArguments().getString(Const.ALBUMS);
        String showQueue = this.getArguments().getString(Const.QUEUE);

        final ListView songListView = (ListView) view.findViewById(R.id.songListView);
        SongAdapter songAdapter;
        if (showSongs != null) { // show all songs
            songAdapter = new SongAdapter(getContext(), allSongs.getSongs());
            songListView.setAdapter(songAdapter);
        } else if (showArtist != null) { // show songs by certain artist
            songAdapter = new SongAdapter(getContext(), allSongs.getArtistSongs(showArtist));
            songListView.setAdapter(songAdapter);
        } else if (showAlbum != null) { // show songs by certain album
            songAdapter = new SongAdapter(getContext(), allSongs.getAlbumSongs(showAlbum));
            songListView.setAdapter(songAdapter);
        } else if (showQueue != null) { // show songs in queue
            queueAdapter = new QueueAdapter(getContext(), queue.getQueue());
            songListView.setAdapter(queueAdapter);
        }

        songListView.setClickable(true);
        return view;
    }

    /**
     * Update queue if song is played
     */
    private BroadcastReceiver queueUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (queueAdapter != null)
                queueAdapter.notifyDataSetChanged();
        }
    };
}
