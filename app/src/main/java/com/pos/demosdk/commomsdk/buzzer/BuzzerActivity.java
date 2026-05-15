package com.pos.demosdk.commomsdk.buzzer;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;

import java.util.HashMap;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/1/4
 * <p>
 * Description:buzzer
 */
public class BuzzerActivity extends BaseActivity {
    TextView tv_name;
    private SoundPool soundPool;
    private HashMap<Integer, Integer> spMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzzer);
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText("Buzzer Service");

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        spMap = new HashMap<Integer, Integer>();

         spMap.put(1, soundPool.load(this, R.raw.beep, 1));
         spMap.put(2, soundPool.load(this, R.raw.alert, 1));

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSounds(1,0);
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSounds(2,0);
            }
        });
    }


    public void playSounds(int sound, int number) {

        AudioManager am = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = audioCurrentVolumn / audioMaxVolumn;

        soundPool.play(spMap.get(sound), volumnRatio, volumnRatio, 1, number, 1);


    }
}
