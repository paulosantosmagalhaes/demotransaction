package com.pos.demosdk.EMV;

import android.content.Context;

import com.pos.demosdk.MainActivity;
import com.telpo.emv.EmvParam;
import com.telpo.emv.EmvService;
import com.telpo.emv.PureAmount;
import com.telpo.emv.PureLimits;
import com.telpo.emv.PureListener;
import com.telpo.emv.PureOnlineData;
import com.telpo.emv.PureOutCome;
import com.telpo.emv.PureParam;
import com.telpo.emv.PureResult;
import com.telpo.emv.PureUserInterface;

public class ActivityPure {
    private EMVActivity nContext;
    private int _LastCode = 0;
    PureOnlineData pureOnlineData;

    public ActivityPure(Context context) {
        this.nContext = (EMVActivity) context;
    }

    PureListener listener = new PureListener() {
        @Override
        public int onPure_InitApp() {
            return 0;
        }

        @Override
        public int onPure_Check_Exception(int PSN, String PAN) {
            return 0;
        }

        @Override
        public int onPure_SendMsg(int MessID, int Status) {
            return 0;
        }

        @Override
        public int onPure_2ndTap(int msg) {
            return 0;
        }

    };

    public boolean StartTransaction(double amount) {
        //set the listener
        nContext.emvService.setListener(listener);

        //set emv param
        EmvParam param = new EmvParam();
        param.CountryCode=new byte[]{(byte) 0x01, (byte) 0x56};//156  EmvCountryCode
        param.Capability = new byte[]{(byte) 0xE0, (byte) 0xF9, (byte) 0xC8};
        param.TransType = (byte) 0x00;     //当前交易类型
        param.TransLimt = 0;   // 交易金融 《 有可能是离线或者免密
        param.TerminalType = (byte) 0x22; //终端类型


        param.NFC_OffLineFloorLimit = 0; //>非接最低限额
        param.NFC_CVMLimit = 0;// >此金融需要持卡人验证
        param.NFC_TransLimit = 2147483647;


        _LastCode = nContext.emvService.Emv_SetParam(param);
        if (EmvService.EMV_TRUE != _LastCode) {
            nContext.AppendDis("Emv_SetParam fail,err code:" + _LastCode);
            return false;
        } else {
            nContext.AppendDis("Emv_SetParam succ");
        }

        if (nContext.isPureSwipeAgain) {
            //Pure_ProcTransComplete
            String RspCode = "";
            String IAD = "";
            String Script71 = "";
            String Script72 = "";
            pureOnlineData = new PureOnlineData(RspCode, IAD, Script71, Script72);
            nContext.emvService.Pure_ProcTransComplete(pureOnlineData);
            nContext.isPureSwipeAgain = false;
        }else {
            //Pure_TransInit
            PureParam pureParam = new PureParam();

            PureAmount pureAmount = new PureAmount();
            pureAmount.Amount = (long) (amount * 100);
            pureParam.TerminalType = 34;
            pureAmount.CurrCode=156;
            pureParam.TermCountryCode = 156;

            nContext.emvService.Pure_DynamicLimit_Clear();
            PureLimits limit = new PureLimits("A0000003710001",2147483647,1000,10000,0, (byte) 0x01, (byte) 0x01);
            nContext.emvService.Pure_DynamicLimit_Add(limit);

            _LastCode = nContext.emvService.Pure_Transaction_Init(pureParam, pureAmount);
            if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Pure_TransInit fail, err code:" + _LastCode);
                return false;
            } else {
                nContext.AppendDis("Pure_TransInit succ!");
            }

            //Pure_Preprocess
            _LastCode = nContext.emvService.Pure_Preprocess();
            if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Pure_Preprocess fail,err code:" + _LastCode);
                return false;
            }
            nContext.AppendDis("Pure_Preprocess succ");

            //Pure_StartApp
            _LastCode = nContext.emvService.Pure_StartApp();
          //  if (EmvService.EMV_TRUE != _LastCode) {
                nContext.AppendDis("Pure_StartApp  code:" + _LastCode);
              //  return false;
           // }
        }
        PureOutCome outCome = new PureOutCome();
        PureUserInterface userInterface = new PureUserInterface();

        nContext.emvService.Pure_Get_Outcome(outCome, userInterface);


        if (outCome.CVM== PureResult.PURE_CVM_ONLINEPIN){//设置是否免密
          //need pin   call pinpad
        }else {
           //no need pin
        }

        if (outCome.CVM== PureResult.PURE_CVM_SIGNATURE){
            //need sign
        }else {
            //no need sign
        }

        if (outCome.Status==PureResult.PURE_STATUS_APPROVED){//离线批准
            nContext.AppendDis("Offline Transaction succ"); //offline

        }else if (outCome.Status==PureResult.PURE_STATUS_DECLINED){
            nContext.AppendDis("Offline Transaction DECLINED"); //offline
        }else if (outCome.Status== PureResult.PURE_STATUS_ONLINE){
            //online 报文
            nContext.AppendDis("Online"); //

        }else if (outCome.Status== PureResult.PURE_STATUS_TRYAGAIN){
            nContext.AppendDis("Pure_StartApp TRYAGAIN:" + outCome.Status);
            return false;
        } else {//交易失败
            nContext.AppendDis("Pure_StartApp Fail:" + outCome.Status);
            return false;
        }

        return true;
    }
}
