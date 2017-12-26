package poc.cevt.hmi.com.musicplayerapp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

class AllSongs {

    @SerializedName("songList")
    private ArrayList<Song> songList = new ArrayList<>();

    private ArrayList<Song> queue = new ArrayList<>();

    private ArrayList<String> playlists = new ArrayList<>();

    private ArrayList<String> albums = new ArrayList<>();

    private ArrayList<String> artists = new ArrayList<>();

    static long currentId;

    static String currentTitle;

    static String currentArtist;

    ArrayList<Song> getSongs() {
        return songList;
    }

    ArrayList<String> getPlaylists() {
        return playlists;
    }

    ArrayList<String> getAlbums() {
        return albums;
    }

    ArrayList<String> getArtists() {
        return artists;
    }

    void addSong(Song song) {
        if (!songList.contains(song)) {
            songList.add(song);
        }
    }

    void setSongList(ArrayList<Song> list) {
        songList = list;
    }
}
