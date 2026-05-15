package com.pos.demosdk.commomsdk.subscreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.common.apiutil.ResultCode;
import com.common.apiutil.lcd.SimpleSubLcd;
import com.common.apiutil.powercontrol.PowerControl;
import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/08/06
 * <p>
 * Description:
 */
public class SubScreenActivity extends BaseActivity {
    TextView tv_name;

    private SimpleSubLcd mSmileLCDUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscreen);
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText("SubScreen Service");

        mSmileLCDUtil = new SimpleSubLcd(this);
        new Thread(() -> mSmileLCDUtil.init()).start();

        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.sub_screen_test);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.image);
        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.mipmap.image1);
        Bitmap bitmap4 = BitmapFactory.decodeResource(getResources(), R.mipmap.image2);

        Log.e("yw_bitmap1", bitmap1.getWidth() + "x" + bitmap1.getHeight());
        Log.e("yw_bitmap2", bitmap2.getWidth() + "x" + bitmap2.getHeight());
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mSmileLCDUtil.show(bitmap1);
                if (result == ResultCode.SUCCESS) {
                    Toast.makeText(SubScreenActivity.this, "Display successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubScreenActivity.this, "Display failed: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mSmileLCDUtil.show(bitmap2);
                if (result == ResultCode.SUCCESS) {
                    Toast.makeText(SubScreenActivity.this, "Display successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubScreenActivity.this, "Display failed: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mSmileLCDUtil.show(bitmap3);
                if (result == ResultCode.SUCCESS) {
                    Toast.makeText(SubScreenActivity.this, "Display successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubScreenActivity.this, "Display failed: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mSmileLCDUtil.show(bitmap4);
                if (result == ResultCode.SUCCESS) {
                    Toast.makeText(SubScreenActivity.this, "Display successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubScreenActivity.this, "Display failed: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSmileLCDUtil.release();
    }


}
