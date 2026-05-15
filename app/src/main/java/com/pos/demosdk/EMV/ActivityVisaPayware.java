package com.pos.demosdk.EMV;

import android.content.Context;

import com.common.sdk.emv.PinpadService;
import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvAmountData;
import com.telpo.emv.EmvCandidateApp;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvPinData;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvServiceListener;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.QvsdcParam;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityVisaPayware {
    private EMVActivity nContext;
    private int _LastCode = 0;

    public ActivityVisaPayware(Context context) {
        this.nContext = (EMVActivity) context;
    }

    EmvServiceListener listener = new EmvServiceListener() {
        @Override
        public int onInputAmount(EmvAmountData emvAmountData) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onInputPin(EmvPinData emvPinData) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onSelectApp(EmvCandidateApp[] emvCandidateApps) {
            return emvCandidateApps[0].index;
        }

        @Override
        public int onSelectAppFail(int i) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onFinishReadAppData() {
            int ret = 0;
            EmvTLV tlv = new EmvTLV(0x5A);
            ret = nContext.emvService.Emv_GetTLV(tlv);
            if(EmvService.EMV_TRUE == ret){
                nContext.cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
               // nContext.AppendDis("0x5A:"+ StringUtil.bytesToHexString(tlv.Value));
            }else{
                tlv = new EmvTLV(0x57);
                ret = nContext.emvService.Emv_GetTLV(tlv);
                if(EmvService.EMV_TRUE == ret) {
                    String str_57 = StringUtil.bytesToHexString(tlv.Value);
                    nContext.cardNum = str_57.substring(0, str_57.indexOf('D'));
                   // nContext.AppendDis("0x57:" + StringUtil.bytesToHexString(tlv.Value));
                }else {
                    nContext.AppendDis("Get cardNum Fail");
                }
            }

            if(!(null == nContext.cardNum || nContext.cardNum.isEmpty())){
                nContext.AppendDis("cardNum:"+ nContext.cardNum);
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

            }
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onVerifyCert() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onOnlineProcess(EmvOnlineData emvOnlineData) {
            if(null == emvOnlineData) {
                return EmvService.ONLINE_FAILED;
            }
            nContext.changeDialogText("Online processing...");
            //is need to get pin
            if (nContext.emvService.qVsdc_IsNeedPin() == EmvService.EMV_TRUE) {
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

                if (nContext.pinresult == PinpadService.PIN_OK) {
                    //success
                }else {
                    nContext.changePanUIVisibility(false, null);
                    return  EmvService.ONLINE_FAILED;
                }

            }
            nContext.changePanUIVisibility(false, null);
            //the online result
            if (nContext.onlineSuccess()) {
                emvOnlineData.ResponeCode = "00".getBytes();
                return EmvService.ONLINE_APPROVE;
            } else {
                return EmvService.ONLINE_FAILED;
            }
        }

        @Override
        public int onRequireTagValue(int i, int i1, byte[] bytes) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onRequireDatetime(byte[] bytes) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onReferProc() {
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

    public boolean StartTransaction(double amount,EmvParam mEMVParam)
    {
        //set the listener
        nContext.emvService.setListener(listener);


        //qVsdc_TransInit
        QvsdcParam param = new QvsdcParam();
        param.AMOUNT_Amount = (long) (amount*100);
        param.AMOUNT_CurrCode=156;

        _LastCode = nContext.emvService.qVsdc_TransInit(param);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("qVsdc_TransInit fail, err code:"+_LastCode);
            return false;
        }
        else{
            nContext.AppendDis("qVsdc_TransInit succ!");
        }

        //set emv param
        _LastCode = nContext.emvService.Emv_SetParam(mEMVParam);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_SetParam fail,err code:"+_LastCode);
            return false;
        }
        else{ nContext.AppendDis("Emv_SetParam succ"); }

        EmvTLV tlv_ttq = new EmvTLV(0x9F66);
      //  tlv_ttq.Value=StringUtil.hexStringToByte("36A04000");

        tlv_ttq.Value=StringUtil.hexStringToByte("32A04000");

        nContext.emvService.Emv_SetTLV(tlv_ttq);

        //qVsdc_Preprocess
        _LastCode = nContext.emvService.qVsdc_Preprocess();
        if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("qVsdc_Preprocess fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("qVsdc_Preprocess succ");

        //qVsdc_StartApp
        _LastCode = nContext.emvService.qVsdc_StartApp();

        nContext.AppendDis("qVsdc_StartApp:"+_LastCode);



        if((EmvService.QVSDC_OFFLINE_APPROVE == _LastCode) || (EmvService.QVSDC_ONLINE_APPROVE == _LastCode))
        {
            nContext.AppendDis("Transaction Success");
            return true;
        }else{
            nContext.AppendDis("qVsdc_StartApp Fail:" + _LastCode );
            return false;
        }

    }
}
