package com.pos.demosdk.commomsdk;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.EMV.EMVActivity;
import com.pos.demosdk.MainActivity;
import com.pos.demosdk.R;
import com.pos.demosdk.commomsdk.buzzer.BuzzerActivity;
import com.pos.demosdk.commomsdk.ic.IccActivityNew;
import com.pos.demosdk.commomsdk.magneticstripecard.MegneticActivity;
import com.pos.demosdk.commomsdk.nfc.NFCActivity;
import com.pos.demosdk.commomsdk.powercontrol.PowerControlActivity;
import com.pos.demosdk.commomsdk.printer.PrintActivity;
import com.pos.demosdk.commomsdk.printer.SetTextFontActivity;
import com.pos.demosdk.commomsdk.psam.PsamCardActivity;
import com.pos.demosdk.commomsdk.scanner.DecodeReaderActivity;
import com.pos.demosdk.commomsdk.scanner.SoftDecodeReaderActivity;
import com.pos.demosdk.commomsdk.speaker.SpeakerActivity;
import com.pos.demosdk.commomsdk.subscreen.SubScreenActivity;

public class CommonSDKActivity extends BaseActivity {

    TextView tv_name,tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commonsdk_main);

        tv_name=findViewById(R.id.tv_name);
        tv_name.setText("COMMON SDK");

        findViewById(R.id.printer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, PrintActivity.class));
            }
        });

        findViewById(R.id.printerfont_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, SetTextFontActivity.class));
            }
        });
        findViewById(R.id.ic_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, IccActivityNew.class));
            }
        });
        findViewById(R.id.psam_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, PsamCardActivity.class));
            }
        });
        findViewById(R.id.mag_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, MegneticActivity.class));
            }
        });
        findViewById(R.id.nfc_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, NFCActivity.class));
            }
        });

        findViewById(R.id.qr_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //    startActivity(new Intent(CommonSDKActivity.this, QrActivity.class)); //不用系统
                AlertDialog.Builder dialog = new AlertDialog.Builder(CommonSDKActivity.this);
                dialog.setTitle(getString(R.string.scanner_dialog_title));
                dialog.setMessage(getString(R.string.scanner_dialog_msg));
                dialog.setNegativeButton(getString(R.string.scanner_dialog_soft_decoding), (dialogInterface, i) -> {
                    // 软读头

                        startActivity(new Intent(CommonSDKActivity.this, SoftDecodeReaderActivity.class));

                });
                dialog.setPositiveButton(getString(R.string.scanner_dialog_hard_decoding), (dialogInterface, i) -> {
                    // 硬读头
                    startActivity(new Intent(CommonSDKActivity.this, DecodeReaderActivity.class));
                });
                dialog.show();

            }
        });
        findViewById(R.id.power_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, PowerControlActivity.class));
            }
        });
        findViewById(R.id.buzzer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, SpeakerActivity.class));
            }
        });

        findViewById(R.id.subscreen_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CommonSDKActivity.this, SubScreenActivity.class));
            }
        });


    }
}