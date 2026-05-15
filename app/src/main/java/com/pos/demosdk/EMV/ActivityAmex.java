package com.pos.demosdk.EMV;

import android.content.Context;
import android.util.Log;

import com.common.sdk.emv.PinpadService;
import com.pos.demosdk.MainActivity;
import com.telpo.emv.AmexAmount;
import com.telpo.emv.AmexLimits;
import com.telpo.emv.AmexListener;
import com.telpo.emv.AmexParam;
import com.telpo.emv.AmexResult;
import com.telpo.emv.EmvCertRevo;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.util.ErrMsg;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityAmex {
    private EMVActivity nContext;
    private int _LastCode = 0;

    public ActivityAmex(Context context) {
        this.nContext = (EMVActivity) context;
    }

    AmexListener listener = new AmexListener() {

        @Override
        public int OnAmexMessage(int i, int i1) {
            nContext.AppendDis("OnAmexMessage,MessageID:" + i + "HoldTimesMs:" + i1);


            if (i==AmexResult.AMEX_MESS_CARD_READ_OK){
                EmvTLV tlv;
                // EmvTLV tlv = new EmvTLV(0x9F06);
                // emvService.Emv_GetTLV(tlv);
                //   AppendDis("AID:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));

                //  tlv = new EmvTLV(0x9F1A);
                //  emvService.Emv_GetTLV(tlv);
                //  AppendDis("CountryCode:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));

                tlv = new EmvTLV(0x5A);
              int ret = nContext.emvService.Emv_GetTLV(tlv);
                if(EmvService.EMV_TRUE == ret){
                    nContext.cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
                    //  AppendDis("0x5A:"+ com.telpo.util.StringUtil.bytesToHexString(tlv.Value));
                }else{
                    tlv = new EmvTLV(0x57);
                    ret = nContext.emvService.Emv_GetTLV(tlv);
                    if(EmvService.EMV_TRUE == ret) {
                        String str_57 = StringUtil.bytesToHexString(tlv.Value);
                        nContext.cardNum = str_57.substring(0, str_57.indexOf('D'));
                        //    AppendDis("0x57:" + com.telpo.util.StringUtil.bytesToHexString(tlv.Value));
                    }else {
                        //  AppendDis("Get cardNum Fail");
                    }
                }
                if(!(null == nContext.cardNum || nContext.cardNum.isEmpty())){
                    nContext.AppendDis("cardNum:"+nContext.cardNum);
                    nContext.AppendDis("encryptPan:" + nContext.encryptPan(nContext.cardNum));
                }

                List<EmvTLV> tagList = EMVUtils.getTLVCardDataTags();
                for (EmvTLV emvTLV : tagList) {
                    ret = nContext.emvService.Emv_GetTLV(emvTLV);
                    // AppendDis("TLV"+Integer.toHexString(emvTLV.Tag).toUpperCase()+ ":"+StringUtil.bytesToHexString(emvTLV.Value));
                    if (EmvService.EMV_TRUE == ret) {
                        if (Integer.toHexString(emvTLV.Tag).toUpperCase().equals("5F20")||Integer.toHexString(emvTLV.Tag).toUpperCase().equals("50")){

                            nContext.AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":" + new String(emvTLV.Value));
                        }else{
                            nContext.AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":" + StringUtil.bytesToHexString(emvTLV.Value));
                        }

                    }else {
                        nContext.AppendDis("Tag" + Integer.toHexString(emvTLV.Tag).toUpperCase() + ":N/G" );
                    }
                    //showMessage(String.format("TLV%s : %s", Integer.toHexString(emvTLV.Tag).toUpperCase(), StringUtil.bytesToHexString(emvTLV.Value)));
                    //   Log.e(TAG, String.format("Getting TLV: %s : %s Result %d", Integer.toHexString(emvTLV.Tag), StringUtils.bytesToHex(emvTLV.Value), ret));

                }
            }
            return EmvService.EMV_TRUE;
        }

        @Override
        public int OnAmexCheckException(int i, String s) {
            return EmvService.EMV_FALSE;
        }

        @Override
        public int OnAmexRequireOnline() {
            //the online result
            //Different values are returned according to the specific situation
            if (nContext.onlineSuccess()) {
                return AmexResult.AMEX_ONLINE_APPROVED;
            } else {
                return AmexResult.AMEX_ONLINE_DECLINED;
            }
        }

        @Override
        public int OnAmexInputPin() {
            if(null == nContext.cardNum || nContext.cardNum.isEmpty()){
                //read card info
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
            }
            // is the cardnum is empty
            if(null == nContext.cardNum || nContext.cardNum.isEmpty()) {
                nContext.AppendDis("Transaction Fail,No Card info!");
                return EmvService.EMV_FALSE;
            }

            nContext.AppendDis("cardNum:"+ nContext.cardNum);
            nContext.AppendDis("encryptPan:" + nContext.encryptPan(nContext.cardNum));

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
            nContext.changePanUIVisibility(false, null);

            int result=0;

            if (nContext.pinresult == PinpadService.PIN_OK) {
                result = EmvService.EMV_TRUE;
            }
           /* else if (nContext.pinresult == PinpadService.PIN_ERROR_PINLEN){ //bypass
                result = EmvService.ERR_NOPIN;
            }
            else if (nContext.pinresult == PinpadService.PIN_ERROR_CANCEL){
                result = EmvService.ERR_USERCANCEL;
            }
            else if (nContext.pinresult == PinpadService.PIN_ERROR_NOKEY){
                result = EmvService.ERR_KEY_NOT_FOUND;
            }
            else if(nContext.pinresult == PinpadService.PIN_ERROR_TIMEOUT){
                result = EmvService.ERR_TIMEOUT;
            }*/else{
                result = EmvService.EMV_FALSE;
            }
            return result;

        }
    };

    public boolean StartTransaction(double amount,EmvParam mEMVParam)
    {
        //set the listener
        nContext.emvService.setListener(listener);
        //set emv param
        _LastCode = nContext.emvService.Emv_SetParam(mEMVParam);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_SetParam fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Emv_SetParam succ"); }

        //Amex TransInit
        //Fill in the corresponding parameters according to the actual situation
/*        EmvCertRevo emvCertRevo = new EmvCertRevo();
        _LastCode = nContext.emvService.Emv_CertRevoList_Add(emvCertRevo);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_CertRevoList_Add fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Emv_CertRevoList_Add succ"); }*/

        AmexParam amexParam = new AmexParam();
        amexParam.AmexVersion = new byte[]{0x00, 0x01};
        amexParam.TermCapability = new byte[]{(byte) 0xE0, 0x00, (byte) 0xC8};
        amexParam.TermCountryCode = 156;
        amexParam.TermType = 0x22;
        amexParam.ContactlessCap = 0xC0;
        amexParam.EnhanceContactlessCap = new byte[]{(byte) 0x58, (byte) 0xE0, 0x00, (byte) 0x83};
        amexParam.MerchantName = "Amex";
        amexParam.MerchantCode = new byte[]{0x41, 0x12};
        amexParam.HoldTimeMs = 300;
        amexParam.MagStripeRangeNumber = 60;
        amexParam.TAC_Denial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};////0010000000
        amexParam.TAC_OnLine = new byte[]{(byte) 0xDE, (byte) 0x00, (byte) 0xFC, (byte) 0x98, (byte) 0x00};//DE00FC9800
        amexParam.TAC_Default = new byte[]{(byte) 0xDC, (byte) 0x50, (byte) 0xFC, (byte) 0x98, (byte) 0x00};////DC50FC9800
        amexParam.IsUnableOnline = 0;
        amexParam.CheckCDAMode = 1;

        AmexAmount amexAmount = new AmexAmount();
        amexAmount.Amount = (long)amount*100;
        amexAmount.CashbackAmount = 0;
        amexAmount.CurrCode = 156;
        amexAmount.CurrExp = 2;

        _LastCode = nContext.emvService.Amex_TransInit(amexParam,amexAmount);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Amex_TransInit fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Amex_TransInit succ"); }

        //Fill in the corresponding parameters according to the actual situation
