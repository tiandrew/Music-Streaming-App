package poc.cevt.hmi.com.musicplayerapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

class SongAdapter extends BaseAdapter {

    private LayoutInflater songInflater;
    private ArrayList<Song> songs;

    SongAdapter(Context context, ArrayList<Song> songs) {
        this.songs = songs;
        songInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        final LinearLayout songL = (LinearLayout) songInflater.inflate(R.layout.song_list_item, parent, false);

        // get title and artist views
        TextView songTitle = (TextView) songL.findViewById(R.id.songTitle);
        TextView songArtist = (TextView) songL.findViewById(R.id.songArtist);
        TextView songAlbum = (TextView) songL.findViewById(R.id.songAlbum);

        // dropdown menu for each item
        final Button dropdown = (Button) songL.findViewById(R.id.dropdown);
        dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(songL.getContext(), dropdown);

                // menu options
                menu.getMenu().add("Add to queue");
                menu.getMenu().add("Add to playlist");
                menu.getMenu().add("Delete");

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Add to queue":
                                // TODO: implement a queue of songs
                                break;
                            case "Add to playlist":
                                // TODO: implement playlists
                                break;
                            case "Delete":
                                // create an alert asking user to confirm the deletion
                                AlertDialog.Builder alert = new AlertDialog.Builder(songL.getContext());
                                alert.setMessage("Remove this song?");
                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // delete song
                                        MainActivity.allSongs.getSongs().remove(getItem(position));
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }
                                });
                                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // don't delete song
                                        dialog.dismiss();
                                    }
                                });
                                alert.show();
                                break;
                        }
                        return false;
                    }
                });
                menu.show();
            }
        });

        // get song using position
        Song currentSong = songs.get(position);

        // get title and artist strings
        songTitle.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());
        songAlbum.setText(currentSong.getAlbum());

        // set position as tag
        songL.setTag(position);
        return songL;
    }
}
