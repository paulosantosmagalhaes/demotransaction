package com.pos.demosdk.EMV;

import android.content.Context;
import android.util.Log;

import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.emv.MirListener;
import com.telpo.emv.MirParam;
import com.telpo.emv.MirResult;
import com.telpo.util.StringUtil;

import java.util.List;

public class ActivityMir {
    private EMVActivity nContext;
    private int _LastCode = 0;

    public ActivityMir(Context context) {
        this.nContext = (EMVActivity) context;
    }

    MirListener listener = new MirListener() {
        @Override
        public int onMir_FinishReadAppData() {
            Log.w("yw_listener", "onMir_FinishReadAppData" );

            int ret = 0;
            EmvTLV tlv = new EmvTLV(0x5A);
            ret = nContext.emvService.Emv_GetTLV(tlv);
            if(EmvService.EMV_TRUE == ret){
                nContext.cardNum = StringUtil.bytesToHexString(tlv.Value).replace("F", "");
                nContext.AppendDis("0x5A:"+ StringUtil.bytesToHexString(tlv.Value));
            }else{
                ret = nContext.emvService.Mir_IsUseTrack2Pan();
                if(EmvService.EMV_TRUE == ret){
                    tlv = new EmvTLV(0x57);
                    ret = nContext.emvService.Emv_GetTLV(tlv);
                    if(EmvService.EMV_TRUE == ret) {
                        String str_57 = StringUtil.bytesToHexString(tlv.Value);
                        nContext.cardNum = str_57.substring(0, str_57.indexOf('D'));
                        nContext.AppendDis("0x57:" + StringUtil.bytesToHexString(tlv.Value));
                    }else {
                        nContext.AppendDis("Get cardNum Fail");
                    }
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
        public int onMir_DataExchange() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onMir_Hint() {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onMir_InitApp() {
            Log.w("yw_listener", "onMir_InitApp" );
            EmvTLV tlv = new EmvTLV(0x9F06);
            nContext.emvService.Emv_GetTLV(tlv);
            Log.w("listener", "select aid:"+ com.telpo.emv.util.StringUtil.bytesToHexString(tlv.Value) );
            return 0;
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

        //Mir_TransInitEx
        MirParam mirParam = new MirParam();
        _LastCode = nContext.emvService.Mir_TransInitEx(mirParam);
        if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("Mir_TransInitEx fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Mir_TransInitEx succ");

        //Mir_StartEx
        _LastCode = nContext.emvService.Mir_StartEx((int)amount*100,0,643,2,0);
      /*  if(EmvService.EMV_TRUE != _LastCode){
            nContext.AppendDis("Mir_StartEx fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Mir_StartEx succ");*/

        _LastCode = nContext.emvService.Mir_GetOutComeResult();
        if(MirResult.MIR_RESULT_APPROVED == _LastCode) {
            //offline APPROVED
        }
        else if(MirResult.MIR_RESULT_ONLINE == _LastCode){
            _LastCode = nContext.emvService.Mir_GetOutComeCVM();
            if(MirResult.MIR_CVM_ONLINEPIN == _LastCode){
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
                if("" == nContext.pinBlock) {
                    nContext.changePanUIVisibility(false, null);
                    return false;
                }
                nContext.changePanUIVisibility(false, null);
            }
        }
        else {
            nContext.AppendDis("Transaction Fail,err code:"+ _LastCode);
            return false;
        }
        nContext.AppendDis("Transaction Success");
        return true;
    }
}
