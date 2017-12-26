package poc.cevt.hmi.com.musicplayerapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import static poc.cevt.hmi.com.musicplayerapp.MainActivity.allSongs;

public class ListFragment extends Fragment {

    ListView listView;
    public ListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String type = getArguments().getString(Const.TYPE);
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        listView = (ListView) view.findViewById(R.id.itemListView);

        if (type == null) { return view; }

        switch (type) {
            case Const.ALBUMS: {
                StringListAdapter adapter = new StringListAdapter(getContext(), allSongs.getAlbums());
                listView.setAdapter(adapter);

                setItemClickListeners(Const.ALBUMS);

                break;
            }
            case Const.ARTISTS: {
                StringListAdapter adapter = new StringListAdapter(getContext(), allSongs.getArtists());
                listView.setAdapter(adapter);

                setItemClickListeners(Const.ARTISTS);
                break;
            }
//            case Const.PLAYLISTS: {
//                StringListAdapter adapter = new StringListAdapter(getContext(), allSongs.getPlaylists());
//                listView.setAdapter(adapter);
//
//                setItemClickListeners(Const.PLAYLISTS);
//                break;
//            }
        }
        return view;
    }


    void setItemClickListeners(final String list) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                Bundle bundle = new Bundle();

                switch (list) {
                    case Const.ALBUMS: {
                        // TODO: open all songs in that album
                        bundle.putString(Const.ALBUMS, parent.getItemAtPosition(position).toString());
                        SongListFragment songListFragment = new SongListFragment();
                        songListFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, songListFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    }
                    case Const.ARTISTS: {
                        // TODO: open all songs with that artist
                        bundle.putString(Const.ARTISTS, parent.getItemAtPosition(position).toString());
                        SongListFragment songListFragment = new SongListFragment();
                        songListFragment.setArguments(bundle);
                        ft.replace(R.id.fragmentLayout, songListFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                        break;
                    }
                    case Const.PLAYLISTS: {
                        // TODO: open all songs in that playlist
                        break;
                    }
                }
            }
        });
    }
}
