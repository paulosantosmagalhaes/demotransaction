package com.pos.demosdk.EMV;

import android.util.Log;

import com.telpo.emv.EmvApp;
import com.telpo.emv.EmvService;
import com.telpo.util.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/5/13
 * <p>
 * Description:
 */
public class AIDUtil {


    public static void addAllAID(EmvService emvService){
        Add_Pure_AID(emvService);
        Add_Visa_AID(emvService);
        Add_Master_AID(emvService);
        Add_Unionpay_AID(emvService);
        Add_Amex_AID(emvService);
        Add_MIR_AID(emvService);
        Add_Multicaixa_AID(emvService);
    }

    public static void Add_Multicaixa_AID(EmvService emvService) {

        //修改参数  默认半匹配
        addVisaAID(emvService,"A0000006900200","Multicaixa");
      /* 全匹配则用下面参数
        addVisaAID(emvService,"A00000000305076010","Visa");
        addVisaAID(emvService,"A0000000031010","Visa");
        addVisaAID(emvService,"A000000003101001","Visa");
        addVisaAID(emvService,"A000000003101002","Visa");
        addVisaAID(emvService,"A0000000032010","Visa");
        addVisaAID(emvService,"A0000000032020","Visa");
        addVisaAID(emvService,"A0000000033010","Visa");
        addVisaAID(emvService,"A0000000034010","Visa");
        addVisaAID(emvService,"A0000000035010","Visa");
        addVisaAID(emvService,"A0000000036010","Visa");
        addVisaAID(emvService,"A0000000036020","Visa");
        addVisaAID(emvService,"A0000000038002","Visa");
        addVisaAID(emvService,"A0000000038010","Visa");
        addVisaAID(emvService,"A0000000039010","Visa");
        addVisaAID(emvService,"A000000003999910","Visa");*/

    }
    public static void Add_Visa_AID(EmvService emvService) {

        //修改参数  默认半匹配
        addVisaAID(emvService,"A000000003","Visa");
      /* 全匹配则用下面参数
        addVisaAID(emvService,"A00000000305076010","Visa");
        addVisaAID(emvService,"A0000000031010","Visa");
        addVisaAID(emvService,"A000000003101001","Visa");
        addVisaAID(emvService,"A000000003101002","Visa");
        addVisaAID(emvService,"A0000000032010","Visa");
        addVisaAID(emvService,"A0000000032020","Visa");
        addVisaAID(emvService,"A0000000033010","Visa");
        addVisaAID(emvService,"A0000000034010","Visa");
        addVisaAID(emvService,"A0000000035010","Visa");
        addVisaAID(emvService,"A0000000036010","Visa");
        addVisaAID(emvService,"A0000000036020","Visa");
        addVisaAID(emvService,"A0000000038002","Visa");
        addVisaAID(emvService,"A0000000038010","Visa");
        addVisaAID(emvService,"A0000000039010","Visa");
        addVisaAID(emvService,"A000000003999910","Visa");*/

    }
    public static void Add_Master_AID(EmvService emvService) {
        addMasterAID(emvService,"A000000004","Master");
        /*
        addMasterAID(emvService,"A00000000401","Master");
        addMasterAID(emvService,"A0000000041010","Master");
        addMasterAID(emvService,"A00000000410101213","Master");
        addMasterAID(emvService,"A00000000410101215","Master");
        addMasterAID(emvService,"A0000000042010","Master");
        addMasterAID(emvService,"A0000000043010","Master");
        addMaestroAID(emvService,"A0000000043060","Maestro");
        addMaestroAID(emvService,"A000000004306001","Maestro");
        addMasterAID(emvService,"A0000000044010","Master");
        addMasterAID(emvService,"A0000000045010","Master");
        addMasterAID(emvService,"A0000000046000","Master");
        addMasterAID(emvService,"A0000000048002","Master");
        addMasterAID(emvService,"A0000000049999","Master");
        addMasterAID(emvService,"A0000000049999","Master");*/

    }
    public static void Add_Unionpay_AID(EmvService emvService) {
        addUPIAID(emvService,"A000000333","Unionpay");

       /* addUPIAID(emvService,"A000000333010101","Unionpay");
        addUPIAID(emvService,"A000000333010102","Unionpay");
        addUPIAID(emvService,"A000000333010103","Unionpay");
        addUPIAID(emvService,"A000000333010106","Unionpay");
        addUPIAID(emvService,"A000000333010108","Unionpay");*/
    }

