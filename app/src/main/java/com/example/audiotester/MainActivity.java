package com.example.audiotester;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mHeadsetMediaPlayer;
    private MediaPlayer mSpeakerMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeMediaPlayers();
        initializeButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.audioInMenuItem:
                // User chose the "Settings" item, show the app settings UI...

                return true;

            case R.id.audioOutMenuItem:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void playAudioHeadset(View view) {
        if (mHeadsetMediaPlayer.isPlaying()) {
            mHeadsetMediaPlayer.stop();
            try {
                mHeadsetMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        mHeadsetMediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void playAudioSpeaker(View view) {
        if (mSpeakerMediaPlayer.isPlaying()) {
            mSpeakerMediaPlayer.stop();
            try {
                mSpeakerMediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        mSpeakerMediaPlayer.start(); // no need to call prepare(); create() does that for you
    }

    private void initializeMediaPlayers() {
        mHeadsetMediaPlayer = MediaPlayer.create(this, R.raw.rickroll);
        mSpeakerMediaPlayer = MediaPlayer.create(this, R.raw.rickroll);
    }

    private void initializeButtons() {
        Button firstButton = findViewById(R.id.firstActionButton);
        Button secondButton = findViewById(R.id.secondActionButton);

        firstButton.setText(R.string.play_headset);
        secondButton.setText(R.string.play_speaker);

        firstButton.setOnClickListener(this::playAudioHeadset);
        secondButton.setOnClickListener(this::playAudioSpeaker);
    }
}