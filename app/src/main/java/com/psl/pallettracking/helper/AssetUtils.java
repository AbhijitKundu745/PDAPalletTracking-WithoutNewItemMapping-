package com.psl.pallettracking.helper;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.psl.pallettracking.R;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessModel;
import com.psl.pallettracking.adapters.DashboardModel;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetUtils {
    public static final String TYPE_PALLET = "PalletTag";
    public static final String TYPE_BEAN = "Bintag";
    public static final String TYPE_TEMPORARY_STORAGE = "TS";
    public static final String TYPE_LOADING_AREA = "LA";
    public static final String TYPE_OTHER = "NA";
    public static String getTagType(String str) {
        if (str.length() > 4) {
            switch (str.substring(2, 4)) {
                case "02":
                    return TYPE_PALLET;
                case "03":
                    return TYPE_BEAN;
                case "04":
                    return TYPE_TEMPORARY_STORAGE;
                case "05":
                    return TYPE_LOADING_AREA;
            }
        }
        return TYPE_OTHER;
    }
    public static boolean isItemAlreadyAdded(String itemDesc, List<BinPartialPalletMappingCreationProcessModel> workOrderListItemList) {
        boolean isPresent = false;
        if (workOrderListItemList != null) {
            for (BinPartialPalletMappingCreationProcessModel item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (itemDesc.equals(item.getBinDescription())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    isPresent = true;
                    break;

                }
            }
        }
        return isPresent;
    }
    public static List<BinPartialPalletMappingCreationProcessModel> getUpdatedOrderList(String itemDesc, List<BinPartialPalletMappingCreationProcessModel> workOrderListItemList,int removedqty) {
        ;if (workOrderListItemList != null) {
            for (BinPartialPalletMappingCreationProcessModel item : workOrderListItemList) {
                int count = 0;
                // Check if the string is equal to any of the fields of the current item
                if (itemDesc.equals(item.getBinDescription())) {

                    // If found, retrieve the loadingAreaTagId and break the loop
                    BinPartialPalletMappingCreationProcessModel obj = item;
                    workOrderListItemList.remove(count);
                    int originalQty = item.getPickedQty();
                    int diff = originalQty-removedqty;
                    obj.setPickedQty(diff);
                    obj = item;
                    workOrderListItemList.add(obj);
                    break;

                }
                count++;
            }
        }
        return workOrderListItemList;
    }

    public static BinPartialPalletMappingCreationProcessModel getItemObject(String itemDesc, String binName, String itemID, List<BinPartialPalletMappingCreationProcessModel> workOrderListItemList) {
        BinPartialPalletMappingCreationProcessModel obj = null;
        if (workOrderListItemList != null) {
            for (BinPartialPalletMappingCreationProcessModel item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (itemDesc.equals(item.getBinDescription()) && binName.equals(item.getBinNumber()) && itemID.equals(item.getItemID())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    obj = item;
                    break;

                }
            }
        }
        return obj;
    }

    public static int getItemPosition(String itemDesc, List<BinPartialPalletMappingCreationProcessModel> workOrderListItemList) {
        int objCnt = -1;
        int cnt =  0;
        if (workOrderListItemList != null) {
            for (BinPartialPalletMappingCreationProcessModel item : workOrderListItemList) {
                // Check if the string is equal to any of the fields of the current item
                if (itemDesc.equals(item.getBinDescription())) {
                    // If found, retrieve the loadingAreaTagId and break the loop
                    objCnt = cnt;
                    break;

                }
            }
            cnt++;
        }
        return objCnt;
    }
    public static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }


    public static List<DashboardModel> getDashboardDetails(){
        List<DashboardModel> dashboardlist = new ArrayList<>();
        DashboardModel dashboardModel0 = new DashboardModel();
        dashboardModel0.setMenuName(AppConstants.DASHBOARD_MENU_ASSET_PALLET_MAPPING_IN);
        dashboardModel0.setDrawableImage(R.mipmap.ic_launcher_inwardpallet_foreground);
        dashboardModel0.setMenuId(AppConstants.MENU_ID_CARTON_PALLET_MAPPING);
        dashboardModel0.setMenuimageName("");
        dashboardModel0.setMenuSequence("1");
        dashboardModel0.setIsMenuActive("true");

        DashboardModel dashboardModel01 = new DashboardModel();
        dashboardModel01.setMenuName(AppConstants.DASHBOARD_MENU_ASSET_PALLET_MAPPING_OUT);
        dashboardModel01.setDrawableImage(R.mipmap.ic_launcher_outwardpallet_foreground);
        dashboardModel01.setMenuId(AppConstants.MENU_ID_ITEM_PALLET_MAPPING);
        dashboardModel01.setMenuimageName("");
        dashboardModel01.setMenuSequence("2");
        dashboardModel01.setIsMenuActive("true");

        DashboardModel dashboardModel1 = new DashboardModel();
        dashboardModel1.setMenuName(AppConstants.DASHBOARD_MENU_PALLET_MOVEMENT);
        dashboardModel1.setDrawableImage(R.mipmap.ic_launcher_palletmovement_foreground);
        dashboardModel1.setMenuId(AppConstants.MENU_ID_PALLET_MOVEMENT);
        dashboardModel1.setMenuimageName("");
        dashboardModel1.setMenuSequence("3");
        dashboardModel1.setIsMenuActive("true");

        DashboardModel dashboardModel2 = new DashboardModel();
        dashboardModel2.setMenuName(AppConstants.DASHBOARD_MENU_PARTIAL_LOADING);
        dashboardModel2.setDrawableImage(R.drawable.dispatchpalletcreation);
        dashboardModel2.setMenuId(AppConstants.MENU_ID_PARTIAL);
        dashboardModel2.setMenuimageName("");
        dashboardModel2.setMenuSequence("4");
        dashboardModel2.setIsMenuActive("true");

        DashboardModel dashboardModel3 = new DashboardModel();
        dashboardModel3.setMenuName(AppConstants.DASHBOARD_MENU_ITEM_MOVEMENT);
        dashboardModel3.setDrawableImage(R.mipmap.ic_launcher_itemmovement);
        dashboardModel3.setMenuId(AppConstants.MENU_ID_ITEM_MOVEMENT);
        dashboardModel3.setMenuimageName("");
        dashboardModel3.setMenuSequence("5");
        dashboardModel3.setIsMenuActive("true");

      /*  DashboardModel dashboardModel3 = new DashboardModel();
        dashboardModel3.setMenuName(AppConstants.DASHBOARD_MENU_CHECKIN);
        dashboardModel3.setDrawableImage(R.drawable.checkin);
        dashboardModel3.setMenuId(AppConstants.MENU_ID_CHECKIN);
        dashboardModel3.setMenuimageName("");
        dashboardModel3.setMenuSequence("4");
        dashboardModel3.setIsMenuActive("true");

        DashboardModel dashboardModel4 = new DashboardModel();
        dashboardModel4.setMenuName(AppConstants.DASHBOARD_MENU_CHECKOUT);
        dashboardModel4.setDrawableImage(R.drawable.checkout);
        dashboardModel4.setMenuId(AppConstants.MENU_ID_CHECKOUT);
        dashboardModel4.setMenuimageName("");
        dashboardModel4.setMenuSequence("5");
        dashboardModel4.setIsMenuActive("true");*/

        DashboardModel dashboardModelSync = new DashboardModel();
        dashboardModelSync.setMenuName(AppConstants.DASHBOARD_MENU_SYNC);
        dashboardModelSync.setDrawableImage(R.mipmap.ic_launcher_sync_foreground);
        dashboardModelSync.setMenuId(AppConstants.MENU_ID_ASSETSYNC);
        dashboardModelSync.setMenuimageName("");
        dashboardModelSync.setMenuSequence("6");
        dashboardModelSync.setIsMenuActive("true");

       /* DashboardModel dashboardModel6 = new DashboardModel();
        dashboardModel6.setMenuName(AppConstants.DASHBOARD_MENU_ROOMCHECKOUT);
        dashboardModel6.setDrawableImage(R.drawable.checkout);
        dashboardModel6.setMenuId(AppConstants.MENU_ID_ROOMCHECKOUT);
        dashboardModel6.setMenuimageName("");
        dashboardModel6.setMenuSequence("6");
        dashboardModel6.setIsMenuActive("true");

        DashboardModel dashboardModel7 = new DashboardModel();
        dashboardModel7.setMenuName(AppConstants.DASHBOARD_MENU_TRACK_POINT);
        dashboardModel7.setDrawableImage(R.drawable.checkout);
        dashboardModel7.setMenuId(AppConstants.MENU_ID_TRACKPOINT);
        dashboardModel7.setMenuimageName("");
        dashboardModel7.setMenuSequence("7");
        dashboardModel7.setIsMenuActive("true");

        DashboardModel dashboardModel8 = new DashboardModel();
        dashboardModel8.setMenuName(AppConstants.DASHBOARD_MENU_SECURITYOUT);
        dashboardModel8.setDrawableImage(R.drawable.checkout);
        dashboardModel8.setMenuId(AppConstants.MENU_ID_SECURITYOUT);
        dashboardModel8.setMenuimageName("");
        dashboardModel8.setMenuSequence("8");
        dashboardModel8.setIsMenuActive("true");*/

        dashboardlist.add(dashboardModel0);
        dashboardlist.add(dashboardModel01);
        dashboardlist.add(dashboardModel1);
        dashboardlist.add(dashboardModel2);
        dashboardlist.add(dashboardModel3);
        //dashboardlist.add(dashboardModel2);
      /*  dashboardlist.add(dashboardModel3);
        dashboardlist.add(dashboardModel4);

        dashboardlist.add(dashboardModel6);
        dashboardlist.add(dashboardModel7);
        Log.e("7","Added");
        dashboardlist.add(dashboardModel8);
        Log.e("8","Added");*/

        dashboardlist.add(dashboardModelSync);

        return dashboardlist;

    }

   /* public static string Encrypt(string toEncrypt)
    {
        byte[] keyArray;
        byte[] toEncryptArray = UTF8Encoding.UTF8.GetBytes(toEncrypt);

        //System.Configuration.AppSettingsReader settingsReader = new System.Configuration.AppSettingsReader();
        // Get the key from config file
        string key = "OTS";
        //System.Windows.Forms.MessageBox.Show(key);

        MD5CryptoServiceProvider hashmd5 = new MD5CryptoServiceProvider();
        keyArray = hashmd5.ComputeHash(UTF8Encoding.UTF8.GetBytes(key));
        hashmd5.Clear();


        TripleDESCryptoServiceProvider tdes = new TripleDESCryptoServiceProvider();
        tdes.Key = keyArray;
        tdes.Mode = CipherMode.ECB;
        tdes.Padding = PaddingMode.PKCS7;

        ICryptoTransform cTransform = tdes.CreateEncryptor();
        byte[] resultArray = cTransform.TransformFinalBlock(toEncryptArray, 0, toEncryptArray.Length);
        tdes.Clear();
        return Convert.ToBase64String(resultArray, 0, resultArray.Length);

    }
*/

    public static boolean isStringContainsOnlyNumbers(String str) {
        // Regex to check string
        // contains only digits
        String regex = "[0-9]+";
        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (str == null) {
            return false;
        }

        // Find match between given string
        // and regular expression
        // using Pattern.matcher()
        Matcher m = p.matcher(str);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }
    public static String get2DigitNumber(String number){
        String newnumber = number.trim();
        if(number.length() == 1){
            newnumber = "0"+number;
        }
        return newnumber;
    }

    static ProgressDialog progressDialog;

    /**
     * method to show Progress Dialog
     */
    public static void showProgress(Context context, String progress_message) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(progress_message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        //progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * method to hide Progress Dialog
     */
    public static void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static int getPercentage(int value) {
        int a = 0;
        switch (value) {

            case 39:
                a = 99;
                break;
            case 40:
                a = 98;
                break;
            case 41:
                a = 97;
                break;
            case 42:
                a = 96;
                break;
            case 43:
                a = 94;
                break;
            case 44:
                a = 92;
                break;
            case 45:
                a = 90;
                break;
            case 46:
                a = 89;
                break;
            case 47:
                a = 87;
                break;
            case 48:
                a = 85;
                break;
            case 49:
                a = 84;
                break;
            case 50:
                a = 82;
                break;
            case 51:
                a = 79;
                break;
            case 52:
                a = 75;
                break;
            case 53:
                a = 72;
                break;
            case 54:
                a = 70;
                break;
            case 55:
                a = 67;
                break;
            case 56:
                a = 65;
                break;
            case 57:
                a = 62;
                break;
            case 58:
                a = 60;
                break;
            case 59:
                a = 57;
                break;
            case 60:
                a = 54;
                break;
            case 61:
                a = 51;
                break;
            case 62:
                a = 48;
                break;
            case 63:
                a = 43;
                break;
            case 64:
                a = 40;
                break;
            case 65:
                a = 36;
                break;
            case 66:
                a = 33;
                break;
            case 67:
                a = 31;
                break;
            case 68:
                a = 29;
                break;
            case 69:
                a = 27;
                break;
            case 70:
                a = 25;
                break;
            case 71:
                a = 23;
                break;
            case 72:
                a = 21;
                break;
            case 73:
                a = 19;
                break;
            case 74:
                a = 17;
                break;
            case 75:
                a = 15;
                break;
            case 76:
                a = 13;
                break;
            case 77:
                a = 11;
                break;
            case 78:
                a = 10;
                break;
            case 79:
                a = 8;
                break;
            case 80:
                a = 7;
                break;
            case 81:
                a = 6;
                break;
            case 82:
                a = 5;
                break;
            case 83:
                a = 4;
                break;
            case 84:
                a = 3;
                break;
            case 85:
                a = 2;
                break;
            case 86:
                a = 1;
                break;
        }
        return a;
    }


    static BottomSheetDialog bottomSheetDialog;

    public static void showCommonBottomSheetErrorDialog(Context context, String message) {
        try {
            if (bottomSheetDialog != null&& bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.custom_bottom_dialog_layout);
            TextView textmessage = bottomSheetDialog.findViewById(R.id.textMessage);
            textmessage.setText(message);
            textmessage.setBackgroundColor(context.getResources().getColor(R.color.red));
            bottomSheetDialog.show();
            new CountDownTimer(2500, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onFinish() {
                    // TODO Auto-generated method stub
                    dismissDialog();
                }
            }.start();
        }catch (Exception e){}
    }


    public static void showCommonBottomSheetSuccessDialog(Context context, String message) {
        try {
            if (bottomSheetDialog != null&& bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.custom_bottom_dialog_layout);
            TextView textmessage = bottomSheetDialog.findViewById(R.id.textMessage);
            textmessage.setText(message);
            textmessage.setBackgroundColor(context.getResources().getColor(R.color.green));
            bottomSheetDialog.show();
            new CountDownTimer(2000, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onFinish() {
                    // TODO Auto-generated method stub
                    dismissDialog();
                }
            }.start();
        }catch (Exception e){}
    }
    public static void dismissDialog() {
        try {
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
            bottomSheetDialog = null; // Reset the dialog reference
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getSystemDateTimeInFormatt() {
        try {
            int year, monthformat, dateformat, sec;
            String da, mont, hor, min, yr, systemDate, secs;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            monthformat = calendar.get(Calendar.MONTH) + 1;
            dateformat = calendar.get(Calendar.DATE);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            sec = calendar.get(Calendar.SECOND);
            da = Integer.toString(dateformat);
            mont = Integer.toString(monthformat);
            hor = Integer.toString(hours);
            min = Integer.toString(minutes);
            secs = Integer.toString(sec);
            if (da.trim().length() == 1) {
                da = "0" + da;
            }
            if(mont.trim().equals("1")){
                mont = "01";
            }
            if(mont.trim().equals("2")){
                mont = "02";
            }
            if(mont.trim().equals("3")){
                mont = "03";
            }
            if(mont.trim().equals("4")){
                mont = "04";
            }
            if(mont.trim().equals("5")){
                mont = "05";
            }
            if(mont.trim().equals("6")){
                mont = "06";
            }
            if(mont.trim().equals("7")){
                mont = "07";
            }
            if(mont.trim().equals("8")){
                mont = "08";
            }
            if(mont.trim().equals("9")){
                mont = "09";
            }
            if(mont.trim().equals("10")){
                mont = "10";
            }
            if(mont.trim().equals("11")){
                mont = "11";
            }
            if(mont.trim().equals("12")){
                mont = "12";
            }
           /* if (mont.trim().equals("1")) {
                mont = "Jan";
            }
            if (mont.trim().equals("2")) {
                mont = "Feb";
            }
            if (mont.trim().equals("3")) {
                mont = "Mar";
            }
            if (mont.trim().equals("4")) {
                mont = "Apr";
            }
            if (mont.trim().equals("5")) {
                mont = "May";
            }
            if (mont.trim().equals("6")) {
                mont = "Jun";
            }
            if (mont.trim().equals("7")) {
                mont = "Jul";
            }
            if (mont.trim().equals("8")) {
                mont = "Aug";
            }
            if (mont.trim().equals("9")) {
                mont = "Sep";
            }
            if (mont.trim().equals("10")) {
                mont = "Oct";
            }
            if (mont.trim().equals("11")) {
                mont = "Nov";
            }
            if (mont.trim().equals("12")) {
                mont = "Dec";
            }*/
            if (hor.trim().length() == 1) {
                hor = "0" + hor;
            }
            if (min.trim().length() == 1) {
                min = "0" + min;
            }
            if (secs.trim().length() == 1) {
                secs = "0" + secs;
            }
            yr = Integer.toString(year);
            // systemDate = (da + mont + yr + hor + min + secs);
            systemDate = (yr + "-" + mont + "-" + da + " " + hor + ":" + min + ":" + secs);
            return systemDate;
        } catch (Exception e) {
            // return "01011970000000";
            // return "1970-01-01 00:00:00";
            return "1970-01-01 00:00:00";
        }
    }

    public static String getUTCSystemDateTimeInFormatt() {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            Log.e("UTCDATETIME1",f.format(new Date()));
           // f.setTimeZone(TimeZone.getTimeZone("GMT"));
            System.out.println(f.format(new Date()));
            Log.e("UTCDATETIME2",f.format(new Date()));
           String utcdatetime = f.format(new Date());
           return utcdatetime;
        } catch (Exception e) {
            // return "01011970000000";
            // return "1970-01-01 00:00:00";
            return "1970-01-01 00:00:00";
        }
    }
    public static String getCurrentSystemDate() {
        try {
            int year, monthformat, dateformat, sec;
            String da, mont, hor, min, yr, systemDate, secs;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            year = calendar.get(Calendar.YEAR);
            monthformat = calendar.get(Calendar.MONTH) + 1;
            dateformat = calendar.get(Calendar.DATE);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            sec = calendar.get(Calendar.SECOND);
            da = Integer.toString(dateformat);
            mont = Integer.toString(monthformat);
            hor = Integer.toString(hours);
            min = Integer.toString(minutes);
            secs = Integer.toString(sec);
            if (da.trim().length() == 1) {
                da = "0" + da;
            }
            if (mont.trim().equals("1")) {
                mont = "01";
            }
            if (mont.trim().equals("2")) {
                mont = "02";
            }
            if (mont.trim().equals("3")) {
                mont = "03";
            }
            if (mont.trim().equals("4")) {
                mont = "04";
            }
            if (mont.trim().equals("5")) {
                mont = "05";
            }
            if (mont.trim().equals("6")) {
                mont = "06";
            }
            if (mont.trim().equals("7")) {
                mont = "07";
            }
            if (mont.trim().equals("8")) {
                mont = "08";
            }
            if (mont.trim().equals("9")) {
                mont = "09";
            }
            if (mont.trim().equals("10")) {
                mont = "10";
            }
            if (mont.trim().equals("11")) {
                mont = "11";
            }
            if (mont.trim().equals("12")) {
                mont = "12";
            }
            if (hor.trim().length() == 1) {
                hor = "0" + hor;
            }
            if (min.trim().length() == 1) {
                min = "0" + min;
            }
            if (secs.trim().length() == 1) {
                secs = "0" + secs;
            }
            yr = Integer.toString(year);
            systemDate = (da + "-" + mont + "-" + yr);
            return systemDate;
        } catch (Exception e) {
            return "01-01-1970";
        }
    }
    public static String getInventoryType(int type){
        String inventirytype = "INV";
        if(type==0){
            inventirytype =  "INV";
        }
        if(type==1){
            inventirytype =  "IN";
        }
        if(type==2){
            inventirytype =  "OUT";
        }
        if(type==3){
            inventirytype =  "RCO";
        }
        if(type==4){
            inventirytype =  "Track";
        }
        if(type==5){
            inventirytype =  "Security";
        }

        return inventirytype;
    }

    public static String get2DigitAssetTypeId(String id){
        String assetid = id;
        if(id.length()==1){
            assetid = "0"+id;
        }
        return assetid;
    }

    public static String get8DigitAssetSerialNumber(String number){
        String serial = number;
        if(serial.length()==1){
            serial = "0000000"+number;
        }
        if(serial.length()==2){
            serial = "000000"+number;
        }
        if(serial.length()==3){
            serial = "00000"+number;
        }
        if(serial.length()==4){
            serial = "0000"+number;
        }
        if(serial.length()==5){
            serial = "000"+number;
        }
        if(serial.length()==6){
            serial = "00"+number;
        }
        if(serial.length()==7){
            serial = "0"+number;
        }
        return serial;
    }

    public static String numberToHex(String number){
        int decimal = Integer.parseInt(number);
        int rem;
        String hex="";
        char hexchars[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(decimal>0)
        {
            rem=decimal%16;
            hex=hexchars[rem]+hex;
            decimal=decimal/16;
        }
        if(hex.length()==1){
            hex = "0"+hex;
        }
        return hex;
    }

    public static String hexToNumber(String hex){
        //int num = Integer.parseInt(hex,16);
        //return String.valueOf(num);
        String digits = "0123456789ABCDEF";
        hex = hex.toUpperCase();
        long val = 0;
        for (int i = 0; i < hex.length(); i++)
        {
            char c = hex.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return String.valueOf(val);
    }

    public static boolean isHexNumber(String str) {
        boolean flag = false;
        for (int i = 0; i < str.length(); i++) {
            char cc = str.charAt(i);
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4'
                    || cc == '5' || cc == '6' || cc == '7' || cc == '8'
                    || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C'
                    || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a'
                    || cc == 'b' || cc == 'c' || cc == 'c' || cc == 'd'
                    || cc == 'e' || cc == 'f') {
                flag = true;
            }
        }
        return flag;
    }
    static Dialog errordialog, successdialog;

    public static void showCustomErrorDialog(Context context, String msg) {
        if (errordialog != null) {
            errordialog.dismiss();
        }
        if (successdialog != null) {
            successdialog.dismiss();
        }
        errordialog = new Dialog(context);
        errordialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        errordialog.setCancelable(false);
        errordialog.setContentView(R.layout.custom_alert_dialog_layout);
        TextView text = (TextView) errordialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) errordialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errordialog.dismiss();
            }
        });
       // errordialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutAnimation;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                errordialog.show();
            }
        }
    }

    static Dialog powersettingDialog;
    public static void openPowerSettingDialog(Context context, SeuicGlobalRfidHandler rfidHandler) {
        if (powersettingDialog != null) {
            powersettingDialog.dismiss();
        }

        powersettingDialog = new Dialog(context);
        powersettingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        powersettingDialog.setCancelable(false);
        powersettingDialog.setContentView(R.layout.custom_setting_dialog_layout);
        ImageView img = (ImageView) powersettingDialog.findViewById(R.id.img);
        TextView image_dialog = (TextView) powersettingDialog.findViewById(R.id.image_dialog);

        image_dialog.setText("Set Reader Power");

        TextView tip = (TextView) powersettingDialog.findViewById(R.id.tip);

        TextView textClose = (TextView) powersettingDialog.findViewById(R.id.textClose);


        textClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                powersettingDialog.dismiss();
            }
        });

        if (SharedPreferencesManager.getPower(context)==30) {
            tip.setTextColor(context.getResources().getColor(R.color.green));
            tip.setText("Current  RF Power : HIGH");
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.success));
        } else if (SharedPreferencesManager.getPower(context)==20){
            tip.setTextColor(context.getResources().getColor(R.color.boh));
            tip.setText("Current  RF Power : MEDIUM");
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.success));
        }
        else if (SharedPreferencesManager.getPower(context)==10){
            tip.setTextColor(context.getResources().getColor(R.color.red));
            tip.setText("Current  RF Power : LOW");
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.success));
        }else{
            tip.setTextColor(context.getResources().getColor(R.color.green));
            tip.setText("Current  RF Power : HIGH");
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.success));
        }

        Button btnHigh = (Button) powersettingDialog.findViewById(R.id.btnHigh);
        Button btnMedium = (Button) powersettingDialog.findViewById(R.id.btnMedium);
        Button btnLow = (Button) powersettingDialog.findViewById(R.id.btnLow);
        btnLow.setVisibility(View.VISIBLE);

        btnHigh.setText("HIGH");
        btnMedium.setText("MEDIUM");
        btnLow.setText("LOW");
        btnHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                powersettingDialog.dismiss();
                SharedPreferencesManager.setPower(context, 30);
                setAntennaPower(context,String.valueOf(30),rfidHandler);
            }
        });
        btnMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                powersettingDialog.dismiss();
                SharedPreferencesManager.setPower(context, 20);
                setAntennaPower(context,String.valueOf(20),rfidHandler);
            }
        });
        btnLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                powersettingDialog.dismiss();
                SharedPreferencesManager.setPower(context, 10);
                setAntennaPower(context,String.valueOf(10),rfidHandler);
            }
        });
        // successdialog.getWindow().getAttributes().windowAnimations = R.style.FadeInOutAnimation;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (!activity.isFinishing()) {
                powersettingDialog.show();
                // startCountDownTimer();
            }
        }
    }

    private static void setAntennaPower(Context context,String power,SeuicGlobalRfidHandler rfidHandler) {
        if (TextUtils.isEmpty(power)) {
            Toast.makeText(context, "Power cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int p = Integer.parseInt(power);
        int m = 30;
        if ((p < 0) || (p > m)) {
            Toast.makeText(context, context.getResources().getString(R.string.power_range), Toast.LENGTH_SHORT).show();
        } else {
            try {
                // mDevice.setParameters(UHFService.PARAMETER_CLEAR_EPCLIST, 1);
                boolean rv =  rfidHandler.mDevice.setPower(p);
                if (!rv) {
                    Toast.makeText(context, context.getResources().getString(R.string.set_power_fail), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(context, context.getResources().getString(R.string.set_power_ok), Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){

            }

        }
    }


}
