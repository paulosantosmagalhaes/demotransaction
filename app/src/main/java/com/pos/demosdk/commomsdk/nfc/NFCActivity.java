package com.pos.demosdk.commomsdk.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.common.apiutil.powercontrol.PowerControl;
import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/3/28
 * <p>
 * Description:
 */
public class NFCActivity extends BaseActivity {
    TextView tv_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText("NFC Service");
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cpu_btn:
                Intent intentCPU = new Intent(NFCActivity.this, NfcActivity_tps900.class);
                startActivity(intentCPU);
                break;

            case R.id.mifare_desfire_btn:
                Intent intentMifareDefire = new Intent(NFCActivity.this, MifareDesfireActivity.class);
                startActivity(intentMifareDefire);
                break;

            case R.id.felica_btn:
                Intent intentFelica = new Intent(NFCActivity.this, FelicaActivity.class);
                startActivity(intentFelica);
                break;

        }
    }

}
