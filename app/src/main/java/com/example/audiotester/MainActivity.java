package com.example.audiotester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Spinner mSpinner;
    private Button mActionButton;

    private MediaTester mMediaTester;
    private ArrayAdapter<CharSequence> mOutputAdapter;
    private ArrayAdapter<CharSequence> mInputAdapter;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initializeMediaTester();
        initializeSpinner();
        initializeActionButton();

        try {
            setAudioOutTestMode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.audioInMenuItem) {
            if (mMediaTester.getTestMode() == MediaTester.TEST_IN) {
                return false;
            }

            setAudioInTestMode();
            mMediaTester.setTestMode(MediaTester.TEST_IN);

            return true;
        } else if (item.getItemId() == R.id.audioOutMenuItem) {
            if (mMediaTester.getTestMode() == MediaTester.TEST_OUT) {
                return false;
            }

            try {
                setAudioOutTestMode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mMediaTester.setTestMode(MediaTester.TEST_OUT);

            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) finish();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaTester.release();
    }


    private int getSelectedOutputAudioSource() {
        return mSpinner.getSelectedItem()
                .toString()
                .equals("Receiver") ? AudioHelper.TYPE_RECEIVER : AudioHelper.TYPE_SPEAKER;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private int getSelectedInputAudioSource() {
        switch (mSpinner.getSelectedItem().toString()) {
            case "Camcorder":
                return MediaRecorder.AudioSource.CAMCORDER;
            case "Mic":
                return MediaRecorder.AudioSource.MIC;
            case "Remote Submix":
                return MediaRecorder.AudioSource.REMOTE_SUBMIX;
            case "Unprocessed":
                return MediaRecorder.AudioSource.UNPROCESSED;
            case "Voice call":
                return MediaRecorder.AudioSource.VOICE_CALL;
            case "Voice communication":
                return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
            case "Voice downlink":
                return MediaRecorder.AudioSource.VOICE_DOWNLINK;
            case "Voice performance":
                return MediaRecorder.AudioSource.VOICE_PERFORMANCE;
            case "Voice recognition":
                return MediaRecorder.AudioSource.VOICE_RECOGNITION;
            case "Voice uplink":
                return MediaRecorder.AudioSource.VOICE_UPLINK;
            default:
                return MediaRecorder.AudioSource.DEFAULT;
        }
    }

    private void setAudioOutTestMode() throws IOException {
        mMediaTester.switchToAudioOutTestMode(mActionButton);

        mActionButton.setText(R.string.play);
        mActionButton.setOnClickListener((view) -> mMediaTester.playAudio(view, getSelectedOutputAudioSource()));

        mSpinner.setAdapter(mOutputAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setAudioInTestMode() {
        mMediaTester.switchToAudioInTestMode(mActionButton);

        mActionButton.setText(R.string.record);
        mActionButton.setOnClickListener((view) -> mMediaTester.recordMic(mActionButton, getSelectedInputAudioSource()));

        mSpinner.setAdapter(mInputAdapter);
    }

    private void initializeMediaTester() {
        mMediaTester = new MediaTester(this);
    }

    private void initializeSpinner() {
        mSpinner = findViewById(R.id.spinner);

        mOutputAdapter = ArrayAdapter.createFromResource(this,
                R.array.outputs_array, android.R.layout.simple_spinner_item);
        mOutputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mInputAdapter = ArrayAdapter.createFromResource(this,
                R.array.inputs_array, android.R.layout.simple_spinner_item);
        mInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void initializeActionButton() {
        mActionButton = findViewById(R.id.actionButton);
    }
}