package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.TYPE_PALLET;
import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AssetPalletMapAdapter;
import com.psl.pallettracking.adapters.ItemMovementAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAssetPalletMappingBinding;
import com.psl.pallettracking.databinding.ActivityItemMovementBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.helper.StringUtils;
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

public class ItemMovementActivity extends AppCompatActivity implements DecodeInfoCallBack{
    private Context context = this;
    private SeuicGlobalRfidHandler rfidHandler;
    private ActivityItemMovementBinding binding;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerErr;
    private ConnectionDetector cd;
    private DatabaseHandler db;
    String SOURCE_PALLET_TAG_ID = "";
    String DEST_PALLET_TAG_ID = "";
    String SOURCE_BIN_TAG_ID = "";
    String DEST_BIN_TAG_ID = "";
    String CURRENT_EPC = "";
    String START_DATE = "";
    String END_DATE = "";
    boolean IS_SOURCE_PALLET_TAG_SCANNED = false;
    boolean IS_DEST_PALLET_TAG_SCANNED = false;
    boolean IS_SOURCE_BIN_TAG_SCANNED = false;
    boolean IS_DEST_BIN_TAG_SCANNED = false;
    boolean IS_SCANNING_LOCKED = false;
    boolean IS_SCANNING_ALREADY_STARTED = false;
    private boolean allow_trigger_to_press = true;
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    public ArrayList<HashMap<String, String>> barcodeList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> hashMap = new HashMap<>();
    HashMap<String, String> barcodeHashMap = new HashMap<>();
    private List<String> epcs = new ArrayList<>();
    private List<String> barcodes = new ArrayList<>();
    Scanner scanner;
    ItemMovementAdapter adapter;

    @Override
    public void onBackPressed() {
        if (allow_trigger_to_press) {
            showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK");
        } else {
            stopInventory();
            showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_item_movement);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_item_movement);
        getSupportActionBar().hide();
        cd = new ConnectionDetector(context);
        db = new DatabaseHandler(context);
        SharedPreferencesManager.setPower(context,10);
        adapter = new ItemMovementAdapter(context, barcodeList);
        binding.LVqrCodes.setAdapter(adapter);

        setDefault();
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayerErr = MediaPlayer.create(context,R.raw.error);

