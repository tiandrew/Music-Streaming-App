package poc.cevt.hmi.com.musicplayerapp;

import com.google.gson.annotations.SerializedName;

class Song {

    private long id;

    @SerializedName("title")
    private String title;

    private String artist;

    private String album;

    // TODO: Delete setters after finished
    void setTitle(String title) {
        this.title = title;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    void setAlbum(String album) {
        this.album = album;
    }

    public long getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    String getAlbum() { return album; }

    Song(long id, String title, String artist, String album) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }
}
