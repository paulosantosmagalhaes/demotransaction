package com.pos.demosdk.EMV;

import android.content.Context;

import com.pos.demosdk.MainActivity;
import com.telpo.emv.DpasAmount;
import com.telpo.emv.DpasCTLOutCome;
import com.telpo.emv.DpasDSObject;
import com.telpo.emv.DpasListener;
import com.telpo.emv.DpasParam;
import com.telpo.emv.DpasResult;
import com.telpo.emv.EmvOnlineData;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;

public class ActivityDiscover {
    private EMVActivity nContext;
    private int _LastCode = 0;
    public ActivityDiscover(Context context) {
        this.nContext = (EMVActivity) context;
    }

    DpasListener dpasCTLListener = new DpasListener() {
        @Override
        public int OnDpasInitApp(int IsNeedExtendLogData, int CanModifyDeatil) {
            //TP_DbgSerialPrn("%s:%d,%d","OnDpasInitApp",IsNeedExtendLogData,CanModifyDeatil);

            return 0;
        }

        @Override
        public int OnDpasFinishReadData() {
            //TP_DbgSerialPrn("%s","OnDpasFinishReadData");
            return 0;
        }

        @Override
        public int OnDpasCheckException(String PAN) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int onDpasOnlineProcess(EmvOnlineData OnlineData) {
            return EmvService.EMV_TRUE;
        }

        @Override
        public int OnDpasReadDSListCheck(DpasDSObject obj) {
            //TP_DbgSerialPrn("%s:%s","ReadDS",StringUtil.bytesToHexString(obj.ContainerID));
            long containerID = 0;
            containerID |= (obj.ContainerID[0]&0xFF)<< 24 ;
            containerID |= (obj.ContainerID[1]&0xFF)<< 16 ;
            containerID |= (obj.ContainerID[2]&0xFF)<< 8 ;
            containerID |= (obj.ContainerID[3]&0xFF)<< 0 ;
            if( (containerID>=0x00000001) && (containerID<=0x00000018) ){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0x00000101) && (containerID<=0x00000118)){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0x00000201) && (containerID<=0x00000218)){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0x00000301) && (containerID<=0x00000318)){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0x00000401) && (containerID<=0x00000418)){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0x80000001) && (containerID<=0x80000018)){
                return EmvService.EMV_TRUE;
            }else if((containerID>=0xFFFFFFE7) && (containerID<=0xFFFFFFFF)){
                return EmvService.EMV_TRUE;
            }else if( containerID == 0x01010101 ||
                    containerID == 0x01010102 ||
                    containerID == 0x01010103 ||
                    containerID == 0x99999999 ||
                    containerID == 0xAABBCCDD ){
                return EmvService.EMV_TRUE;
            }

            return EmvService.EMV_FALSE;
        }

        @Override
        public int OnDpasFinishReadDSList(int Cnt, DpasDSObject[] objs) {
            return EmvService.EMV_TRUE;
        }
    };

    public boolean StartTransaction(double amount,EmvParam emvParam) {
        //set the listener
        nContext.emvService.setListener(dpasCTLListener);

        if(nContext.isDiscoverSwipeAgain){
            nContext.isDiscoverSwipeAgain = false;
            //Dpas_CTL_StartApp_2ndTap
            _LastCode = nContext.emvService.Dpas_CTL_StartApp_2ndTap();
            if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Dpas_CTL_StartApp_2ndTap fail,err code:" + _LastCode);
                return false;
            } else {
                nContext.AppendDis("Dpas_CTL_StartApp_2ndTap succ");
            }

            //Dpas_CTL_Get_Outcome
            DpasCTLOutCome outcome = new DpasCTLOutCome();
            nContext.emvService.Dpas_CTL_Get_Outcome(outcome);
            if(outcome.Result == DpasResult.DPASCTL_RESULT_APPROVED
                    || outcome.Result == DpasResult.DPASCTL_RESULT_ONLINE_APPROVED){
                nContext.AppendDis("Transaction Success");
                return true;
            }else{
                nContext.AppendDis("Transaction fail,err code:" + outcome.Result);
                return false;
            }
        }
        else {
            //set emv param
            EmvParam mEMVParam;
            mEMVParam = new EmvParam();

            _LastCode = nContext.emvService.Emv_SetParam(mEMVParam);
            if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Emv_SetParam fail,err code:" + _LastCode);
                return false;
            } else {
                nContext.AppendDis("Emv_SetParam succ");
            }

            //Dpas_CTL_Transaction_Init
            DpasParam dpasParam = new DpasParam();
            DpasAmount amt = new DpasAmount();
            amt.Amount = (long) amount * 100;
            _LastCode = nContext.emvService.Dpas_CTL_Transaction_Init(dpasParam, amt);

            //Dpas_CTL_StartApp
            _LastCode = nContext.emvService.Dpas_CTL_StartApp();
            if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Dpas_CTL_StartApp fail,err code:" + _LastCode);
                return false;
            } else {
                nContext.AppendDis("Dpas_CTL_StartApp succ");
            }

            //Dpas_CTL_Get_Outcome
            DpasCTLOutCome outcome = new DpasCTLOutCome();
            _LastCode = nContext.emvService.Dpas_CTL_Get_Outcome(outcome);

            if (outcome.IsNeedSecondTap == EmvService.EMV_TRUE) {
                nContext.isDiscoverSwipeAgain = true;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                nContext.AppendDis("Please tap the dpas card again");
                return false;
            }

            if(outcome.Result == DpasResult.DPASCTL_RESULT_APPROVED
                    || outcome.Result == DpasResult.DPASCTL_RESULT_ONLINE_APPROVED){
                nContext.AppendDis("Transaction Success");
                return true;
            }else{
                nContext.AppendDis("Transaction fail,err code:" + outcome.Result);
                return false;
            }
        }
    }
}
