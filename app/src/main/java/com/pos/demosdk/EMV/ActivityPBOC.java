package com.pos.demosdk.EMV;

import android.content.Context;

import com.common.sdk.emv.PinpadService;
import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityPBOC {
    private EMVActivity nContext;
    private int _LastCode = 0;
    public ActivityPBOC(Context context) {
        this.nContext = (EMVActivity) context;
    }

    public boolean StartTransaction(double amount,EmvParam mEMVParam)
    {

        _LastCode = nContext.emvService.qPboc_InitParam(mEMVParam);
        if(EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("qPboc_InitParam fail, err code:"+_LastCode);
            return false;
        }
        else{
            nContext.AppendDis("qPboc_InitParam succ!");
        }

        _LastCode = nContext.emvService.qPboc_Preprocess((int)(amount*100),2,156,EmvService.KERNEL_QPBOC);
        if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("qPboc_Preprocess fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("qPboc_Preprocess succ");

        _LastCode = nContext.emvService.qPboc_StartApp(EmvService.KERNEL_QPBOC);

        nContext.AppendDis("qPboc_StartApp:"+ _LastCode);

        int ret=0;

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


        if(EmvService.QPBOC_TC == _LastCode)
        {
            nContext.AppendDis("Offline Transaction Success");
        }
        else if(EmvService.QPBOC_ARQC == _LastCode)
        {
            EmvTLV tlv = new EmvTLV(0x5A);
            _LastCode = nContext.emvService.Emv_GetTLV(tlv);
            if(EmvService.EMV_TRUE == _LastCode){
                nContext.cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
                nContext.AppendDis("0x5A:"+ StringUtil.bytesToHexString(tlv.Value));
            }else{
                tlv = new EmvTLV(0x57);
                _LastCode = nContext.emvService.Emv_GetTLV(tlv);
                if(EmvService.EMV_TRUE == _LastCode) {
                    String str_57 = StringUtil.bytesToHexString(tlv.Value);
                    nContext.cardNum = str_57.substring(0, str_57.indexOf('D'));
                    nContext.AppendDis("0x57:" + StringUtil.bytesToHexString(tlv.Value));
                }else {
                    nContext.AppendDis("Get cardNum Fail");
                }
            }
            if(!(null == nContext.cardNum || nContext.cardNum.isEmpty())){
                nContext.AppendDis("cardNum:" + nContext.cardNum);
                nContext.AppendDis("encryptPan:" + nContext.encryptPan(nContext.cardNum));
            }

            if(EmvService.EMV_TRUE == nContext.emvService.qPboc_IsNeedPin())
            {
                if(null == nContext.cardNum || nContext.cardNum.isEmpty()){
                    nContext.AppendDis("Transaction Fail,No Card info!");
                    return false;
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
                    nContext.AppendDis("Transaction Fail");
                    return false;
                }
            }
        }
        else{
            nContext.AppendDis("qPboc_StartApp Fail:" + _LastCode );
            return false;
        }
        nContext.changePanUIVisibility(false, null);
        //the online result
        if (nContext.onlineSuccess()) {
            nContext.AppendDis("Transaction Success");
            return true;
        } else {
            nContext.AppendDis("Transaction Fail");
            return false;
        }


    }
}
