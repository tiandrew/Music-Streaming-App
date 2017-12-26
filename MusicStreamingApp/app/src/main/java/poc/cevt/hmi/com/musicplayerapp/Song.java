package poc.cevt.hmi.com.musicplayerapp;

import com.google.gson.annotations.SerializedName;

class Song {
    @SerializedName("track")
    private String track;

    @SerializedName("title")
    private String title;

    @SerializedName("artist")
    private String artist;

    @SerializedName("album")
    private String album;

    @SerializedName("duration")
    private long duration;

    @SerializedName("albumArt")
    private byte[] albumArt;

    String getTrack() {
        return track;
    }

    String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    String getAlbum() {
        return album;
    }

    long getDuration() {
        return duration;
    }

    byte[] getAlbumArt() {
        return albumArt;
    }
}