        SharedPreferencesManager.setPower(context, 20);

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IS_SOURCE_PALLET_TAG_SCANNED){
                    if (IS_SOURCE_BIN_TAG_SCANNED){
                        if(IS_DEST_PALLET_TAG_SCANNED){
                            if (IS_DEST_BIN_TAG_SCANNED){
                                if (barcodeList.size() > 0) {
                                    showCustomConfirmationDialog("Are you sure you want to upload", "UPLOAD");
                               } else{
                                    AssetUtils.showCommonBottomSheetErrorDialog(context,"Please Scan the barcode");
                               }
                            } else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context,"Please Scan Destination Bin Tag");
                            }
                        } else{
                            AssetUtils.showCommonBottomSheetErrorDialog(context,"Please Scan Destination Pallet Tag");
                        }
                    }  else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context,"Please Scan Source Bin Tag");
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context,"Please Scan Source Pallet Tag");
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
                            if (IS_SOURCE_PALLET_TAG_SCANNED && IS_DEST_PALLET_TAG_SCANNED && IS_SOURCE_BIN_TAG_SCANNED && IS_DEST_BIN_TAG_SCANNED) {
                                //OPEN BARCODE SCANNER
                                if (IS_SCANNING_ALREADY_STARTED) {
                                    IS_SCANNING_ALREADY_STARTED = false;
                                } else {
                                    IS_SCANNING_ALREADY_STARTED = true;
                                    startScanning();
                                }

                            } else {
                                //Start Inventory
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
                        scanner = ScannerFactory.getScanner(ItemMovementActivity.this);
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
                                String assettpid = epc.substring(2, 4);

                                if (assettpid.equalsIgnoreCase("02")) {
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
                scanner.setDecodeInfoCallBack(ItemMovementActivity.this);
                scanner.enable();
                scanner.startScan();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int CURRENT_INDEX = -1;
    public void onListItemClicked(HashMap<String, String> hashmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (allow_trigger_to_press) {
                    int index = checkIsBarcodeExist(hashmap.get("BARCODE"));
                    if (index == -1) {

                    } else {
                        CURRENT_INDEX = index;
                        showCustomConfirmationDialog("Are you sure you want to delete", "DELETE");

                    }
                    Toast.makeText(context, hashmap.get("MESSAGE"), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private String SCANNED_EPC = "";
    public void stopInventoryAndDoValidations() {
        hideProgressDialog();
        adapter.notifyDataSetChanged();
        allow_trigger_to_press = true;
        hideProgressDialog();
        try {
            if (SCANNED_EPC != null && !SCANNED_EPC.isEmpty() && SCANNED_EPC.length() >= 24) {
                CURRENT_EPC = SCANNED_EPC;
                Log.e("EPC", CURRENT_EPC);
                CURRENT_EPC = CURRENT_EPC.substring(0, 24);
                String companycode = CURRENT_EPC.substring(0, 2);
                String assettpid = CURRENT_EPC.substring(2, 4);
                if (companycode.equalsIgnoreCase(SharedPreferencesManager.getCompanyCode(context))) {
                    Log.e("SharedPrefCompanyCode", SharedPreferencesManager.getCompanyCode(context));
                    if (assettpid.equalsIgnoreCase("02")) {
                            if (!IS_SOURCE_PALLET_TAG_SCANNED) {
                                // First pallet tag scan
                                SOURCE_PALLET_TAG_ID = CURRENT_EPC;
                                String SourcePalletName = db.getProductNameByProductTagId(SOURCE_PALLET_TAG_ID);
                                binding.edtSourcePalletNumber.setText(SourcePalletName);
                                IS_SOURCE_PALLET_TAG_SCANNED = true;
                                Log.e("SourcePallet", SOURCE_PALLET_TAG_ID);
                            } else if (!IS_DEST_PALLET_TAG_SCANNED) {
                                // Second pallet tag scan
                                DEST_PALLET_TAG_ID = CURRENT_EPC;
                                String DestPalletName = db.getProductNameByProductTagId(DEST_PALLET_TAG_ID);
                                binding.edtDestPalletNumber.setText(DestPalletName);
                                IS_DEST_PALLET_TAG_SCANNED = true;
                                Log.e("DestPallet", DEST_PALLET_TAG_ID);
                            }
                    } else if (assettpid.equalsIgnoreCase("03")) {
                        if (!IS_SOURCE_BIN_TAG_SCANNED) {
                            SOURCE_BIN_TAG_ID = CURRENT_EPC;
                            String SourceBinName = db.getProductNameByProductTagId(SOURCE_BIN_TAG_ID);
                            binding.edtSourceBinNumber.setText(SourceBinName);
                            IS_SOURCE_BIN_TAG_SCANNED = true;
                        } else if(!IS_DEST_BIN_TAG_SCANNED){
                            DEST_BIN_TAG_ID = CURRENT_EPC;
                            String DestBinName = db.getProductNameByProductTagId(DEST_BIN_TAG_ID);
                            binding.edtDestBinNumber.setText(DestBinName);
                            IS_DEST_BIN_TAG_SCANNED = true;
                        }
                    }
                    else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                    }
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                }
            } else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
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
                SOURCE_PALLET_TAG_ID = "";
                DEST_PALLET_TAG_ID = "";
                CURRENT_EPC = "";
                IS_SCANNING_LOCKED = false;
                IS_SCANNING_ALREADY_STARTED = false;
                changeImageStatusToRfidScan();
                binding.edtSourcePalletNumber.setText("");
                binding.edtDestPalletNumber.setText("");
                binding.edtSourceBinNumber.setText("");
                binding.edtDestBinNumber.setText("");
                allow_trigger_to_press = true;
                IS_SOURCE_PALLET_TAG_SCANNED = false;
                IS_DEST_PALLET_TAG_SCANNED = false;
                IS_SOURCE_BIN_TAG_SCANNED = false;
                IS_DEST_BIN_TAG_SCANNED = false;
                binding.textCount.setVisibility(View.GONE);
                if (epcs != null) {
                    epcs.clear();
                }
                if (tagList != null) {
                    tagList.clear();
                }
                if (barcodeList != null) {
                    barcodeList.clear();
                }
                if (barcodes != null) {
                    barcodes.clear();
                }
                binding.textCount.setText("Count : " + barcodeList.size());
            }
        });
    }

    public void changeImageStatusToRfidScan() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
                CURRENT_EPC = "";
                //PALLET_TAG_ID = "";
                binding.edtSourcePalletNumber.setText("");
                binding.edtDestPalletNumber.setText("");
                allow_trigger_to_press = true;
                adapter.notifyDataSetChanged();
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
            adapter.notifyDataSetChanged();
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
        adapter.notifyDataSetChanged();
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
        if (barcodeList != null) {
            barcodeList.clear();
        }
        if (barcodes != null) {
            barcodes.clear();
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


    public int checkIsBarcodeExist(String barcode) {

        for (int i = 0; i < barcodeList.size(); i++) {
            String existingBarcode = barcodeList.get(i).get("BARCODE");
            if (existingBarcode != null && existingBarcode.equals(barcode)) {
                return i;
            }

            if (StringUtils.isEmpty(barcode)) {
                return -1;
            }
        }
        return binarySearch(barcodes, barcode);
    }

    static int binarySearch(List<String> array, String src) {
        int left = 0;
        int right = array.size() - 1;
        while (left <= right) {
            if (compareString(array.get(left), src)) {
                return left;
            } else if (left != right) {
                if (compareString(array.get(right), src))
                    return right;
            }
            left++;
            right--;
        }
        return -1;
    }

    static boolean compareString(String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        } else if (str1.hashCode() != str2.hashCode()) {
            return false;
        } else {
            char[] value1 = str1.toCharArray();
            char[] value2 = str2.toCharArray();
            int size = value1.length;
            for (int k = 0; k < size; k++) {
                if (value1[k] != value2[k]) {
                    return false;
                }
            }
            return true;
        }
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

                } else if (action.equals("DELETE")) {
                    barcodeList.remove(CURRENT_INDEX);
                    barcodes.remove(CURRENT_INDEX);
                    CURRENT_INDEX = -1;
                    binding.textCount.setText("Count : " + barcodeList.size());
                    adapter.notifyDataSetChanged();
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

        if (barcodeList.size() > 0) {
            new ItemMovementActivity.CollectInventoryData().execute("ABC");
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
            if (barcodeList.size() > 0) {
                try {
                    String activity_type = "ITEM_MOVEMENT";
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
                    jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, activity_type);
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, barcodeList.size());
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_SOURCE_PALLET_TAG_ID, SOURCE_PALLET_TAG_ID);
                    jsonobject.put(APIConstants.K_DESTINATION_PALLET_TAG_ID, DEST_PALLET_TAG_ID);
                    jsonobject.put(APIConstants.K_SOURCE_BIN_TAG_ID, SOURCE_BIN_TAG_ID);
                    jsonobject.put(APIConstants.K_DESTINATION_BIN_TAG_ID, DEST_BIN_TAG_ID);
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < barcodeList.size(); i++) {
                        JSONObject qrcodeObject = new JSONObject();
                        String qrcode = barcodeList.get(i).get("BARCODE");
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        qrcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, qrcode);

                        //barcodeObject.put(APIConstants.K_ACTIVITY_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        qrcodeObject.put(APIConstants.K_TRANSACTION_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());

                        js.put(qrcodeObject);
                    }
                    jsonobject.put(APIConstants.K_DATA, js);
                    Log.e("JSON", jsonobject.toString());

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
                        // uploadInventory(result, APIConstants.M_UPLOAD_ASSET_PALLET_MAPPING, "Please wait...\n" + " Mapping is in progress");
                        uploadInventory(result, APIConstants.M_UPLOAD_ITEM_MOVEMENT, "Please wait...\n" + " Mapping is in progress");

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
    private void addBarcodeToList(String barcode) {
        hideProgressDialog();
        allow_trigger_to_press = true;
        if (barcode != null) {
            String[] parts = barcode.split("[,\\s]+");
            if (parts.length > 3 && parts.length < 6) {
                barcodeHashMap = new HashMap<>();
                barcodeHashMap.put("EPC", CURRENT_EPC);
                barcodeHashMap.put("BARCODE", barcode);
                barcodeHashMap.put("ASSETNAME", barcode);
                barcodeHashMap.put("COUNT", "1");
                barcodeHashMap.put("STATUS", "true");
                barcodeHashMap.put("MESSAGE", "");
                int index = checkIsBarcodeExist(barcode);
                if (index == -1) {

                    barcodeList.add(barcodeHashMap);

                    if (!barcodes.contains(barcode)) {
                        barcodes.add(barcode);
                        mediaPlayer.start();
                    }
                } else {
                    int tagCount = Integer.parseInt(barcodeList.get(index).get("COUNT"), 10) + 1;
                    barcodeHashMap.put("COUNT", String.valueOf(tagCount));
                    barcodeList.set(index, barcodeHashMap);
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.barcode_already_scanned));
                    mediaPlayerErr.start();

                }
                binding.textCount.setText("Count : " + barcodeList.size());
                adapter.notifyDataSetChanged();
                END_DATE = AssetUtils.getSystemDateTimeInFormatt();

                try {
                    if (scanner != null) {
                        scanner.stopScan();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    hideProgressDialog();
                    if (IS_SCANNING_ALREADY_STARTED) {
                        IS_SCANNING_ALREADY_STARTED = false;
                        // startScanning();
                    }
                }, 500);
            } else {
                // Barcode format does not match, handle accordingly
                AssetUtils.showCommonBottomSheetErrorDialog(context, "Barcode format does not match the expected format");
            }
        }
    }

    @Override
    public void onDecodeComplete(DecodeInfo info) {
        String barcode = info.barcode;
        if (barcode != null && !barcode.isEmpty()) {
            Log.e("Barcode", info.barcode);
            addBarcodeToList(barcode);
        }
    }
}