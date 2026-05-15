package com.pos.demosdk;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pos.demosdk.EMV.EMVActivity;
import com.pos.demosdk.EMV.EMVUtils;
import com.pos.demosdk.commomsdk.CommonSDKActivity;
import com.pos.demosdk.osapi.OSAPIActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);


        findViewById(R.id.emv_service_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EMVActivity.class));
            }
        });

        findViewById(R.id.common_sdk_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CommonSDKActivity.class));
            }
        });

        findViewById(R.id.osapi_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, OSAPIActivity.class));
            }
        });



    }
}