//        AID = new byte[]{(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x01};
//        SelFlag = (byte) 0x00;
//        Priority = (byte) 0x00;
//        TargetPer = (byte) 20;
//        MaxTargetPer = (byte) 50;
//        FloorLimitCheck = (byte) 1;
//        RandTransSel = (byte) 1;
//        VelocityCheck = (byte) 1;
//        FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit
//        Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00};
//        TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//        TACOnline = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//        TACDefault = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
//        AcquierId = new byte[]{(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x10};
//        DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
//        TDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x02, (byte) 0x06};
//        Version = new byte[]{(byte) 0x00, (byte) 0x01};

        AmexLimits amexLimits = new AmexLimits();
        byte[] AID = new byte[]{(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x25};

        amexLimits.CVMLimit = 0;
        amexLimits.FloorLimit = 0;
        amexLimits.TransLimit = 2147483647;
        _LastCode = nContext.emvService.Amex_AidLimit_Add(amexLimits, AID);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Amex_AidLimit_Add fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Amex_AidLimit_Add succ"); }

        //Amex_Preprocess
        _LastCode = nContext.emvService.Amex_Preprocess();
        if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("Amex_Preprocess fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Amex_Preprocess succ");

        //Amex_StartApp
        _LastCode = nContext.emvService.Amex_StartApp();

        nContext.AppendDis("Amex_StartApp code:"+_LastCode);
//        if(EmvService.EMV_TRUE != _LastCode){
            //nContext.AppendDis("Amex_StartApp code:"+_LastCode);
//            return false;
//        }

        //Get result
        _LastCode = nContext.emvService.Amex_GetOutComeResult();
        nContext.AppendDis("Amex_GetOutComeResult code:" + _LastCode);
        /*if(AmexResult.AMEX_RESULT_AGAIN == _LastCode) {
            _LastCode = nContext.emvService.Amex_RetryApp();
        }*/
        if (AmexResult.AMEX_RESULT_APPROVED == _LastCode) {
            //Approved
            nContext.AppendDis("Transaction Success");
            return true;

        } else {
            nContext.AppendDis("Transaction fail");
            return false;
        }


    }
}
