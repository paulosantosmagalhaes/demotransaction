package com.pos.demosdk.EMV;

import android.content.Context;
import android.util.Log;

import com.telpo.emv.EmvApp;
import com.telpo.emv.EmvCAPK;
import com.telpo.emv.EmvService;
import com.telpo.emv.EmvTLV;
import com.telpo.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray.
 * <p>
 * Date: 2024/1/22
 * <p>
 * Description:
 */
public class EMVUtils {

    public static void addAID(EmvService emvService,String AID,String name){

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
        emvApp.FloorLimit = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x50, (byte) 0x00};//9F1B:FloorLimit
        emvApp.Threshold = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDenial = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACOnline = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.TACDefault = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        emvApp.AcquierId = new byte[]{(byte) 0x01, (byte) 0x22, (byte) 0x55, (byte) 0x66, (byte) 0x33, (byte) 0x40};
        emvApp.DDOL = new byte[]{(byte) 0x03, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.TDOL = new byte[]{(byte) 0x0F, (byte) 0x9F, (byte) 0x02, (byte) 0x06, (byte) 0x5F, (byte) 0x2A, (byte) 0x02, (byte) 0x9A, (byte) 0x03, (byte) 0x9C, (byte) 0x01, (byte) 0x95, (byte) 0x05, (byte) 0x9F, (byte) 0x37, (byte) 0x04};
        emvApp.Version = new byte[]{(byte) 0x00, (byte) 0x02};

        result = emvService.Emv_AddApp(emvApp);
        Log.i("addAID",name+" AID:"+ AID+" result:"+ result);

    }



    public static List<EmvTLV> getTLVCardDataTags() {
        List<EmvTLV> iccTags = new ArrayList<>();
        //  NFCTags.add(new EmvTLV(0x4F));//Application Identifier (ADF Name)
        iccTags.add(new EmvTLV(0x9F06)); //AID
        iccTags.add(new EmvTLV(0x57));//PAN
        iccTags.add(new EmvTLV(0x5A));//PAN
        iccTags.add(new EmvTLV(0x5F20));//持卡人姓名
        iccTags.add(new EmvTLV(0x5F24));//有效期
        iccTags.add(new EmvTLV(0x50));//Application Label
        iccTags.add(new EmvTLV(0x5F2A));//Transaction Currency Code
        //    NFCTags.add(new EmvTLV(0x8F));//Certification Authority Public Key Index
        //   NFCTags.add(new EmvTLV(0x8E));//Cardholder Verification Method (CVM) List
        // NFCTags.add(new EmvTLV(0x84));//Dedicated File (DF) Name
        // NFCTags.add(new EmvTLV(0x82));//Application Interchange Profile (AIP)
        // NFCTags.add(new EmvTLV(0x95));//Terminal Verification Results (TVR)
        // NFCTags.add(new EmvTLV(0x9A));//Transaction Date
        // NFCTags.add(new EmvTLV(0x9C));//Transaction Type
        // NFCTags.add(new EmvTLV(0x9F02));//Amount, Authorised (Numeric)
        // NFCTags.add(new EmvTLV(0x9F03));//Amount, Other (Numeric)
        // NFCTags.add(new EmvTLV(0x9F10));//Issuer Application Data (IAD)
        iccTags.add(new EmvTLV(0x9F1A));//Terminal Country Code
        iccTags.add(new EmvTLV(0x9F1B));//Terminal Floor Limit
        //  NFCTags.add(new EmvTLV(0x9F26));//Application Cryptogram
        // NFCTags.add(new EmvTLV(0x9F27));//Cryptogram Information Data (CID)
        iccTags.add(new EmvTLV(0x9F33));//Terminal Capabilities
        iccTags.add(new EmvTLV(0x9F34));//Cardholder Verification Method (CVM) Results
        iccTags.add(new EmvTLV(0x9F35));//Terminal Type
        //  NFCTags.add(new EmvTLV(0x9F36));//Application Transaction Counter (ATC)
        // NFCTags.add(new EmvTLV(0x9F4C));//ICC Dynamic Number
        //  NFCTags.add(new EmvTLV(0x9F1E));//Interface Device (IFD) Serial Number
        iccTags.add(new EmvTLV(0x9F09));//Application Version Number
        // NFCTags.add(new EmvTLV(0x9F41));//Transaction Sequence Counter
        return iccTags;
    }

