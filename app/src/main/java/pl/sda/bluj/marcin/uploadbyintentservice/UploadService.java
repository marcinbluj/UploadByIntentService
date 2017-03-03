package pl.sda.bluj.marcin.uploadbyintentservice;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UploadService extends IntentService {

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(MainActivity.UPLOAD_ACTION)) {
                String stringUri = intent.getStringExtra(MainActivity.URI_EXTRA);
                Uri uri = Uri.parse(stringUri);
                uploadFile(uri);
            }
        }
    }

    private void uploadFile(Uri uri) {
        Request.Builder builder = new Request.Builder();
        builder.url("https://content.dropboxapi.com/2/files/upload");
        builder.addHeader("Authorization",
                "Bearer OBXA9Bb7b9AAAAAAAAAAJJYJQsN3J6fLaYvhOs-EDLdPFooFh1s_04954qA0lizS");
        builder.addHeader("Content-Type", "application/octet-stream");
        String lastPathSegment = uri.getLastPathSegment();
        builder.addHeader("Dropbox-API-Arg", "{\"path\":\"/" + lastPathSegment + ".jpg\"}");
        builder.post(new StreamRequestBody(getApplicationContext(), uri));
        Request request = builder.build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("TEST", "fail", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("TEST", "onResponse " + response.body().string());
                broadcastResult();
            }
        });
    }

    private void broadcastResult() {
        Intent intent = new Intent();
        intent.setAction(MainActivity.RESULT_ACTION);
        intent.putExtra(MainActivity.STATE_EXTRA, true);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(intent);
    }
}
