package com.example.audiotester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.function.BiConsumer;

public class AudioHelper {

    private static final String TAG = "AudioHelper";

    public static final int TYPE_RECEIVER   = 1;
    public static final int TYPE_SPEAKER    = 2;

    AudioManager audioManager;

    HeadsetPlugReceiver headsetPlugReceiver;
    BiConsumer<AudioHelper, Integer> headsetPlugListener;

    NoisyAudioStreamReceiver noisyAudioStreamReceiver;
    Consumer<AudioHelper> noisyAudioListener;

    FragmentActivity context;

    public AudioHelper(FragmentActivity context) {
        this.context = context;

        this.audioManager = (AudioManager)(context.getSystemService(Context.AUDIO_SERVICE));
        context.getLifecycle().addObserver(new LifecycleListener());
    }

    public void setDestination(int type) {

        switch (type) {
            case TYPE_RECEIVER: {
                this.setDestination(AudioManager.MODE_IN_COMMUNICATION, false);
                break;
            }
            case TYPE_SPEAKER: {
                this.setDestination(AudioManager.MODE_NORMAL, true);
                break;
            }
        }
    }

    public void setMode(int mode) {

        // setMode directly
        audioManager.setMode(mode);
    }

    public int getMode() {
        return audioManager.getMode();
    }

    public void setSpeakerphoneOn(boolean value) {

        // setSpeakerphone directly
        audioManager.setSpeakerphoneOn(value);
    }

    public boolean isSpeakerphoneOn() {
        return audioManager.isSpeakerphoneOn();
    }

    public boolean isWiredHeadsetOn() {

        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);

        for (AudioDeviceInfo deviceInfo : audioDevices) {
            if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                return true;
            }

        }

        return false;
    }

    public void setOnHeadsetPlugListener(BiConsumer<AudioHelper, Integer> listener) {
        this.headsetPlugListener = listener;
    }

    public void setOnNoisyAudioListener(Consumer<AudioHelper> listener) {
        this.noisyAudioListener = listener;
    }

    void setDestination(int mode, boolean speaker) {

        // set params directly

        // must be first
        audioManager.setMode(mode);

        // and second
        audioManager.setSpeakerphoneOn(speaker);
    }

    private void registerHeadsetPlugReceiver(){
        if (headsetPlugReceiver == null) {
            headsetPlugReceiver = new HeadsetPlugReceiver();

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);

            context.registerReceiver(headsetPlugReceiver, filter);
        }
    }

    private void unregisterHeadsetPlugReceiver(){
        if (headsetPlugReceiver != null) {

            context.unregisterReceiver(headsetPlugReceiver);

            headsetPlugReceiver = null;
        }
    }

    private void registerNoisyAudioStreamReceiver(){
        if (noisyAudioStreamReceiver == null) {
            this.noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();

            IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

            context.registerReceiver(noisyAudioStreamReceiver, filter);
        }
    }

    private void unregisterNoisyAudioStreamReceiver(){
        if (noisyAudioStreamReceiver != null) {

            context.unregisterReceiver(noisyAudioStreamReceiver);

            this.noisyAudioStreamReceiver = null;
        }
    }

    /**
     *
     */
    private class LifecycleListener implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        void onCreate() {

            registerHeadsetPlugReceiver();
            registerNoisyAudioStreamReceiver();

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestroy() {

            unregisterHeadsetPlugReceiver();
            unregisterNoisyAudioStreamReceiver();

            context.getLifecycle().removeObserver(this);
        }
    }

    /**
     *
     */
    private class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String name = intent.getStringExtra("name");
            name = (name == null)? "": name;

            int state = intent.getIntExtra("state", 0);

            int microphone = intent.getIntExtra("microphone", 0);

            if (headsetPlugListener != null) {
                headsetPlugListener.accept(AudioHelper.this, state);
            }

            Log.w(TAG, name + ", state = " + state + ", microphone = " + microphone);
        }

    }

    /**
     *
     */
    private class NoisyAudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = AudioManager.ACTION_AUDIO_BECOMING_NOISY;

            if (action.equals(intent.getAction())) {

                if (noisyAudioListener != null) {
                    noisyAudioListener.accept(AudioHelper.this);
                }
            }
        }
    }
}

