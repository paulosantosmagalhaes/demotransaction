package com.pos.demosdk.commomsdk.ic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.R;

public class IccActivityNew extends BaseActivity {
    TextView tv_name,tv_version;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.iccard_main_new);

        tv_name = findViewById(R.id.tv_name);
        tv_name.setText(getString(R.string.ic_service));
        tv_version=findViewById(R.id.tv_version);
        tv_version.setVisibility(View.GONE);


        OnClickListener listener = new OnClickListener() {


            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sle4442_btn:
                        Intent intent4442 = new Intent(IccActivityNew.this, SLE4442Activity.class);
                        startActivity(intent4442);

                        break;

                    case R.id.sle4428_btn:
                        Intent intent4428 = new Intent(IccActivityNew.this, SLE4428Activity.class);
                        startActivity(intent4428);

                        break;


                    case R.id.smartcard:
                        Intent smartcardintent = new Intent(IccActivityNew.this, SmarCardActivity.class);
                        startActivity(smartcardintent);
                        break;

                }
            }
        };
        Button sle4442_btn = (Button) findViewById(R.id.sle4442_btn);
        sle4442_btn.setOnClickListener(listener);

        Button sle4428_btn = (Button) findViewById(R.id.sle4428_btn);
        sle4428_btn.setOnClickListener(listener);


        Button smart_btn = (Button) findViewById(R.id.smartcard);
        smart_btn.setOnClickListener(listener);


    }


}
