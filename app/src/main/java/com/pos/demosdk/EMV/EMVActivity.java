package com.pos.demosdk.EMV;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.common.sdk.emv.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pos.demosdk.BaseActivity;
import com.pos.demosdk.EMV.ActivityMasterPaypass;
import com.pos.demosdk.EMV.ActivityVisaPayware;
import com.pos.demosdk.MainActivity;
import com.pos.demosdk.R;
import com.pos.demosdk.databinding.DetectDialogBinding;
import com.telpo.emv.EmvApp;
import com.telpo.emv.EmvCAPK;
import com.telpo.emv.EmvCountryCode;
import com.telpo.emv.EmvCurrencyCode;
import com.telpo.pinpad.PinParam;
import com.telpo.emv.EmvAmountData;
import com.telpo.emv.EmvCandidateApp;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvPinData;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvServiceListener;
import com.telpo.emv.EmvTLV;
import com.telpo.util.DefaultAPPCAPK;
import com.telpo.util.ErrMsg;
import com.telpo.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class EMVActivity extends BaseActivity {

    //log tag
    private String TAG = "EMVActivity";
    //The last result
    public int _LastCode = 0;

    public int pinresult = 0;
    //Payment amount
    public double amount = 0.00;
    //Display message buffer
    public StringBuffer DisplayBuf = new StringBuffer("");
    //context
    public Context context;
    //Pinpad service
    public com.common.sdk.emv.PinpadService pinpadService;
    // Emv service
    public EmvService emvService;

    public DetectDialogBinding detectDialogBinding;
    public EditText et_MsgView;
    public MaterialButton bt_confirm;


    //Process dialog
    public AlertDialog processDialog;
    //Show pan message dialog
    public AlertDialog panDialog;
    //Select app result
    public int selectAPPResult = 0;
    //whether the UI thread is running
    public boolean UIThreadisRunning = true;


    //PinKey index
    public int PIN_KEY_INDEX = 1;
    //PanKey index
    public int PAN_KEY_INDEX = 2;
    //macKey index
    public int MAC_KEY_INDEX = 3;
    //Dukpt's current KSN
    public String currentKSN = "";
    //PAN Dukpt's current KSN
    public String PanCurrentKSN = "";
    //Is the device initialized succ
    public boolean isDevInit = false;
    //MK/SK mode or not(false:DUKPT mode)
    public boolean isMkMode = false;
    //DES mode or not(fales:AES mode)
    public boolean isDesMode = false;
    //Is pan encryption in Des mode
    public boolean isPanDesMode = false;
    //is pan encryption in mk/sk mode
    public  boolean isPanMkMode = true;
    //PIN Block
    public String pinBlock = "";
    //PIN format（Des：0、1、3；Aes：4）
    //PAN
    public String cardNum = "";

    //Is online transaction or not
    public boolean isOnlineTransaction = false;
    //The signal to stop detect
    public boolean stopDetect = false;
    //Is NFC card
    public boolean isNFC = false;
    //Gson

    //Track1 data
    public String Track1 = null;
    //Track2 data
    public String Track2 = null;

    //Is Mag card
    public boolean isMag = false;
    //If no err ,go to pay
    public boolean isNoErr = true;
    //locker
    public ReentrantLock ThreadLock = new ReentrantLock();
    //terminal mode

    //
    public ActivityVisaPayware activityVisaPayware;
    public ActivityPBOC activityPBOC;
    public ActivityMasterPaypass activityMasterPaypass;
    public ActivityRupay activityRupay;
    public ActivityAmex activityAmex;
    public ActivityMir activityMir;
    public ActivityTransit activityTransit;

    public ActivityDiscover activityDiscover;

    public ActivityPure activityPure;

    public boolean isPureSwipeAgain = false;

    public boolean isDiscoverSwipeAgain = false;

    EmvServiceListener MyListener = new EmvServiceListener() {
        @Override
        public int onInputAmount(EmvAmountData emvAmountData) {
            emvAmountData.Amount = (long) (amount * 100);
            emvAmountData.TransCurrCode = 156;
            emvAmountData.ReferCurrCode = 156;// EmvCurrencyCode
            emvAmountData.TransCurrExp = 2;
            emvAmountData.ReferCurrExp = 2;
            emvAmountData.ReferCurrCon = 1;
            emvAmountData.CashbackAmount = 0;
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onInputPin(EmvPinData emvPinData) {

            Log.e("yw_emvPinData",emvPinData.toString());

            changePanUIVisibility(false, null);
            if(emvPinData.IsRetry==1){
                changeDialogText("Wrong Pin.Please input PIN");
            }else{
                changeDialogText("Please input PIN");
            }


            int result =0;
            String hidePan = cardNum.substring(0, 6) + "******" + cardNum.substring(cardNum.length() - 4, cardNum.length());
            changePanUIVisibility(true, "PAN:"+hidePan);
            PinpadBytesOut pinBlocktt = new PinpadBytesOut();
            if (emvPinData.type == EmvService.ONLIEN_ENCIPHER_PIN) {
                //online PIN


                pinresult = pinpadService.Pinpad_GetPin(PIN_KEY_INDEX, cardNum,  PinpadEnum.ENUM_PIN_BLOCK_FORMAT.ISO_9564_FORMAT_0, 6, 0, 60, pinBlocktt);
            } else {

                //offline PIN（Emv lib calls pinpad,application does not handle that）

                pinresult = pinpadService.Pinpad_GetPlainPin( 6, 0, 60, pinBlocktt);
                Log.i(TAG, "offline PIN!");
                changeDialogText("EMV Processing...");
               // return EmvService.EMV_TRUE;
            }

            if (pinresult == PinpadService.PIN_OK) {

                Log.e("yw","cardNum:"+cardNum+" pinBlock:"+com.telpo.util.StringUtil.bytesToHexString(pinBlocktt.outResult));
                emvPinData.Pin = pinBlocktt.outResult;
                result = EmvService.EMV_TRUE;
            }
            else if (pinresult == PinpadService.PIN_ERROR_PINLEN){ //bypass
                result = EmvService.ERR_NOPIN;
            }
            else if (pinresult == PinpadService.PIN_ERROR_CANCEL){
                result = EmvService.ERR_USERCANCEL;
            }
            else if (pinresult == PinpadService.PIN_ERROR_NOKEY){
                result = EmvService.ERR_KEY_NOT_FOUND;
            }
            else if(pinresult == PinpadService.PIN_ERROR_TIMEOUT){
                result = EmvService.ERR_TIMEOUT;
            }else{
                AppendDis("Get PIN Error:"+pinresult);
                isNoErr =false;
                result = EmvService.EMV_FALSE;
            }
            changeDialogText("EMV Processing...");
            return result;
        }

        @Override
        public int onSelectApp(EmvCandidateApp[] emvCandidateApps) {
            final EmvCandidateApp[] mAppList = emvCandidateApps;
            int appListLen = emvCandidateApps.length;
            selectAPPResult = 0;
            UIThreadisRunning = true;

            final String[] items = new String[appListLen];
            for (int i = 0; i < appListLen; i++) {
                items[i] = emvCandidateApps[i].appName;
            }

            new Handler(context.getMainLooper()).post(new Runnable() {/*Using UI thread resources to create dialog box*/
                @Override
                public void run() {
                    //The first UI application is selected by default
                    selectAPPResult = mAppList[0].index;
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                            .setTitle("Please Select App")
                            .setCancelable(false)
                            .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                //Set the radio list item, and select the first item by default (index is 0)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppendDis("callback [onSelectApp] You Select \"" + items[which] + "\"");
                                    AppendDis("callback [onSelectApp] It's appIndex is \"" + mAppList[which].index + "\"");
                                    selectAPPResult = mAppList[which].index;
                                }
                            })

                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UIThreadisRunning = false;
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UIThreadisRunning = false;
                                    selectAPPResult = EmvService.ERR_USERCANCEL;
                                }
                            });

                    builder.create().show();

                }
            });

            while (UIThreadisRunning) {
                //Waiting for user confirmation
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return selectAPPResult;
        }

        @Override
        public int onSelectAppFail(int i) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onFinishReadAppData() {
            int ret = 0;

            EmvTLV tlv;
           // EmvTLV tlv = new EmvTLV(0x9F06);
           // emvService.Emv_GetTLV(tlv);
         //   AppendDis("AID:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));

          //  tlv = new EmvTLV(0x9F1A);
          //  emvService.Emv_GetTLV(tlv);
          //  AppendDis("CountryCode:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));

            tlv = new EmvTLV(0x5A);
            ret = emvService.Emv_GetTLV(tlv);
            if(EmvService.EMV_TRUE == ret){
                cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
              //  AppendDis("0x5A:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));
            }else{
                tlv = new EmvTLV(0x57);
                ret = emvService.Emv_GetTLV(tlv);
                if(EmvService.EMV_TRUE == ret) {
                    String str_57 = StringUtil.bytesToHexString(tlv.Value);
                    cardNum = str_57.substring(0, str_57.indexOf('D'));
                //    AppendDis("0x57:" + com.telpo.util.StringUtil.bytesToHexString(tlv.Value));
                }else {
                  //  AppendDis("Get cardNum Fail");
                }
            }
            if(!(null == cardNum || cardNum.isEmpty())){
                AppendDis("cardNum:"+cardNum);
                AppendDis("encryptPan:" + encryptPan(cardNum));
            }

            List<EmvTLV> tagList = EMVUtils.getTLVCardDataTags();
            for (EmvTLV emvTLV : tagList) {
                ret = emvService.Emv_GetTLV(emvTLV);
                // AppendDis("TLV"+Integer.toHexString(emvTLV.Tag).toUpperCase()+ ":"+StringUtil.bytesToHexString(emvTLV.Value));
                if (EmvService.EMV_TRUE == ret) {
                    if (Integer.toHexString(emvTLV.Tag).toUpperCase().equals("5F20")||Integer.toHexString(emvTLV.Tag).toUpperCase().equals("50")){

                        AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":" + new String(emvTLV.Value));
                    }else{
                        AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":" + StringUtil.bytesToHexString(emvTLV.Value));
                    }

                }else {
                    AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":N/G" );
                }
                //showMessage(String.format("TLV%s : %s", Integer.toHexString(emvTLV.Tag).toUpperCase(), StringUtil.bytesToHexString(emvTLV.Value)));
                //   Log.e(TAG, String.format("Getting TLV: %s : %s Result %d", Integer.toHexString(emvTLV.Tag), StringUtils.bytesToHex(emvTLV.Value), ret));

            }
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onVerifyCert() {
            changePanUIVisibility(false, null);
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onOnlineProcess(EmvOnlineData emvOnlineData) {

            Log.e("yw_onOnlineProcess","onOnlineProcess");

            isOnlineTransaction = true;
            changeDialogText("Online processing...");

            changePanUIVisibility(false, null);
            if (onlineSuccess()) {
                emvOnlineData.ResponeCode = "00".getBytes();
//                emvOnlineData.ScriptData71 =
//                emvOnlineData.IssuAuthenData =
//                emvOnlineData.ScriptData72 =
//                emvOnlineData.ResponeCode =
                return EmvService.ONLINE_APPROVE;
            } else {
                return EmvService.ONLINE_FAILED;
            }
        }

        @Override
        public int onRequireTagValue(int i, int i1, byte[] bytes) {
            return 0;
        }

        @Override
        public int onRequireDatetime(byte[] bytes) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());//Get the current time
            String str = formatter.format(curDate);
            byte[] time = new byte[0];
            try {
                time = str.getBytes("ascii");
                System.arraycopy(time, 0, bytes, 0, bytes.length);
                return EmvService.EMV_TRUE;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e("MyEmvService", "onRequireDatetime failed");
                return EmvService.EMV_FALSE;
            }
        }

        @Override
        public int onReferProc() {
            changePanUIVisibility(false, null);
            return EmvService.EMV_TRUE;
        }

        @Override
        public int OnCheckException(String s) {
            return EmvService.EMV_FALSE;
        }

        @Override
        public int OnCheckException_qvsdc(int i, String s) {
            return EmvService.EMV_FALSE;
        }
    };

    TextView tv_name,tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv);

        tv_name=findViewById(R.id.tv_name);
        tv_name.setText("Transaction");
        // set pinpad fix    off mean Dynamic
     //   Intent pinpadintent = new Intent("android.intent.action.emv.res.set");
      //  pinpadintent.putExtra("pinpad_fix","off");
        //set pinpad beep    off mean disable
      //  pinpadintent.putExtra("emv_beep","on");
      //  sendBroadcast(pinpadintent);

        //Set the context value;
        context = this;
        //Init View
        initView();
        //Init Dev
        initDev();
        //new all the other NFC card Class
        InitTheClasses();
    }

    @Override
    protected void onResume() {
        if (!isDevInit) {
            initDev();
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        if (isDevInit) {
            emvService.deviceClose();


            pinpadService.Pinpad_Close();


            isDevInit = false;
        }
        super.onStop();
    }

    private void confirm()
    {
        if (!isDevInit) {
            //emv is not ok
            AppendDis("EMV not Init,Please Reopen the App");
            return;
        }

        amount = 100.00;
        detectDialogBinding = DetectDialogBinding.inflate(getLayoutInflater());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        processDialog = builder.setTitle("Amount：kz "+amount)
                .setView(detectDialogBinding.getRoot())
                .setCancelable(false)
                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        cancelDetect();
                    }
                })
                .show();
        detectDialogBinding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processDialog.cancel();
            }
        });

        startDetect();
    }

    /**
     * start card detection
     */
    private void startDetect() {
        //new a thread
        MyThread myThread = new MyThread();
        Thread thread = new Thread(myThread);
        thread.start();
    }


    public class MyThread implements Runnable {

        @Override
        public void run() {
            //Lock
            ThreadLock.lock();
            //reset the param
            resetData();

            try {
               EmvService.MagStripeCloseReader();
                EmvService.NfcCloseReader();
                EmvService.IccCloseReader();

                _LastCode = EmvService.MagStripeOpenReader();
               if(EmvService.EMV_DEVICE_TRUE == _LastCode)
                {
                    _LastCode = EmvService.NfcOpenReader(200);
                    if(EmvService.EMV_DEVICE_TRUE == _LastCode)
                    {
                        _LastCode = EmvService.IccOpenReader();
                        if(EmvService.EMV_DEVICE_TRUE == _LastCode)
                        {
                            AppendDis("Start Detect Card.");
                            while (!stopDetect) {
                                if (EmvService.NfcCheckCard(30) == EmvService.EMV_DEVICE_TRUE) {
                                    stopDetect = true;
                                    changeDialogText("start transaction...");
                                    AppendDis("Find a nfc card start transaction...");
                                    startNFCTransaction();
                                }

                                if (EmvService.IccCheckCard(30) == EmvService.EMV_DEVICE_TRUE) {
                                    stopDetect = true;
                                    EmvService.IccCard_Poweron();
                                    emvService.setListener(MyListener);
                                    changeDialogText("start transaction...");
                                    AppendDis("Find a ic card start transaction...");
                                    startIcTransaction();
                                    EmvService.IccCard_Poweroff();
                                }

                                if (EmvService.MagStripeCheckCard(100) == EmvService.EMV_DEVICE_TRUE) {
                                    stopDetect = true;
                                    changeDialogText("start transaction...");
                                    AppendDis("Find a MagStripe card start transaction...");
                                    startMagTransaction();
                                }
                            }
                            EmvService.IccCloseReader();
                        }
                        else{
                            AppendDis("Icc Reader open fail:$_LastCode");
                        }
                        EmvService.NfcCloseReader();
                    }
                    else{
                        AppendDis("Nfc Reader open fail:$_LastCode");
                    }
                   EmvService.MagStripeCloseReader();
               }
               else {
                   AppendDis("Mag Reader open fail:$_LastCode");
                }
                if (processDialog.isShowing()) {
                    processDialog.dismiss();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                //unlocck
                ThreadLock.unlock();
                stopDetect = true;
            }
        }
    };

    /**
     * start NFC transaction
     */
    private void startNFCTransaction() {
        //set the NFC flag
        isNFC = true;

        EmvParam param = new EmvParam();
        param.CountryCode=new byte[]{(byte) 0x01, (byte) 0x56};//156  EmvCountryCode
        param.Capability = new byte[]{(byte) 0xE0, (byte) 0xF9, (byte) 0xC8};
        param.TransType = (byte) 0x00;     //当前交易类型
      //  param.TransLimt = 0;   // 以前对qvsdc/qTransmit旧规范有用, 但最新的规范已经没用到了
        param.TerminalType = (byte) 0x22; //终端类型


        param.NFC_OffLineFloorLimit = 0; //>非接最低限额
        param.NFC_CVMLimit = 0;// >此金融需要持卡人验证
        param.NFC_TransLimit = 2147483647;

        //check the type of card
        switch (detectCardKernelEX()) {
            case EmvService.NFC_KERNEL_DEFAUT_CARD_VISA:
                AppendDis("CardType:VISA");
                /*Because payware and transit cards are both VISA contactless cards,
                 only be distinguished by application scenarios*/
                activityVisaPayware.StartTransaction(amount,param);
                //activityTransit.StartTransaction(amount);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_MASTER:
                AppendDis("CardType:MASTER");
                activityMasterPaypass.StartTransaction(amount,param);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_UNIONPAY:
                AppendDis("CardType:UNIONPAY");
                activityPBOC.StartTransaction(amount,param);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_RUPAY:
                AppendDis("CardType:RUPAY");
                activityRupay.StartTransaction(amount);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_AMEX:
                AppendDis("CardType:AMEX");
                activityAmex.StartTransaction(amount,param);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_DISCOVER:
                AppendDis("CardType:NFC_KERNEL_DEFAUT_CARD_DISCOVER");
                activityDiscover.StartTransaction(amount,param);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_MIR:
                AppendDis("CardType:MIR");
                activityMir.StartTransaction(amount);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_JCB:
                AppendDis("CardType:JCB");
                AppendDis("This card is not supported at present");
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_PURE:
               //Pure 流程
                AppendDis("CardType:PURE");
                activityPure.StartTransaction(amount);
                break;
            case EmvService.NFC_KERNEL_DEFAUT_CARD_UNKNOWN:
                AppendDis("CardType:UNKNOWN");
                AppendDis("This card is not supported at present");
                break;
            default:
                AppendDis("This card is not supported at present");
                break;
        }

        changePanUIVisibility(false, null);
        if (processDialog.isShowing()) {
            processDialog.dismiss();
        }
    }

    /**
     * start IC transaction
     */
    private void startIcTransaction() {
        _LastCode = emvService.Emv_TransInit();
        if (EmvService.EMV_TRUE != _LastCode) {
            AppendDis("Emv_TransInit fail:" + _LastCode);
            return;
        }
        AppendDis("Emv_TransInit succ.");

        EmvParam param = new EmvParam();
        param.CountryCode=new byte[]{(byte) 0x01, (byte) 0x56};//156  EmvCountryCode
        param.Capability = new byte[]{(byte) 0xE0, (byte) 0xF9, (byte) 0xC8};
        param.TransType = (byte) 0x00;     //当前交易类型
        param.TransLimt = 0;   // 交易金融 《 有可能是离线或者免密
        param.TerminalType = (byte) 0x22; //终端类型


        param.NFC_OffLineFloorLimit = 0; //>非接最低限额
        param.NFC_CVMLimit = 0;// >此金融需要持卡人验证
        param.NFC_TransLimit = 2147483647;

        _LastCode = emvService.Emv_SetParam(param);
        if (EmvService.EMV_TRUE != _LastCode) {
            AppendDis("Emv_SetParam fail,err code:" + _LastCode);
            return;
        }
        AppendDis("Emv_SetParam succ");
         emvService.Emv_SetOfflinePinCBenable(1);
        _LastCode = emvService.Emv_StartApp(0);//1  online

        EmvTLV tlv = new EmvTLV(0x9F27);//second cryptogram
        emvService.Emv_GetTLV(tlv);
        AppendDis("Tag9F27:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));

//Aqui
        cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");

        AppendDis("cardNumber->" + cardNum);
        AppendDis("Encrypted cardNumber:" + encryptPan(cardNum));

        if (isMkMode) {
            //MK/SK mode
            pinBlock = getMkPin(cardNum);
        } else {
            //Dukpt mode
            pinBlock = getDukptPin(cardNum);
        }

        changePanUIVisibility(false, null);
        changeDialogText("Online processing...");

//Aqui
        if (EmvService.EMV_TRUE == _LastCode) {
            AppendDis("Transaction Success");
        } else {
            AppendDis("EMV Fail:" + ErrMsg.GetEmvErrMsg(_LastCode));
            AppendDis("EMV Fail:" +"error code:"+_LastCode );
            isNoErr = false;
        }

        changePanUIVisibility(false, null);
        if (!isOnlineTransaction) {
            //upload offline transaction data
        }

        if (processDialog.isShowing()) {
            processDialog.dismiss();
        }
    }

    /**
     * start mag transaction
     */
    private void startMagTransaction() {

        try {
            isMag = true;
            Track1 = EmvService.MagStripeReadStripeData(1);
            Track2 = EmvService.MagStripeReadStripeData(2);
            String Track3 = EmvService.MagStripeReadStripeData(3);
            AppendDis("Get mag data->\n" +
                    "Track1:" + Track1 +
                    "\nTrack2:" + Track2 +
                    "\nTrack3:" + Track3);

            if (!(null == Track1 || Track1.isEmpty())) {
                AppendDis("Encrypted Track1:" + encryptPan(Track1));
            }

            if (!(null == Track2 || Track2.isEmpty()))
            {
                int index = Track2.indexOf("=");
                if (index < 1 || index > 21) {
                    index = Track2.indexOf("D");
                    if (index < 1 || index > 21) {
                        AppendDis("Get Mag Data Fail");
                        if (processDialog.isShowing()) {
                            processDialog.dismiss();
                        }
                        isNoErr = false;
                        return;
                    }
                }
                cardNum = Track2.substring(0, index);
                AppendDis("cardNumber->" + cardNum);
                AppendDis("Encrypted cardNumber:" + encryptPan(cardNum));
                AppendDis("Encrypted Track2:" + encryptPan(Track2));

                String serviceNum = Track2.substring(index + 5, index + 8);
                changeDialogText("Please input PIN");
                String hidePan = cardNum.substring(0, 6) + "******" + cardNum.substring(cardNum.length() - 4, cardNum.length());
                changePanUIVisibility(true, "PAN:"+ hidePan + "\nServiceNum:" + serviceNum);
                int result;
                if (isMkMode) {
                    //MK/SK mode
                    pinBlock = getMkPin(cardNum);
                } else {
                    //Dukpt mode
                    pinBlock = getDukptPin(cardNum);
                }
                if("" == pinBlock) {
                    changePanUIVisibility(false, null);
                    isNoErr = false;
                    return;
                }
                changePanUIVisibility(false, null);
                changeDialogText("Online processing...");

                if (onlineSuccess()) {
                    AppendDis("Transaction Success");
                } else {
                    AppendDis("Pay Fail");
                }
            }
            else {
                AppendDis("Get mag data fail! the Track 2 information is required");
            }
        }catch (Exception e){
            AppendDis("err=" + e.getMessage());
        };
    }

    /**
     * cancel card detection
     */
    private void cancelDetect() {
        stopDetect = true;
    }

    /**
     * reset the data
     */
    private void resetData() {
        currentKSN = "";
        pinBlock = "";
        cardNum = "";
        isNFC = false;
        stopDetect = false;
        Track1 = "";
        Track2 = "";
        isMag = false;
        isNoErr = true;
    }

    /**
     * change the message in the process dialog
     * @param msg String
     */
    public void changeDialogText(String msg) {
        EMVActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectDialogBinding.tvCancel.setVisibility(View.GONE);
                detectDialogBinding.tvMessage.setText(msg);
            }
        });
    }

    public void changePanUIVisibility(Boolean isShow, String text) {
        EMVActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    panDialog.setMessage(text);
                    panDialog.getWindow().setGravity(Gravity.TOP);
                    panDialog.show();
                } else {
                    if (panDialog.isShowing()) {
                        panDialog.cancel();
                    }
                }
            }
        });
    }

    /**
     * get PIN with MK/SK
     * @param
     * @return String result PinBlockStr
     */
    public String getMkPin(String cardNum) {
        String PinBlockStr = "";

        PinpadBytesOut pinBlock = new PinpadBytesOut();

        pinresult = pinpadService.Pinpad_GetPin(PIN_KEY_INDEX, cardNum,  PinpadEnum.ENUM_PIN_BLOCK_FORMAT.ISO_9564_FORMAT_0, 6, 0, 60, pinBlock);
        if (pinresult != PinpadService.PIN_OK) {
            AppendDis("encipher pin error:"+pinresult);
            isNoErr =false;
        } else {
            PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult);
            AppendDis("Pinblock:" + PinBlockStr);
        }

        return PinBlockStr;
    }

    /**
     * get PIN with DUKPT
     * @param
     * @return String result PinBlockStr
     */
    public String getDukptPin(String cardNum) {
        String PinBlockStr = "";


        PinpadBytesOut ksn = new PinpadBytesOut();
        if (isDesMode) {
            //start session
            AppendDis("DEA_DUKPT");
            pinresult = pinpadService.Pinpad_DEA_DUKPT_Session_Start(1, ksn);
            if (PinpadService.PIN_OK == pinresult) {
                currentKSN = StringUtil.bytesToHexString(ksn.outResult);
                Log.i(TAG, "KSN:" + currentKSN);
                //DES mode
                PinpadBytesOut pinBlock = new PinpadBytesOut();
                pinresult = pinpadService.Pinpad_DEA_DUKPT_GetPin(cardNum,  PinpadEnum.ENUM_PIN_BLOCK_FORMAT.ISO_9564_FORMAT_0, 6, 0, 60, pinBlock);
                if(PinpadService.PIN_OK == pinresult) {
                    PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult);
                }
                else {
                    AppendDis("Pinpad_DEA_DUKPT_GetPin Err:" + ErrMsg.GetPinPadErrMsg(pinresult));
                    isNoErr =false;
                }
            }
            else{
                AppendDis("Sesssion Start Err:" + ErrMsg.GetPinPadErrMsg(pinresult));
                isNoErr =false;
            }
            pinpadService.Pinpad_DEA_DUKPT_Session_End();
        } else {
            //start session
            AppendDis("AES_DUKPT");
            pinresult = pinpadService.Pinpad_AES_DUKPT_Session_Start(1, ksn);
            if (PinpadService.PIN_OK == pinresult) {
                currentKSN = StringUtil.bytesToHexString(ksn.outResult);
                Log.i(TAG, "KSN:" + currentKSN);
                AppendDis("KSN:" + currentKSN);
                //AES mode
                PinpadBytesOut pinBlock = new PinpadBytesOut();
                pinresult = pinpadService.Pinpad_AES_DUKPT_GetPin(PinpadEnum.ENUM_AES_DUKPT_KeyUsage._PINEncryption, cardNum, 6, 0, 60, pinBlock);
                if(PinpadService.PIN_OK == pinresult) {
                    PinBlockStr = StringUtil.bytesToHexString(pinBlock.outResult);
                }
                else {
                    AppendDis("Pinpad_AES_DUKPT_GetPin Err:" + ErrMsg.GetPinPadErrMsg(pinresult));
                }
            }
            else{
                AppendDis("Sesssion Start Err:" + ErrMsg.GetPinPadErrMsg(pinresult));
            }
            pinpadService.Pinpad_AES_DUKPT_Session_End();
        }


        AppendDis("Pinblock:" + PinBlockStr);
        return PinBlockStr;
    }

    /**
     * padding the PAN or mag card's str
     * @param data String? raw data
     * @return String PaddingText
     */
    public String PanPadding(String data) {
        int dataLen = data.length();
        int remain = 16 - dataLen % 16;
        byte[] res = new byte[dataLen + remain];
        int i = 0;
        while (i < dataLen + remain) {
            if (i < dataLen)
                res[i] = (byte)data.charAt(i);
            else
                res[i] = (byte) remain;
            i++;
        }
        String PaddingText = StringUtil.bytesToHexString(res);
        return PaddingText;
    }

    /**
     * use PanKey to encrypt data(PAN or mag card's str)
     * @param data String? encrypt data
     * @return String cipherText
     */
    public String encryptPan(String data) {

        //Open task with index 1
        String cipherText = "";
        String DataAfterPadding = PanPadding(data.toString());
        int result = 0;
        if (isPanMkMode) {
            //MK/SK mode
            if (isPanDesMode) {
                //DES
                PinpadBytesOut output = new PinpadBytesOut();
                byte[] iv = new byte[]{0,0,0,0,0,0,0,0};
                result = pinpadService.Pinpad_Calculate_Normal_DES(
                        PAN_KEY_INDEX,
                        StringUtil.hexStringToByte(DataAfterPadding),
                        output,
                        iv,
                        PinpadEnum.ENUM_ENC_MODE.PIN_ENC_ENCRYPT,
                        PinpadEnum.ENUM_ECB_MODE.PIN_ECB_CBC);
                if(PinpadService.PIN_OK == result){
                    cipherText = StringUtil.bytesToHexString(output.outResult);
                }else {
                    AppendDis("Pinpad_Calculate_Normal_DES Err:" + ErrMsg.GetPinPadErrMsg(result));
                }
            }
            else {
                //AES
                PinpadBytesOut output = new PinpadBytesOut();
                byte[] iv = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
                result = pinpadService.Pinpad_Calculate_Normal_AES(
                        PAN_KEY_INDEX,
                        StringUtil.hexStringToByte(DataAfterPadding),
                        output,
                        iv,
                        PinpadEnum.ENUM_ENC_MODE.PIN_ENC_ENCRYPT,
                        PinpadEnum.ENUM_ECB_MODE.PIN_ECB_CBC);
                if(PinpadService.PIN_OK == result) {
                    cipherText = StringUtil.bytesToHexString(output.outResult);
                }
                else {
                    AppendDis("Pinpad_Calculate_Normal_AES Err:" + ErrMsg.GetPinPadErrMsg(result));
                }
            }
        }
        else{
            //DUKPT mode
            PinpadBytesOut Panksn = new PinpadBytesOut();
            int StartResult = 0;

            if (isPanDesMode) {
                //start session
                StartResult = pinpadService.Pinpad_DEA_DUKPT_Session_Start(1, Panksn);
                if (PinpadService.PIN_OK == StartResult){
                    //DES
                    PinpadBytesOut output = new PinpadBytesOut();
                    byte[] iv = new byte[]{0,0,0,0,0,0,0,0};
                    result = pinpadService.Pinpad_DEA_DUKPT_Calculate_Des(
                            StringUtil.hexStringToByte(DataAfterPadding),
                            output,
                            iv,
                            PinpadEnum.ENUM_ENC_MODE.PIN_ENC_ENCRYPT,
                            PinpadEnum.ENUM_ECB_MODE.PIN_ECB_CBC);

                    if(PinpadService.PIN_OK == result) {
                        cipherText = StringUtil.bytesToHexString(output.outResult);
                    }
                    else {
                        AppendDis("Pinpad_DEA_DUKPT_Calculate_Des Err:" + ErrMsg.GetPinPadErrMsg(result));
                    }
                    pinpadService.Pinpad_DEA_DUKPT_Session_End();
                }
                else{
                    AppendDis("Session Start Err:" + ErrMsg.GetPinPadErrMsg(StartResult));
                }
            } else {
                //start session
                StartResult = pinpadService.Pinpad_AES_DUKPT_Session_Start(1, Panksn);
                if(PinpadService.PIN_OK == StartResult){
                    //AES
                    PinpadBytesOut output = new PinpadBytesOut();
                    byte[] iv = new byte[]{0,0,0,0,0,0,0,0};
                    result= pinpadService.Pinpad_AES_DUKPT_Calculate_Des(
                            PinpadEnum.ENUM_AES_DUKPT_KeyUsage._DataEncryptionEncrypt,
                            StringUtil.hexStringToByte(DataAfterPadding),
                            output,
                            iv,
                            PinpadEnum.ENUM_ENC_MODE.PIN_ENC_ENCRYPT,
                            PinpadEnum.ENUM_ECB_MODE.PIN_ECB_CBC);
                    if(PinpadService.PIN_OK == result) {
                        cipherText = StringUtil.bytesToHexString(output.outResult);
                    }
                    else {
                        AppendDis("Pinpad_AES_DUKPT_Calculate_Des Err:" + ErrMsg.GetPinPadErrMsg(result));
                    }
                    pinpadService.Pinpad_AES_DUKPT_Session_End();
                }
                else{
                    AppendDis("Session Start Err:" + ErrMsg.GetPinPadErrMsg(StartResult));
                }
            }
        }
        return cipherText;
    }

    /**
     * init EMV Lib
     */
    private void initDev() {
        //Init the EmvService
        emvService = EmvService.getInstance();
        AppendDis("Debug On:" + emvService.Emv_SetDebugOn(1));
   //     emvService.setListener(MyListener);

        if(EmvService.EMV_TRUE != (_LastCode = emvService.Open(context))) {
            AppendDis("EmvService.Open Fail:"+_LastCode);
            return;
        }
        AppendDis("EmvService.Open succ");

        if(EmvService.EMV_DEVICE_TRUE != (_LastCode = emvService.deviceOpen())) {
            AppendDis("EmvService.deviceOpen Fail:"+_LastCode);
            return;
        }
        AppendDis("EmvService.deviceOpen succ");

        AppendDis("EMV init ok!");
        emvService.Emv_RemoveAllApp();
        emvService.Emv_RemoveAllCapk();

        AIDUtil.addAllAID(emvService);
        CAPKUtil.addAllCAPK(emvService);


        AppendDis("Add Apps and Capks ok!");

        //Init the PinpadService
        _LastCode = InitPinPad();
        if(PinpadService.PIN_OK != _LastCode) {
            AppendDis("InitPinPad Fail:"+_LastCode);
            return;
        }
        //write key
        _LastCode = WriteKey();
        if(PinpadService.PIN_OK != _LastCode) {
            AppendDis("WriteKey Fail:"+_LastCode);
            return;
        }

        isDevInit = true;
        AppendDis("Dev init succ!!!");
    }



    /**
     * init views
     */
    private void initView() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        panDialog = builder.setTitle("Please Input PIN")
                .setCancelable(false)
                .create();

        et_MsgView = (EditText) findViewById(R.id.id_MsgView);
        bt_confirm = (MaterialButton) findViewById(R.id.id_confirm);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearDis();
                confirm();
            }
        });


    }

    public int WriteKey()
    {

        if (isMkMode){
            byte[] MasterKey = new byte[]{0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11};
            byte[] PinKey = new byte[]{0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22};
            byte[] MacKey = new byte[]{0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33};
            byte[] PanKey = new byte[]{0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44,0x44};

            _LastCode = saveMasterKey(MasterKey);
            if(PinpadService.PIN_OK != _LastCode) {
                return _LastCode;
            }
            _LastCode = savePinKey(PinKey);
            if(PinpadService.PIN_OK != _LastCode) {
                return _LastCode;
            }
            _LastCode = saveMacKey(MacKey);
            if(PinpadService.PIN_OK != _LastCode) {
                return _LastCode;
            }
            _LastCode = savePanKey(PanKey);


        }else {
            //byte[] IPEK = StringUtil.hexStringToByte("0123456789ABCDEFFEDCBA9876543210");
            //byte[] IPEK = StringUtil.hexStringToByte("3F2216D8297BCE9C");
           // byte[] IKSN = StringUtil.hexStringToByte("0000000002DDDDE00000");
            //_LastCode = pinpadService.Pinpad_Write_DEA_DUKPT_IPEK(1,IPEK,IKSN);
        }
        return _LastCode;
    }


    public int InitPinPad()
    {

        pinpadService = new com.common.sdk.emv.PinpadService(context);
        return pinpadService.Pinpad_Open(context);

    }

    /**
     * transaction online
     */
    public boolean onlineSuccess() {
        return true;
    }

    /**
     * write masterKey
     * @param masterKey masterKey
     */
    private int saveMasterKey(byte[] masterKey) {


        return pinpadService.Pinpad_Write_Normal_Key(0,
                PinpadEnum.ENUM_WRITE_MODE.KEY_WRITE_DIRECT,
                0,
                PinpadEnum.PINPAD_NOR_KEY_TYPE.PINKEY_MASTER,
                masterKey);
    }

    /**
     * write pinKey
     * @param pinKey pinKey
     */
    private int savePinKey(byte[] pinKey) {


        return pinpadService.Pinpad_Write_Normal_Key(PIN_KEY_INDEX,
                PinpadEnum.ENUM_WRITE_MODE.KEY_WRITE_DECRYPT,
                0,
                PinpadEnum.PINPAD_NOR_KEY_TYPE.PINKEY_PIN,
                pinKey);
    }

    /**
     * write macKey
     * @param macKey macKey
     */
    private int saveMacKey(byte[] macKey) {

        return pinpadService.Pinpad_Write_Normal_Key(MAC_KEY_INDEX,
                PinpadEnum.ENUM_WRITE_MODE.KEY_WRITE_DECRYPT,
                0,
                PinpadEnum.PINPAD_NOR_KEY_TYPE.PINKEY_MAC,
                macKey);
    }

    /**
     * write panKey
     * @param panKey panKey
     */
    private int savePanKey(byte[] panKey){

        if (isPanDesMode)
        {
            return pinpadService.Pinpad_Write_Normal_Key(PAN_KEY_INDEX,
                    PinpadEnum.ENUM_WRITE_MODE.KEY_WRITE_DECRYPT,
                    0,PinpadEnum.PINPAD_NOR_KEY_TYPE.PINKEY_DES,
                    panKey);
        }
        else {
            return pinpadService.Pinpad_Write_Normal_Key(PAN_KEY_INDEX,
                    PinpadEnum.ENUM_WRITE_MODE.KEY_WRITE_DECRYPT,
                    0,PinpadEnum.PINPAD_NOR_KEY_TYPE.PINKEY_AES,
                    panKey);
        }

    }

    /**
     * use MacKey to calc data
     * @param data String? data need to calc
     * @return String MacResult HexString format
     */
    private String GetMac(byte[] data){
        String  ResultString = "";
        PinpadBytesOut MacResult = new PinpadBytesOut();
        int result = 0;
        if (isMkMode) {
            result = pinpadService.Pinpad_Calculate_Normal_MAC(
                    MAC_KEY_INDEX,
                    data, MacResult,
                    PinpadEnum.ENUM_MAC_MODE.MAC_X99);
            if(PinpadService.PIN_OK == result){
                ResultString = StringUtil.bytesToHexString(MacResult.outResult);
            }
            else {
                AppendDis("Pinpad_Calculate_Normal_MAC Err:" + ErrMsg.GetPinPadErrMsg(result));
            }
        }
        else{
            if (isDesMode) {
                result = pinpadService.Pinpad_DEA_DUKPT_Calculate_Mac(
                        data,
                        MacResult, 0);
                if(PinpadService.PIN_OK == result)
                {
                    ResultString = StringUtil.bytesToHexString(MacResult.outResult);
                }else{
                    AppendDis("Pinpad_DEA_DUKPT_Calculate_Mac Err:" + ErrMsg.GetPinPadErrMsg(result));
                }
            }
            else{
                result = pinpadService.Pinpad_AES_DUKPT_Calculate_Mac(
                        PinpadEnum.ENUM_AES_DUKPT_KeyUsage._MessageAuthenticationGeneration,
                        data,
                        MacResult, 0);
                if(PinpadService.PIN_OK == result) {
                    ResultString = StringUtil.bytesToHexString(MacResult.outResult);
                }
                else {
                    AppendDis("Pinpad_AES_DUKPT_Calculate_Mac Err:" + ErrMsg.GetPinPadErrMsg(result));
                }
            }
        }
        return  ResultString;
    }

    private void InitTheClasses()
    {
        //set the init flag
        isDevInit = false;
        //new the payware class
        activityVisaPayware = new ActivityVisaPayware(this);
        if(null == activityVisaPayware){
            return;
        }
        //new the PBOC class
        activityPBOC = new ActivityPBOC(this);
        if(null == activityPBOC){
            return;
        }
        //new the MasterPaypass class
        activityMasterPaypass = new ActivityMasterPaypass(this);
        if(null == activityMasterPaypass){
            return;
        }
        //new the Rupay class
        activityRupay = new ActivityRupay(this);
        if(null == activityRupay){
            return;
        }
        //new the Amex class
        activityAmex = new ActivityAmex(this);
        if(null == activityAmex){
            return;
        }
        //new the Mir class
        activityMir = new ActivityMir(this);
        if(null == activityMir){
            return;
        }

        //new the Transit class
        activityTransit = new ActivityTransit(this);
        if(null == activityTransit){
            return;
        }

        activityDiscover = new ActivityDiscover(this);
        if(null == activityDiscover){
            return;
        }


        activityPure = new ActivityPure(this);
        if(null == activityPure){
            return;
        }


        //init succ
        isDevInit = true;
        return ;
    }

    void ClearDis() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_MsgView.setText("");
                DisplayBuf = new StringBuffer("");
            }
        });
    }


    void AppendDis(String Mes) {
        Log.i("AppendDis", Mes);
        DisplayBuf.append(Mes);
        DisplayBuf.append("\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                et_MsgView.requestFocus();
                et_MsgView.setText(DisplayBuf.toString());
                et_MsgView.setSelection(et_MsgView.getText().length());
            }
        });
    }

    int detectCardKernelEX() {


        int card_type_ret = -1;
        EmvApp app_visa = new EmvApp();
        app_visa.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x03};

        EmvApp app_master = new EmvApp();
        app_master.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};

        EmvApp app_unionpay = new EmvApp();
        app_unionpay.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33};

        EmvApp app_rupay = new EmvApp();
        app_rupay.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x05, 0x24};

        EmvApp app_amex = new EmvApp();
        app_amex.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x25};

        EmvApp app_discover = new EmvApp();
        app_discover.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x24};

        EmvApp app_jcb = new EmvApp();
        app_jcb.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x00, 0x65};

        EmvApp app_mir = new EmvApp();
        app_mir.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x06, 0x58};

        EmvApp app_pure = new EmvApp();
        app_pure.AID = new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x71};  //A0000003710001


        EmvApp[] list = new EmvApp[]{app_visa, app_master, app_unionpay, app_rupay, app_amex, app_discover, app_jcb, app_mir, app_pure};
        card_type_ret = emvService.NFC_CheckKernelIDEx(list);
        Log.e("yw_card kernel", "detectCardKernelEX: " + card_type_ret);
        return card_type_ret;
    }
}