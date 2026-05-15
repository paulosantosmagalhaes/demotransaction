package com.pos.demosdk.EMV;

import android.content.Context;

import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.RupayAmount;
import com.telpo.emv.RupayListener;
import com.telpo.emv.RupayOnlineData;
import com.telpo.emv.RupayParam;
import com.telpo.emv.RupayServParam;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityRupay {
    private EMVActivity nContext;
    private int _LastCode = 0;

    public ActivityRupay(Context context) {
        this.nContext = (EMVActivity) context;
    }

    RupayListener listener = new RupayListener() {
        @Override
        public int OnFinishReadAppData() {
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
        public int OnRupayCheckException(int i, String s) {
            return EmvService.EMV_FALSE;
        }

        @Override
        public int OnRupayRequireOnline(RupayOnlineData rupayOnlineData) {
            if(null == rupayOnlineData) {
                return EmvService.ONLINE_FAILED;
            }
            nContext.changeDialogText("Online processing...");
            //is need to get pin
            if (nContext.emvService.Rupay_IsNeedPin() == EmvService.EMV_TRUE) {
                if(null == nContext.cardNum || nContext.cardNum.isEmpty()){
                    nContext.AppendDis("Transaction Fail,No Card info!");
                    return EmvService.ONLINE_FAILED;
                }
                String hidePan = nContext.cardNum.substring(0, 6) + "******" + nContext.cardNum.substring(nContext.cardNum.length() - 4,nContext.cardNum.length());
                nContext.changePanUIVisibility(true, "PAN:" + hidePan);
                //online PIN
                if (nContext.isMkMode) {
                    //MK/SK mode
                    nContext.pinBlock = nContext.getMkPin(nContext.cardNum);
                } else {
                    //Dukpt mode
                    nContext.pinBlock = nContext.getDukptPin(nContext.cardNum);
                }
                if("" == nContext.pinBlock) {
                    nContext.changePanUIVisibility(false, null);
                    return EmvService.ONLINE_FAILED;
                }
            }
            nContext.changePanUIVisibility(false, null);
            //the online result
            if (nContext.onlineSuccess()) {
                rupayOnlineData.ResponeCode = "00".getBytes();
                return EmvService.ONLINE_APPROVE;
            } else {
                return EmvService.ONLINE_FAILED;
            }
        }


        boolean stopDetect = false;
        @Override
        public int OnCanRemoveCard() {
            nContext.AppendDis("Please take the card...");



            while (!stopDetect) {
                if (EmvService.NfcCheckCard(10) == EmvService.EMV_DEVICE_TRUE) {
                    stopDetect = false;
                  //  nContext.AppendDis("Please take the card...");
                }else {
                    stopDetect = true;
                }
            }
            return EmvService.EMV_TRUE;
        }
    };

    public boolean StartTransaction(double amount)
    {
        //set the listener
        nContext.emvService.setListener(listener);
        //set emv param
        EmvParam mEMVParam;
        mEMVParam = new EmvParam();
        _LastCode = nContext.emvService.Emv_SetParam(mEMVParam);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_SetParam fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Emv_SetParam succ"); }

        //Rupay_TransInit
        RupayParam param = new RupayParam();
        RupayAmount rupayAmount = new RupayAmount();
        rupayAmount.Amount = (long)amount*100;
        rupayAmount.CashbackAmount = 0;
        rupayAmount.CurrCode = 356;
        rupayAmount.CurrExp = 2;
        _LastCode = nContext.emvService.Rupay_TransInit(param,rupayAmount);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Rupay_TransInit fail, err code:"+_LastCode);
            return false;
        }
        else{
            nContext.AppendDis("Rupay_TransInit succ!");
        }
        //Rupay_SetServiceParam
        RupayServParam rupayServParam = new RupayServParam();
        _LastCode = nContext.emvService.Rupay_SetServiceParam(rupayServParam);
        if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("Rupay_SetServiceParam fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Rupay_SetServiceParam succ");

        //Rupay_Preprocess
        _LastCode = nContext.emvService.Rupay_Preprocess();
        if(EmvService.RUPAY_RESULT_CONTINUE != _LastCode){
            nContext.AppendDis("Rupay_Preprocess fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Rupay_Preprocess succ");

        //Rupay_StartApp
        _LastCode = nContext.emvService.Rupay_StartApp(EmvService.EMV_TRUE);
        _LastCode = nContext.emvService.Rupay_GetOutComeResult();
        if(EmvService.RUPAY_RESULT_DECLINED == _LastCode) {
            nContext.AppendDis("Transaction Declined");
            return false;
        }
        else if(EmvService.RUPAY_RESULT_2TAP == _LastCode){
         //   EmvService.NfcOpenReader(200);
          //  if (EmvService.NfcCheckCard(300) == EmvService.EMV_DEVICE_TRUE){
                _LastCode = nContext.emvService.Rupay_StartApp2nd();
                if(EmvService.EMV_TRUE != _LastCode){
                    nContext.AppendDis("Rupay_StartApp2nd Fial:"+ _LastCode);
                    return false;
                }
          //  }


        }
        else if(EmvService.RUPAY_RESULT_APPROVED == _LastCode) {
            //APPROVED
        }else{
            nContext.AppendDis("Rupay_StartApp Fail:" + _LastCode);
            return false;
        }
        nContext.AppendDis("Transaction Success");
        return true;
    }
}
