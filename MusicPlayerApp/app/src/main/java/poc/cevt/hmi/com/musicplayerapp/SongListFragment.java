package poc.cevt.hmi.com.musicplayerapp;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SongListFragment extends Fragment {

    public SongListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

        ListView songListView = (ListView) view.findViewById(R.id.songListView);
        SongAdapter songAdapter = new SongAdapter(getContext(), MainActivity.allSongs.getSongs());
        songListView.setAdapter(songAdapter);

        return view;
    }
}
