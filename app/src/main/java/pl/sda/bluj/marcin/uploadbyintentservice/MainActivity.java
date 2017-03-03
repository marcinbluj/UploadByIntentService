package pl.sda.bluj.marcin.uploadbyintentservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final String UPLOAD_ACTION = "UPLOAD";
    public static final String URI_EXTRA = "URI";
    public static final String RESULT_ACTION = "RESULT";
    public static final String STATE_EXTRA = "STATE";
    Intent intent;
    LocalBroadcastManager localBroadcastManager;
    String uriString;

    @BindView(R.id.chosen_file_textview)
    TextView chosenFile;
    @BindView(R.id.upload_file_button)
    Button uploadFileButton;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean state = intent.getBooleanExtra(STATE_EXTRA, false);
            if (state) {
                Toast.makeText(getApplicationContext(), "File uploaded!", Toast.LENGTH_LONG).show();
                uploadFileButton.setEnabled(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();

        uploadFileButton.setEnabled(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(RESULT_ACTION);

        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(broadcastReceiver, filter);
    }

    private boolean checkPermissions() {
        int status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (status == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 0);
            return false;
        }
    }

    @OnClick(R.id.choose_file_button)
    public void chooseFile(View view) {
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileIntent.setType("image/jpeg");
        startActivityForResult(chooseFileIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        uriString = uri.toString();
        if (uriString != null && !uriString.isEmpty()) {
            chosenFile.setText(uriString);
            uploadFileButton.setEnabled(true);
        }
    }

    @OnClick(R.id.upload_file_button)
    public void uploadFile(View view) {
        if (uriString != null && !uriString.isEmpty()) {
            initializeIntent(UPLOAD_ACTION, uriString);
        } else {
            Toast.makeText(getApplicationContext(), "No file selected!", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeIntent(String action, String uri) {
        intent = new Intent(MainActivity.this, UploadService.class);
        intent.setAction(action);
        intent.putExtra(URI_EXTRA, uri);
        start(intent);
    }

    public void start(Intent intent) {
        startService(intent);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void stop(Intent intent) {
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        if (intent != null) {
            stop(intent);
        }
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
