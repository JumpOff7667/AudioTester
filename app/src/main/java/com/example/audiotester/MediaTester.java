package com.example.audiotester;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class MediaTester {

    public static final int TEST_OUT = 1;
    public static final int TEST_IN = 2;

    private final MediaPlayer mMediaPlayer;
    private final MediaRecorder mMediaRecorder;
    private final AudioHelper mAudioHelper;
    private final AppCompatActivity mActivityContext;

    private int mTestMode = TEST_OUT;
    private boolean mRecording = false;

    public MediaTester(AppCompatActivity context) {
        mActivityContext = context;

        mAudioHelper = new AudioHelper(context);
        mMediaPlayer = new MediaPlayer();
        mMediaRecorder = new MediaRecorder();

        Log.d("AudioTester", "MediaTester has been initialized");
    }

    public int getTestMode() {
        Log.d("AudioTester", "Current test mode: " + mTestMode);
        return mTestMode;
    }

    public void playAudio(View view, int selectedOutputSource) {
        if (mMediaPlayer == null) return;

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            ((Button) view).setText(R.string.play);

            Log.d("AudioTester", "Stopped playing audio");
            return;
        }

        mAudioHelper.setDestination(selectedOutputSource);
        try {
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            Log.d("AudioTester", "Started playing audio on "+ (selectedOutputSource == AudioHelper.TYPE_RECEIVER ? "handset" : "speaker"));
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void recordMic(View view, int selectedInputSource) {
        if (mMediaRecorder == null) return;

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();

            ((Button) view).setText(R.string.record);
            Log.d("AudioTester", "Stopped playing mic record");
            return;
        }

        if (mRecording) {
            mMediaRecorder.stop();
            Log.d("AudioTester", "Stopped recording mic");

            try {
                mMediaPlayer.setDataSource(mActivityContext.getFilesDir() + "/mic_record.mp4");
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                mRecording = false;
                ((Button) view).setText(R.string.playing);
                Log.d("AudioTester", "Started playing mic record");
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                mMediaPlayer.reset();

                Log.e("AudioTester", "MediaPlayer has been reset due to an exception");
            }

            return;
        }

        mMediaRecorder.setAudioSource(selectedInputSource);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(new File(mActivityContext.getFilesDir(), "mic_record.mp4"));
        Log.d("AudioTester", "MediaRecorder has been set up on input source " + selectedInputSource);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecording = true;
            ((Button) view).setText(R.string.stop);
            Log.d("AudioTester", "Started mic recording");
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            mMediaRecorder.reset();

            Log.e("AudioTester", "MediaRecorder has been reset due to an exception");
        }

    }

    public void release() {
        mMediaPlayer.release();
        mMediaRecorder.release();

        Log.d("AudioTester", "MediaPlayer and MediaRecorder have been released");
    }

    public void setTestMode(int testMode) {
        mTestMode = testMode;
        Log.d("AudioTester", "Changed test mode to " + (testMode == TEST_IN ? "TEST_IN" : "TEST_OUT"));
    }

    public void switchToAudioInTestMode(Button button) {
        mMediaPlayer.reset();

        mMediaPlayer.setOnCompletionListener((mediaPlayer) -> {
            mMediaPlayer.reset();
            button.setText(R.string.record);
        });

        mMediaPlayer.setOnPreparedListener((mediaPlayer) -> button.setText(R.string.playing));

        Log.d("AudioTester", "Switched to input test mode");
    }

    public void switchToAudioOutTestMode(Button button) throws IOException {
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(mActivityContext.getApplicationContext(), Uri.parse("android.resource://com.example.audiotester/" + R.raw.rickroll));

        mMediaPlayer.setOnCompletionListener((mediaPlayer) -> button.setText(R.string.play));

        mMediaPlayer.setOnPreparedListener((mediaPlayer) -> button.setText(R.string.stop));

        Log.d("AudioTester", "Switched to output test mode");
    }
}
