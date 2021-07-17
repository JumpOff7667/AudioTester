package com.example.audiotester;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int TEST_OUT = 1;
    public static final int TEST_IN = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private AudioHelper mAudioHelper;

    private int mTestMode = TEST_OUT;
    private boolean mRecording = false;

    private Spinner mSpinner;
    private Button mActionButton;

    private ArrayAdapter<CharSequence> mOutputAdapter;
    private ArrayAdapter<CharSequence> mInputAdapter;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        initializeAudioHelper();
        initializeMediaPlayer();
        initializeMediaRecorder();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.audioInMenuItem) {
            if (mTestMode == TEST_IN) {
                return false;
            }

            setAudioInTestMode();
            mTestMode = TEST_IN;

            return true;
        } else if (item.getItemId() == R.id.audioOutMenuItem) {
            if (mTestMode == TEST_OUT) {
                return false;
            }

            try {
                setAudioOutTestMode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mTestMode = TEST_OUT;

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
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mMediaPlayer.release();
        mMediaRecorder.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void recordMic(View view) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();

            mActionButton.setText(R.string.record);

            return;
        }

        if (mRecording) {
            mMediaRecorder.stop();

            try {
                mMediaPlayer.setDataSource(this.getFilesDir() + "/mic_record.mp4");
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                mRecording = false;
                ((Button) view).setText(R.string.playing);
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                mMediaPlayer.reset();
            }

            return;
        }

        mMediaRecorder.setAudioSource(getInputAudioSource());
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(new File(this.getFilesDir(), "mic_record.mp4"));

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecording = true;
            ((Button) view).setText(R.string.stop);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            mMediaRecorder.reset();
        }

    }

    private int getOutputAudioSource() {
        return mSpinner.getSelectedItem()
                .toString()
                .equals("Receiver") ? AudioHelper.TYPE_RECEIVER : AudioHelper.TYPE_SPEAKER;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private int getInputAudioSource() {
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

    private void playAudio(View view) {
        mAudioHelper.setDestination(getOutputAudioSource());

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mActionButton.setText(R.string.play);
            return;
        }

        try {
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void setAudioOutTestMode() throws IOException {
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://com.example.audiotester/" + R.raw.rickroll));

        mMediaPlayer.setOnCompletionListener((mediaPlayer) -> mActionButton.setText(R.string.play));

        mMediaPlayer.setOnPreparedListener((mediaPlayer) -> mActionButton.setText(R.string.stop));

        mActionButton.setText(R.string.play);
        mActionButton.setOnClickListener(this::playAudio);

        mSpinner.setAdapter(mOutputAdapter);
    }

    private void setAudioInTestMode()  {
        mMediaPlayer.reset();

        mMediaPlayer.setOnCompletionListener((mediaPlayer) -> {
            mMediaPlayer.reset();
            mActionButton.setText(R.string.record);
        });

        mMediaPlayer.setOnPreparedListener((mediaPlayer) -> mActionButton.setText(R.string.playing));

        mActionButton.setText(R.string.record);
        mActionButton.setOnClickListener(this::recordMic);

        mSpinner.setAdapter(mInputAdapter);
    }

    private void initializeAudioHelper() {
        mAudioHelper = new AudioHelper(this);
    }

    private void initializeMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
    }

    private void initializeMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
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