    public static void Add_Pure_AID(EmvService emvService) {
        addPureAID(emvService,"A000000371","Pure");//  尼日利亚 A0000003710001
       // addAID(emvService,"D9999999991010","Pure");
      //  addAID(emvService,"D9999999992020","Pure");
        //addAID(emvService,"D666666666","Pure");
    }

    public static void Add_MIR_AID(EmvService emvService) {
        addMirAID(emvService,"A000000658","MIR");
    }


    // Amex单独参数添加
    public static void Add_Amex_AID(EmvService emvService) {
        addAmexAID(emvService,"A000000025","Amex");
        /*addAmexAID(emvService,"A0000000250000","Amex");
        addAmexAID(emvService,"A00000002501","Amex");
        addAmexAID(emvService,"A000000025010104","Amex");
        addAmexAID(emvService,"A000000025010402","Amex");
        addAmexAID(emvService,"A000000025010701","Amex");
        addAmexAID(emvService,"A000000025010801","Amex");
        addAmexAID(emvService,"A00000002504","Amex");*/
    }




    public static void addPureAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0010000000
        emvApp.TACOnline = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x04, (byte) 0xF8, (byte) 0x00};//D84004F800
        emvApp.TACDefault = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x00, (byte) 0xA8, (byte) 0x00};//D84000A800
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x01};
        emvApp.CtlKernelCaps=new byte[]{0x36,0x00,0x60,0x43,(byte) 0xF9};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addMirAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0010000000
        emvApp.TACOnline = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x04, (byte) 0xF8, (byte) 0x00};//D84004F800
        emvApp.TACDefault = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x00, (byte) 0xA8, (byte) 0x00};//D84000A800
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x01};
        emvApp.CtlKernelCaps=new byte[]{0x36,0x00,0x60,0x43,(byte) 0xF9};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addVisaAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0010000000
        emvApp.TACOnline = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x04, (byte) 0xF8, (byte) 0x00};//D84004F800  DC4004F800
        emvApp.TACDefault = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x00, (byte) 0xA8, (byte) 0x00};//D84000A800  DC4000A800
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addMasterAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0000000000
        emvApp.TACOnline = new byte[]{(byte) 0xF4, (byte) 0x50, (byte) 0x84, (byte) 0x80, (byte) 0x0C};//F45084800C
        emvApp.TACDefault = new byte[]{(byte) 0xF4, (byte) 0x50, (byte) 0x84, (byte) 0x80, (byte) 0x0C};//F45084800C
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addMaestroAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00};//0000800000
        emvApp.TACOnline = new byte[]{(byte) 0xF4, (byte) 0x50, (byte) 0x84, (byte) 0x80, (byte) 0x0C};//F45084800C
        emvApp.TACDefault = new byte[]{(byte) 0xF4, (byte) 0x50, (byte) 0x84, (byte) 0x80, (byte) 0x0C};//F45084800C
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addAmexAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0010000000
        emvApp.TACOnline = new byte[]{(byte) 0xDE, (byte) 0x00, (byte) 0xFC, (byte) 0x98, (byte) 0x00};//DE00FC9800
        emvApp.TACDefault = new byte[]{(byte) 0xDC, (byte) 0x50, (byte) 0xFC, (byte) 0x98, (byte) 0x00};//DC50FC9800
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }

    public static void addUPIAID(EmvService emvService, String AID, String name){

        int result = 0;

        EmvApp emvApp = new EmvApp();
        try {
            emvApp.AppName = name.getBytes("ascii");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        emvApp.AID = StringUtil.hexStringToByte(AID);
        emvApp.SelFlag = (byte) 0x00;
        emvApp.Priority = (byte) 0x00;
        emvApp.TargetPer = (byte) 0;
        emvApp.MaxTargetPer = (byte) 0;
        emvApp.FloorLimitCheck = (byte) 1;
        emvApp.RandTransSel = (byte) 1;
        emvApp.VelocityCheck = (byte) 1;
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};//9F1B:FloorLimit 000000
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00};//0010000000
        emvApp.TACOnline = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x04, (byte) 0xF8, (byte) 0x00};//D84004F800
        emvApp.TACDefault = new byte[]{(byte) 0xD8, (byte) 0x40, (byte) 0x00, (byte) 0xA8, (byte) 0x00};//D84000A800
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }


}
