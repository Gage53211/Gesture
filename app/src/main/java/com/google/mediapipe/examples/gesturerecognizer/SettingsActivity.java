package com.google.mediapipe.examples.gesturerecognizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "GestureSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Volume Control
        setupSlider(R.id.sbVolDelay, R.id.tvVolValue, "vol_delay", 500);

        // 2. App Navigation
        setupSlider(R.id.sbNextAppDelay, R.id.tvNavValue, "next_app_delay", 3000);

        // 3. Pause/Play
        setupSlider(R.id.sbPausePlayDelay, R.id.tvPausePlayValue, "gen_delay", 1500);

        // 4. Next Song
        setupSlider(R.id.sbNextSongDelay, R.id.tvNextSongValue, "next_delay", 1900);

        // 5. Previous Song
        setupSlider(R.id.sbPrevDelay, R.id.tvPrevSongValue, "prev_delay", 1500);
    }

    private void setupSlider(int sbId, int tvValueId, String prefKey, int defaultValue) {
        SeekBar seekBar = findViewById(sbId);
        TextView tvValue = findViewById(tvValueId);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int savedValue = prefs.getInt(prefKey, defaultValue);
        seekBar.setProgress(savedValue);
        tvValue.setText(savedValue + " ms");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the text in real-time as the user slides
                tvValue.setText(progress + " ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the value permanently when the user lets go
                prefs.edit().putInt(prefKey, seekBar.getProgress()).apply();
            }
        });
    }
}