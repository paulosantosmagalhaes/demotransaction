package com.pos.demosdk.commomsdk.scanner;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Toast;

import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;
import com.pos.demosdk.databinding.ActivitySoftdecodereaderBinding;

public class SoftDecodeReaderActivity extends BaseActivity {

    private static final String TAG = "SoftDecodeReaderActivity";
    private int successCount;
    private ActivitySoftdecodereaderBinding binding;
    private final int SOFTCODE = 0x124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivitySoftdecodereaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        successCount = 0;

        initView();
    }

    private void initView() {
        binding.openBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClassName("com.telpo.tps550.api", "com.telpo.tps550.api.barcode.Capture");
            try {
                startActivityForResult(intent, SOFTCODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, getResources().getString(R.string.identify_fail), Toast.LENGTH_LONG).show();
            }
        });
        binding.closeBtn.setVisibility(View.GONE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SOFTCODE) {
            if (resultCode == 0) {
                if (data != null) {
                    playBeepSound();

                    String qrcode = data.getStringExtra("qrCode");
                    String msg = "\n\n[" + ++successCount + "]\n" + qrcode;
                    Log.d(TAG, "onScanSuccess: " + msg);
                    binding.dataShow.append(msg);
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            binding.scrollViewData.fullScroll(ScrollView.FOCUS_DOWN);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
                }
            } else {
                Toast.makeText(this, "Scan Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void playBeepSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 无需关闭 MediaPlayer，已在播放完成时释放
    }
}