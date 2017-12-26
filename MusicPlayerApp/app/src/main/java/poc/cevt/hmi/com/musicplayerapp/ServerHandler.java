package poc.cevt.hmi.com.musicplayerapp;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerHandler extends AsyncTask<String, Void, String> {

    private OkHttpClient client = new OkHttpClient();

    private String getSongs() throws IOException {

        Request request = new Request.Builder()
                .url("http://" + "172.31.113.22" + ":8080/api/music/getSongs")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return getSongs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String json) {
        System.out.println("Test" + json);
        Gson gson = new Gson();

        if(null != json) {
            MainActivity.allSongs.setSongList(gson.fromJson(json, ArrayList.class));
        }
        super.onPostExecute(json);
    }
}
