package com.example.audiotester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Spinner mModeSpinner;
    private Spinner mMainSpinner;
    private Button mActionButton;
    private SwitchCompat mSpeakerphoneSwitch;

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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initializeMediaTester();
        initializeSpinners();
        initializeActionButton();
        initializeSwitch();

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

    @RequiresApi(api = Build.VERSION_CODES.R)
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private int getSelectedMode() {
        switch (mModeSpinner.getSelectedItem().toString()) {
            case "In call mode":
                return AudioManager.MODE_IN_CALL;
            case "In communication mode":
                return AudioManager.MODE_IN_COMMUNICATION;
            case "Current mode":
                return AudioManager.MODE_CURRENT;
            case "Call screening mode":
                return AudioManager.MODE_CALL_SCREENING;
            case "Ringtone mode":
                return AudioManager.MODE_RINGTONE;
            case "Invalid mode":
                return AudioManager.MODE_INVALID;
            default:
                return AudioManager.MODE_NORMAL;
        }

    }

    private int getSelectedOutputAudioSource() {
        return mMainSpinner.getSelectedItem()
                .toString()
                .equals("Receiver") ? AudioHelper.TYPE_RECEIVER : AudioHelper.TYPE_SPEAKER;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private int getSelectedInputAudioSource() {
        switch (mMainSpinner.getSelectedItem().toString()) {
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

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setAudioOutTestMode() throws IOException {
        mMediaTester.switchToAudioOutTestMode(mActionButton);

        mSpeakerphoneSwitch.setVisibility(View.GONE);
        mActionButton.setText(R.string.play);
        mActionButton.setOnClickListener((view) -> mMediaTester.playAudio(view, getSelectedOutputAudioSource(), getSelectedMode()));

        mMainSpinner.setAdapter(mOutputAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setAudioInTestMode() {
        mMediaTester.switchToAudioInTestMode(mActionButton);

        mSpeakerphoneSwitch.setVisibility(View.VISIBLE);
        mActionButton.setText(R.string.record);
        mActionButton.setOnClickListener((view) -> mMediaTester.recordMic(mActionButton, getSelectedInputAudioSource(), getSelectedMode(), mSpeakerphoneSwitch.isChecked()));

        mMainSpinner.setAdapter(mInputAdapter);
    }

    private void initializeMediaTester() {
        mMediaTester = new MediaTester(this);
    }

    private void initializeSpinners() {
        mMainSpinner = findViewById(R.id.mainSpinner);
        mModeSpinner = findViewById(R.id.modeSpinner);

        mOutputAdapter = ArrayAdapter.createFromResource(this,
                R.array.outputs_array, android.R.layout.simple_spinner_item);
        mOutputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mInputAdapter = ArrayAdapter.createFromResource(this,
                R.array.inputs_array, android.R.layout.simple_spinner_item);
        mInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(this,
                R.array.modes_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mModeSpinner.setAdapter(modeAdapter);
    }

    private void initializeSwitch() {
        mSpeakerphoneSwitch = findViewById(R.id.speakerphoneSwitch);
    }

    private void initializeActionButton() {
        mActionButton = findViewById(R.id.actionButton);
    }
}