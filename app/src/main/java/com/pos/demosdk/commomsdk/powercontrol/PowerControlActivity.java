package com.pos.demosdk.commomsdk.powercontrol;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.common.apiutil.powercontrol.PowerControl;
import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/3/28
 * <p>
 * Description:
 */
public class PowerControlActivity extends BaseActivity {
    TextView tv_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_powercontrol);
        tv_name = findViewById(R.id.tv_name);
        tv_name.setText("PowerControl Service");

        PowerControl powerControl = new PowerControl(this);



        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerControl.usbPower(1);//  USB上电  900上面指纹仪
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerControl.usbPower(0);//USB下电  900上面指纹仪
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerControl.idBoxPowerCtl(1); // IDBox上电
                //   idBoxPowerCtl(1);
            }
        });

        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //idBoxPowerCtl(0);
                powerControl.idBoxPowerCtl(0); // IDBox下电
            }
        });
    }

    /*
     *参数：status：上电状态 1：上电 0：下电
     */
    public synchronized int idBoxPowerCtl(int status)
    {
        int ret;
        Class<?> pinpad = null;
        Method method = null;
        Object obj    = null;
        int result = -1;

        try {
            pinpad = Class.forName("com.common.sdk.power.PowerControlServiceManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        obj =  getSystemService("PowerControl");
        try {
            method = pinpad.getMethod("idBoxPowerCtl",
                    new Class[]{int.class});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            result = (Integer) method.invoke(obj,status);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }


}
