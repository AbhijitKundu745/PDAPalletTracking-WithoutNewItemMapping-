package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showCommonBottomSheetErrorDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAssetPalletQrmanualBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;
import com.seuic.uhf.EPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AssetPalletQRManualActivity extends AppCompatActivity implements DecodeInfoCallBack {
    private Context context = this;
    ActivityAssetPalletQrmanualBinding binding;
    DatabaseHandler db;
    ConnectionDetector cd;
    private SeuicGlobalRfidHandler rfidHandler;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerErr;
    String PALLET_TAG_ID = "";
    String QR_CODE = "";
    String CURRENT_EPC = "";
    String SCANNED_EPC = "";
    String START_DATE = "";
    String END_DATE = "";
    boolean IS_PALLET_TAG_SCANNED = false;
    boolean IS_QR_CODE_SCANNED = false;
    boolean IS_SCANNING_LOCKED = false;
    boolean IS_SCANNING_ALREADY_STARTED = false;
    private boolean allow_trigger_to_press = true;
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    private List<String> epcs = new ArrayList<>();
    Scanner scanner;
    String DC_NO = "";
    String processType = null;
    String Qty  = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(AssetPalletQRManualActivity.this, R.layout.activity_asset_pallet_qrmanual);
        getSupportActionBar().hide();
        cd = new ConnectionDetector(context);
        db = new DatabaseHandler(context);

        Intent intent = getIntent();
        DC_NO = intent.getStringExtra("DRN");
        binding.TruckNumber.setText(SharedPreferencesManager.getTruckNumber(context));
        binding.TruckNumber.setSelected(true);
        binding.LocationName.setText(SharedPreferencesManager.getLocationName(context));
        binding.LocationName.setSelected(true);
        binding.DRN.setText(DC_NO);
        binding.DRN.setSelected(true);

        processType = SharedPreferencesManager.getProcessType(context);

        setDefault();
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayerErr = MediaPlayer.create(context,R.raw.error);

        SharedPreferencesManager.setPower(context, 10);


        binding.btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Qty = binding.edtQty.getText().toString();
                Log.e("QTY", Qty);
                if(IS_PALLET_TAG_SCANNED){
                    if (IS_QR_CODE_SCANNED) {
                        if(!TextUtils.isEmpty(Qty) || !Qty.equalsIgnoreCase("0")){
                            showCustomConfirmationDialog("Are you sure you want to upload", "UPLOAD");
                        }
                        else{
                            showCommonBottomSheetErrorDialog(context, "Please enter a valid quantity");
                        }
                    }
                    else{
                        showCommonBottomSheetErrorDialog(context, "Please scan a QR code");
                    }
                }
                else{
                    showCommonBottomSheetErrorDialog(context, "Please scan a pallet tag");
                }
            }
        });

        binding.btnPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allow_trigger_to_press) {
                    AssetUtils.openPowerSettingDialog(context, rfidHandler);
                }
            }
        });
        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allow_trigger_to_press) {
                    showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "CANCEL");
                }
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allow_trigger_to_press) {
                    showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK");
                }
            }
        });

        AssetUtils.showProgress(context, getResources().getString(R.string.uhf_initialization));
        rfidHandler = new SeuicGlobalRfidHandler();
        rfidHandler.onCreate(context, new RFIDInterface() {
            @Override
            public void handleTriggerPress(boolean pressed) {
                runOnUiThread(() -> {
                    if (pressed) {
                        if (!IS_SCANNING_LOCKED) {
                            SCANNED_EPC = "";
                            if (IS_PALLET_TAG_SCANNED) {
                                //OPEN BARCODE SCANNER
                                if(IS_SCANNING_ALREADY_STARTED){
                                    IS_SCANNING_ALREADY_STARTED = false;
                                }else{
                                    IS_SCANNING_ALREADY_STARTED = true;
                                    startScanning();
                                }

                            } else {
                                //Start Inventory
                                //startScanning();
                                START_DATE = AssetUtils.getSystemDateTimeInFormatt();
                                startInventory();
                                new Handler().postDelayed(() -> {
                                    hideProgressDialog();
                                    allow_trigger_to_press = true;
                                    stopInventory();
                                    stopInventoryAndDoValidations();
                                }, 2000);
                            }
                        }
                    }
                });
            }

            @Override
            public void RFIDInitializationStatus(boolean status) {
                runOnUiThread(() -> {
                    hideProgressDialog();
                    if (status) {
                        //startInventory();
                        //init barcode scanner
                        //uploadDummyData();
                        scanner = ScannerFactory.getScanner(AssetPalletQRManualActivity.this);
                    } else {

                    }
                });
            }

            @Override
            public void handleLocateTagResponse(int value, int size) {
                runOnUiThread(() -> {

                });
            }

            @Override
            public void onDataReceived(List<EPC> rfifList) {
                runOnUiThread(() -> {
                    if (rfifList != null) {
                        if (rfifList.size() > 0) {
                            int maxRssi = Integer.MIN_VALUE;//changed
                            String maxRssiEpc = null;//changed
                            for (int i = 0; i < rfifList.size(); i++) {
                                String epc = rfifList.get(i).getId();
                                int rssivalue = rfifList.get(i).rssi;//changed
                                if (rssivalue > maxRssi) {
                                    maxRssi = rssivalue;
                                    maxRssiEpc = epc;
                                }//changed
                                try {
                                    Log.e("EPC11", epc);
                                }catch (Exception ex){

                                }
                                String assettpid = epc.substring(2, 4);

                                if ( assettpid.equalsIgnoreCase("02")) {
                                    if (rssivalue > maxRssi) {
                                        maxRssi = rssivalue;
                                        maxRssiEpc = epc;
                                    }
                                }//changed
                            }
                            if (maxRssiEpc != null) {
                                    SCANNED_EPC = maxRssiEpc;

                            }
                        }
                    }
                });
            }
        });
    }
    private void startScanning() {
        try {
            if (scanner != null) {
                scanner.open();
                scanner.setDecodeInfoCallBack(AssetPalletQRManualActivity.this);
                scanner.enable();
                scanner.startScan();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void onDecodeComplete(DecodeInfo info) {
        String qrCode = info.barcode;
        if (qrCode != null && !qrCode.isEmpty()) {
            Log.e("Barcode", info.barcode);
            validBarcodeAndAddToUi(qrCode);
        }
    }
    public void stopInventoryAndDoValidations() {
        hideProgressDialog();
        allow_trigger_to_press = true;
        // if (tagList.size() == 1) {
        hideProgressDialog();
        try {
            if (SCANNED_EPC != null) {
                if (!SCANNED_EPC.isEmpty()) {
                    if (SCANNED_EPC.length() >= 24) {
                        CURRENT_EPC = SCANNED_EPC;
                        SCANNED_EPC = "";
                        Log.e("EPC",CURRENT_EPC);
                        CURRENT_EPC = CURRENT_EPC.substring(0, 24);
                        String companycode = CURRENT_EPC.substring(0, 2);
                        String companycode1 = AssetUtils.hexToNumber(companycode);
                        Log.e("CompanyCode", companycode);
                        Log.e("CompanyCodeHex", companycode1);
                        String assettpid = CURRENT_EPC.substring(2, 4);
                        String serialnumber = CURRENT_EPC.substring(4, 12);
                        if (companycode.equalsIgnoreCase(SharedPreferencesManager.getCompanyCode(context))) {
                            Log.e("SharedCompanyCode", SharedPreferencesManager.getCompanyCode(context));
                            if (assettpid.equalsIgnoreCase("02")) {//||assettpid.equalsIgnoreCase("03")) {
                                PALLET_TAG_ID = CURRENT_EPC;
                                binding.edtRfidNumber.setText(PALLET_TAG_ID);
                                binding.edtRfidNumber.setText(db.getProductNameByProductTagId(PALLET_TAG_ID));
                                IS_PALLET_TAG_SCANNED = true;
                            } else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a pallet tag");
                            }
                        } else {
                            changeImageStatusToRfidScan();
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                        }
                    } else {
                        changeImageStatusToRfidScan();
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                    }

                } else {
                    changeImageStatusToRfidScan();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "No tag scanned.");
                }
            } else {
                changeImageStatusToRfidScan();
                AssetUtils.showCommonBottomSheetErrorDialog(context, "No tag scanned.");

            }
        } catch (Exception e) {
            Log.e("INEXCEPTION", "" + e.getMessage());
            changeImageStatusToRfidScan();
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
        }

    }

    public void setDefault() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PALLET_TAG_ID = "";
                QR_CODE = "";
                Qty = "";
                CURRENT_EPC = "";
                IS_SCANNING_LOCKED = false;
                IS_SCANNING_ALREADY_STARTED = false;
                changeImageStatusToRfidScan();
                binding.imgStatus.setImageDrawable(getDrawable(R.drawable.rfidscan));
                binding.edtRfidNumber.setText("");
                binding.edtQrCode.setText("");
                binding.batchID.setText("");
                binding.edtQty.setText("");
                allow_trigger_to_press = true;
                IS_PALLET_TAG_SCANNED = false;
                IS_QR_CODE_SCANNED = false;
                if (epcs != null) {
                    epcs.clear();
                }
                if (tagList != null) {
                    tagList.clear();
                }
            }
        });
    }

    public void changeImageStatusToRfidScan() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
                CURRENT_EPC = "";
                binding.edtRfidNumber.setText("");
                binding.imgStatus.setImageDrawable(getDrawable(R.drawable.rfidscan));
                allow_trigger_to_press = true;
            }
        });

    }

    private void startInventory() {
        if (allow_trigger_to_press) {
            if (epcs != null) {
                epcs.clear();
            }
            if (tagList != null) {
                tagList.clear();
            }
            allow_trigger_to_press = false;
            CURRENT_EPC = "";
            showProgress(context, "Please wait...Scanning Rfid Tag");
            setFilterandStartInventory();
        } else {
            hideProgressDialog();
        }
    }

    private void stopInventory() {
        rfidHandler.stopInventory();
        allow_trigger_to_press = true;
    }

    private void setFilterandStartInventory() {
        int rfpower = SharedPreferencesManager.getPower(context);
        rfidHandler.setRFPower(rfpower);

        rfidHandler.startInventory();
    }

    @Override
    public void onResume() {
        super.onResume();
        rfidHandler.onResume();
    }

    @Override
    public void onDestroy() {
        if (scanner != null) {
            try {
                ScannerKey.close();
                scanner.setDecodeInfoCallBack(null);
                scanner.close();
                scanner = null;
            } catch (Exception e) {

            }
        }

        rfidHandler.onDestroy();
        if (epcs != null) {
            epcs.clear();
        }
        if (tagList != null) {
            tagList.clear();
        }
        super.onDestroy();


    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (scanner != null) {
                scanner.stopScan();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rfidHandler.onPause();
    }
    Dialog customConfirmationDialog;

    public void showCustomConfirmationDialog(String msg, final String action) {
        if (customConfirmationDialog != null) {
            customConfirmationDialog.dismiss();
        }
        customConfirmationDialog = new Dialog(context);
        if (customConfirmationDialog != null) {
            customConfirmationDialog.dismiss();
        }
        customConfirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customConfirmationDialog.setCancelable(false);
        customConfirmationDialog.setContentView(R.layout.custom_alert_dialog_layout2);
        TextView text = (TextView) customConfirmationDialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) customConfirmationDialog.findViewById(R.id.btn_dialog);
        Button dialogButtonCancel = (Button) customConfirmationDialog.findViewById(R.id.btn_dialog_cancel);
        dialogButton.setText("YES");
        dialogButtonCancel.setText("NO");
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfirmationDialog.dismiss();
                if (action.equals("UPLOAD")) {
                    allow_trigger_to_press = false;
                    uploadInventoryToServer();
                } else if (action.equals("CANCEL")) {
                    setDefault();
                } else if (action.equals("BACK")) {
                    setDefault();
                    finish();
                }
            }
        });
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfirmationDialog.dismiss();
            }
        });
        customConfirmationDialog.show();
    }

    private void uploadInventoryToServer() {

        if (!binding.edtQrCode.equals("")) {
            new CollectInventoryData().execute("ABC");
        } else {
            allow_trigger_to_press = true;
            AssetUtils.showCommonBottomSheetErrorDialog(context, "No data to upload");
        }

    }



    public class CollectInventoryData extends AsyncTask<String, String, JSONObject> {
        protected void onPreExecute() {
            showProgress(context, "Collectiong Data To Upload");
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... params) {
            if (!binding.edtQrCode.equals("")) {
                try {
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
                    jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, "AssetPalletMapping");
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, 1);
                    jsonobject.put(APIConstants.K_PARENT_TAG_ID, PALLET_TAG_ID);
                    jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "Pallet");
                    jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
                    jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));
                    jsonobject.put(APIConstants.K_DRN, DC_NO);
                    //jsonobject.put(APIConstants.K_PALLET_ID, CURRENT_EPC);
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < 1; i++) {
                        JSONObject barcodeObject = new JSONObject();
                        String epc = QR_CODE;
                        String qty = Qty;
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, epc);
                        barcodeObject.put("Qty", qty);

                        //barcodeObject.put(APIConstants.K_ACTIVITY_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_TRANSACTION_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());

                        js.put(barcodeObject);
                    }
                    jsonobject.put(APIConstants.K_DATA, js);

                    return jsonobject;

                } catch (JSONException e) {

                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            if (result != null) {
                if (cd.isConnectingToInternet()) {
                    try {
                        allow_trigger_to_press = false;
                        hideProgressDialog();
                        uploadInventory(result, APIConstants.M_UPLOAD_ITEM_QR_MANUAL, "Please wait...\n" + " Mapping is in progress");

                    } catch (OutOfMemoryError e) {
                        hideProgressDialog();
                        allow_trigger_to_press = false;
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Huge Data cannot be uploaded");
                    }

                } else {
                    hideProgressDialog();
                    allow_trigger_to_press = true;
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            } else {
                hideProgressDialog();
                allow_trigger_to_press = true;
                ;
                AssetUtils.showCommonBottomSheetErrorDialog(context, "Something went wrong");
            }

        }

    }
    public void uploadInventory(final JSONObject loginRequestObject, String METHOD_NAME, String progress_message) {
        showProgress(context, progress_message);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Log.e("ASSETPALLETMAPURL", SharedPreferencesManager.getHostUrl(context) + METHOD_NAME);
        Log.e("ASSETPALLETMAPRES", loginRequestObject.toString());
        AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME).addJSONObjectBody(loginRequestObject)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        hideProgressDialog();
                        allow_trigger_to_press = true;
                        if (result != null) {
                            try {
                                Log.e("ASSETPALLETMAPRES", result.toString());
                                String status = result.getString(APIConstants.K_STATUS);
                                String message = result.getString(APIConstants.K_MESSAGE);

                                if (status.equalsIgnoreCase("true")) {
                                    allow_trigger_to_press = false;
                                    //TODO do validations
                                    // JSONArray data = result.getJSONArray(APIConstants.K_DATA);
                                    //  checkResponseAndDovalidations(data);

                                    //TODO
                                    setDefault();
                                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Mapping Done Successfully");

                                } else {
                                    allow_trigger_to_press = true;
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                }
                            } catch (JSONException e) {
                                hideProgressDialog();
                                allow_trigger_to_press = true;
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
                            }
                        } else {
                            hideProgressDialog();
                            allow_trigger_to_press = true;
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        allow_trigger_to_press = true;
                        if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                        }
                    }
                });
    }
    private void validBarcodeAndAddToUi(String Qrcode){
        hideProgressDialog();
        allow_trigger_to_press = true;
        String[] parts;
        parts = Qrcode.split("[,\\s]+");
        if (parts.length >3 && parts.length<6) {
            binding.edtQrCode.setText(Qrcode);
            QR_CODE = Qrcode;
            IS_QR_CODE_SCANNED = true;
            if(Qrcode.contains(",")){
                parts = Qrcode.split("[,]+");
                String batchID = parts[3].trim().replaceAll("^0*", "");
                Log.e("BatchID", batchID);
                binding.batchID.setText(batchID);
            }
            else if(Qrcode.contains(" ")){
                parts = Qrcode.split("[\\s]+");
                String batchID = parts[4].trim();
                Log.e("BatchID", batchID);
                binding.batchID.setText(batchID);
            }

            try {
                if (scanner != null) {
                    scanner.stopScan();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            END_DATE = AssetUtils.getCurrentSystemDate();
        }
        else {
            // Barcode format does not match, handle accordingly
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Barcode format does not match the expected format");
        }
    }

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Do you want to go back?", "BACK");
        //super.onBackPressed();
    }
}