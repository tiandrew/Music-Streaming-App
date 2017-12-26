package poc.cevt.hmi.com.musicplayerapp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class AllSongs {

    @SerializedName("songList")
    private ArrayList<Song> songList = new ArrayList<>();

    // current song and previous song will need to be moved to a separate user
    // unique class object once more than one client application exists
    private Song currentSong;

    private Song prevSong;

    ArrayList<Song> getSongs() {
        return songList;
    }

    void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    void setPrevSong(Song prevSong) {
        this.prevSong = prevSong;
    }

    Song getCurrentSong() {
        return currentSong;
    }

    Song getPrevSong() {
        return prevSong;
    }

    ArrayList<String> getArtists() {
        ArrayList<String> artists = new ArrayList<>();

        // get all artists
        for (Song song : songList) {
            if (!artists.contains(song.getArtist())) {
                artists.add(song.getArtist());
            }
        }
        // sort alphabetically
        Collections.sort(artists.subList(1, artists.size()));

        return artists;
    }

    ArrayList<String> getAlbums() {
        ArrayList<String> albums = new ArrayList<>();

        // get all albums
        for (Song song : songList) {
            if (!albums.contains(song.getAlbum())) {
                albums.add(song.getAlbum());
            }
        }
        // sort alphabetically
        Collections.sort(albums.subList(1, albums.size()));

        return albums;
    }

    ArrayList<Song> getArtistSongs(String artist) {
        ArrayList<Song> songs = new ArrayList<>();

        for (Song song : songList) {
            if (song.getArtist() == artist) {
                songs.add(song);
            }
        }

        // sort by title
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });

        return songs;
    }

    ArrayList<Song> getAlbumSongs(String album) {
        ArrayList<Song> songs = new ArrayList<>();

        for (Song song : songList) {
            if (song.getAlbum() == album) {
                songs.add(song);
            }
        }

        // sort by title
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });

        return songs;
    }
}
