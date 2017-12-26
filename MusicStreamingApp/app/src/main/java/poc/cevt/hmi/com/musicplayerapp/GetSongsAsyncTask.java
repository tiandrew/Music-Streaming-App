package poc.cevt.hmi.com.musicplayerapp;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class GetSongsAsyncTask extends AsyncTask<String, Void, String> {
    private OkHttpClient client = new OkHttpClient();

    /**
     * Gets a list of all songs in source in the form of a JsonString
     */
    private String getSongs() throws IOException {
        Request request = new Request.Builder()
                .url("http://" + Const.IP + ":8080/api/music/getSongs")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Calls getSongs() method
     */
    @Override
    protected String doInBackground(String... params) {
        try {
            return getSongs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets all songs to source's songs
     */
    @Override
    protected void onPostExecute(String jsonString) {
        if (jsonString != null) {
            MainActivity.allSongs = new Gson().fromJson(jsonString, AllSongs.class);
            // sort songs alphabetically
            Collections.sort(MainActivity.allSongs.getSongs(), new Comparator<Song>() {
                // custom Comparator
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
        }

        super.onPostExecute(jsonString);
    }
}
