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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

import static poc.cevt.hmi.com.musicplayerapp.MainActivity.convertedTime;
import static poc.cevt.hmi.com.musicplayerapp.MainActivity.queue;
import static poc.cevt.hmi.com.musicplayerapp.MainActivity.songPicked;

class QueueAdapter extends BaseAdapter{
    private LayoutInflater songInflater;
    private ArrayList<Song> songs;

    // constructor
    QueueAdapter(Context context, ArrayList<Song> songs) {
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
        final LinearLayout songL = (LinearLayout) songInflater.inflate(R.layout.queue_item, parent, false);

        // get title view
        TextView songTitle = (TextView) songL.findViewById(R.id.songTitle);
        TextView songArtist = (TextView) songL.findViewById(R.id.songArtist);
        TextView songAlbum = (TextView) songL.findViewById(R.id.songAlbum);
        TextView songDuration = (TextView) songL.findViewById(R.id.songDuration);

        // dropdown menu for each item
        final Button remove = (Button) songL.findViewById(R.id.remove);
        songL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songPicked(songs.get(position));
            }
        });

        // listener for remove button
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // create an alert asking user to confirm the deletion
                AlertDialog.Builder alert = new AlertDialog.Builder(songL.getContext());
                alert.setMessage("Remove this song from queue?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete song
                        queue.getQueue().remove(getItem(position));
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
            }
        });

        // get song using position
        Song currentSong = songs.get(position);

        // get title and artist strings
        songTitle.setText(currentSong.getTitle());
        songArtist.setText(currentSong.getArtist());
        songAlbum.setText(currentSong.getAlbum());
        songDuration.setText(convertedTime(currentSong.getDuration()));

        // set position as tag
        songL.setTag(position);
        return songL;
    }

}
