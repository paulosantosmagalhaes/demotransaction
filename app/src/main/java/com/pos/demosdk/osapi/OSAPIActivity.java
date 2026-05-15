package com.pos.demosdk.osapi;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.common.osapi.OSApi;
import com.android.common.osapi.impl.IDeviceInfoControl;
import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.EMV.EMVActivity;
import com.pos.demosdk.MainActivity;
import com.pos.demosdk.R;
import com.pos.demosdk.commomsdk.CommonSDKActivity;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/08/06
 * <p>
 * Description:
 */
public class OSAPIActivity extends BaseActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osapi);

        textView=findViewById(R.id.tv_text);

        OSApi osApi = new OSApi(this);
        IDeviceInfoControl control = osApi.getDeviceInfoControl();
        String SN=control.getSerialNumber();

        String IMEI=control.getIMEI(0);
        String FirmwareVersion=control.getBuildNumber();

        textView.setText("SN:"+SN+"\n"+"IMEI:"+IMEI+"\n"+"FirmwareVersion:"+FirmwareVersion);

    }


}