    public static List<EmvTLV> getTLVContactlessCardDataTags() {
        List<EmvTLV> NFCTags = new ArrayList<>();
      //  NFCTags.add(new EmvTLV(0x4F));//Application Identifier (ADF Name)
        NFCTags.add(new EmvTLV(0x9F06)); //AID
        NFCTags.add(new EmvTLV(0x57));//PAN
        NFCTags.add(new EmvTLV(0x5A));//PAN
        NFCTags.add(new EmvTLV(0x5F20));//持卡人姓名
        NFCTags.add(new EmvTLV(0x5F24));//有效期
        NFCTags.add(new EmvTLV(0x50));//Application Label
        NFCTags.add(new EmvTLV(0x5F2A));//Transaction Currency Code
    //    NFCTags.add(new EmvTLV(0x8F));//Certification Authority Public Key Index
     //   NFCTags.add(new EmvTLV(0x8E));//Cardholder Verification Method (CVM) List
       // NFCTags.add(new EmvTLV(0x84));//Dedicated File (DF) Name
       // NFCTags.add(new EmvTLV(0x82));//Application Interchange Profile (AIP)
       // NFCTags.add(new EmvTLV(0x95));//Terminal Verification Results (TVR)
       // NFCTags.add(new EmvTLV(0x9A));//Transaction Date
       // NFCTags.add(new EmvTLV(0x9C));//Transaction Type
       // NFCTags.add(new EmvTLV(0x9F02));//Amount, Authorised (Numeric)
       // NFCTags.add(new EmvTLV(0x9F03));//Amount, Other (Numeric)
       // NFCTags.add(new EmvTLV(0x9F10));//Issuer Application Data (IAD)
        NFCTags.add(new EmvTLV(0x9F1A));//Terminal Country Code
        NFCTags.add(new EmvTLV(0x9F1B));//Terminal Floor Limit
      //  NFCTags.add(new EmvTLV(0x9F26));//Application Cryptogram
       // NFCTags.add(new EmvTLV(0x9F27));//Cryptogram Information Data (CID)
        NFCTags.add(new EmvTLV(0x9F33));//Terminal Capabilities
        NFCTags.add(new EmvTLV(0x9F34));//Cardholder Verification Method (CVM) Results
        NFCTags.add(new EmvTLV(0x9F35));//Terminal Type
      //  NFCTags.add(new EmvTLV(0x9F36));//Application Transaction Counter (ATC)
       // NFCTags.add(new EmvTLV(0x9F4C));//ICC Dynamic Number
      //  NFCTags.add(new EmvTLV(0x9F1E));//Interface Device (IFD) Serial Number
        NFCTags.add(new EmvTLV(0x9F09));//Application Version Number
       // NFCTags.add(new EmvTLV(0x9F41));//Transaction Sequence Counter

        return NFCTags;
    }


    public static String getAgencyKeyPath(Context mContext) {
        Class<?> clazz;
        Method method;
        Object obj;
        String path = null;
        try{
            clazz = Class.forName("com.common.sdk.security.SecurityServiceManager");
            obj = mContext.getSystemService("security");
            method = clazz.getMethod("getAgencyKeyPath");
            method.setAccessible(true);
            if (obj!=null){
                path = (String) method.invoke(obj);
            }
        }catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException var1){
            var1.printStackTrace();
        }
        return path;
    }


    public static String getAgencyKey(Context mContext) {
        Class<?> clazz;
        Method method;
        Object obj;
        String agencyKey = null;
        try{
            clazz = Class.forName("com.common.sdk.security.SecurityServiceManager");
            obj = mContext.getSystemService("security");
            method = clazz.getMethod("getAgencyKey");
            method.setAccessible(true);
            if (obj!=null){
                agencyKey = (String) method.invoke(obj);
            }
        }catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException var1){
            var1.printStackTrace();
        }
        return agencyKey;
    }

}
