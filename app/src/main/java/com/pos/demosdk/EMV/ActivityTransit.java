package com.pos.demosdk.EMV;

import android.content.Context;

import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.QTransitListener;
import com.telpo.emv.QTransitParam;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityTransit {

    private EMVActivity nContext;
    private int _LastCode = 0;

    public ActivityTransit(Context context) {
        this.nContext = (EMVActivity) context;
    }

    QTransitListener listener = new QTransitListener() {


        @Override
        public int onTransitOnlineProcess(EmvOnlineData OnlineData) {
            if(null == OnlineData) {
                return EmvService.ONLINE_FAILED;
            }
            nContext.changeDialogText("Online processing...");
            nContext.changePanUIVisibility(false, null);
            //the online result
            if (nContext.onlineSuccess()) {
                OnlineData.ResponeCode = "00".getBytes();
                return EmvService.ONLINE_APPROVE;
            } else {
                return EmvService.ONLINE_FAILED;
            }
        }

        @Override
        public int OnTransitFinishReadAppData() {
            int ret = 0;
            EmvTLV tlv = new EmvTLV(0x5A);
            ret = nContext.emvService.Emv_GetTLV(tlv);
            if(EmvService.EMV_TRUE == ret){
                nContext.cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
                nContext.AppendDis("0x5A:"+ StringUtil.bytesToHexString(tlv.Value));
            }else{
                tlv = new EmvTLV(0x57);
                ret = nContext.emvService.Emv_GetTLV(tlv);
                if(EmvService.EMV_TRUE == ret) {
                    String str_57 = StringUtil.bytesToHexString(tlv.Value);
                    nContext.cardNum = str_57.substring(0, str_57.indexOf('D'));
                    nContext.AppendDis("0x57:" + StringUtil.bytesToHexString(tlv.Value));
                }else {
                    nContext.AppendDis("Get cardNum Fail");
                }
            }
            if(!(null == nContext.cardNum || nContext.cardNum.isEmpty())){
                nContext.AppendDis("cardNum:"+ nContext.cardNum);
                nContext.AppendDis("encryptPan:" + nContext.encryptPan(nContext.cardNum));
            }

            List<EmvTLV> tagList=EMVUtils.getTLVCardDataTags();
            for (EmvTLV emvTLV : tagList) {
                ret= nContext.emvService.Emv_GetTLV(emvTLV);
                // AppendDis("TLV"+Integer.toHexString(emvTLV.Tag).toUpperCase()+ ":"+StringUtil.bytesToHexString(emvTLV.Value));
                if (EmvService.EMV_TRUE == ret){
                    nContext.AppendDis("Tag"+Integer.toHexString(emvTLV.Tag).toUpperCase()+ ":"+StringUtil.bytesToHexString(emvTLV.Value));
                }else {
                    nContext.AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":N/G" );
                }
                //showMessage(String.format("TLV%s : %s", Integer.toHexString(emvTLV.Tag).toUpperCase(), StringUtil.bytesToHexString(emvTLV.Value)));
                //   Log.e(TAG, String.format("Getting TLV: %s : %s Result %d", Integer.toHexString(emvTLV.Tag), StringUtils.bytesToHex(emvTLV.Value), ret));

            }
            return EmvService.EMV_TRUE;
        }

        @Override
        public int OnTransitCheckException(int index, String PAN) {
            nContext.AppendDis("OnCheckException PAN:"+ PAN);
            return EmvService.EMV_FALSE;
        }

        @Override
        public int onTransitRequireDatetime(byte[] datetime) {
            return 0;
        }
    };

    public boolean StartTransaction(double amount)
    {
        //set the listener
        nContext.emvService.setListener(listener);

        //qVsdc_TransInit
        QTransitParam param = new QTransitParam();
        param.AMOUNT_Amount = (long) (amount*100);
        _LastCode = nContext.emvService.qTransit_TransInit(param);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("qTransit_TransInit fail, err code:"+_LastCode);
            return false;
        }
        else{
            nContext.AppendDis("qTransit_TransInit succ!");
        }

        //set emv param
        EmvParam mEMVParam;
        mEMVParam = new EmvParam();
        _LastCode = nContext.emvService.Emv_SetParam(mEMVParam);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_SetParam fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Emv_SetParam succ"); }

        //StartApp
        _LastCode = nContext.emvService.qTransit_StartApp();
        if((EmvService.QVSDC_OFFLINE_APPROVE == _LastCode) || (EmvService.QVSDC_ONLINE_APPROVE == _LastCode))
        {
            nContext.AppendDis("Transaction Success");
        }else{
            nContext.AppendDis("qTransit_StartApp Fail:" + _LastCode);
            return false;
        }
        return true;
    }
}
