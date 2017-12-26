package poc.cevt.hmi.com.musicplayerapp;

import java.util.ArrayList;

public class Queue {

    ArrayList<Song> queue = new ArrayList<>();

    public ArrayList<Song> getQueue() {
        return queue;
    }

    public void setQueue(ArrayList<Song> queue) {
        this.queue = queue;
    }

    public void addSong(Song song) {
        queue.add(song);
    }

    public void popFront() {
        queue.remove(0);
    }

    public void addFront(Song song) {
        queue.add(0, song);
    }
}
