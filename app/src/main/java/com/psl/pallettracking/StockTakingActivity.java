package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.skuitemListAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityStockTakingBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.psl.pallettracking.viewHolder.ItemDetailsList;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;
import com.seuic.uhf.EPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class StockTakingActivity extends AppCompatActivity implements DecodeInfoCallBack {
    private Context context = this;
    ActivityStockTakingBinding binding;
    private SeuicGlobalRfidHandler rfidHandler;
    Scanner scanner;
    DatabaseHandler db;
    ConnectionDetector cd;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerErr;
    private boolean allow_trigger_to_press = true;
    private boolean IS_SCANNING_LOCKED = false;
    boolean IS_BIN_TAG_SCANNED = false;
    boolean IS_PALLET_TAG_SCANNED = false;
    boolean IS_EMPTY_BIN = false;
    boolean IS_EMPTY_PALLET = false;
    String Qty  = "";
    private String CURRENT_EPC = "";
    private String SCANNED_EPC = "";
    private String BIN_NAME = "";
    private String PALLET_NAME = "";
    private String BATCH_ID = "";
    private String SKU_CODE = "";
    private String SKU_NAME = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stock_taking);
        getSupportActionBar().hide();
        db = new DatabaseHandler(context);
        cd = new ConnectionDetector(context);
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayerErr = MediaPlayer.create(context, R.raw.error);
        binding.emptyPalletbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!IS_BIN_TAG_SCANNED) {
                    // Show error message when bin is not scanned
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // Ensure error shows only once on release
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Scan a bin");
                    }
                    return true; // Consume the touch event, preventing toggle
                }
                return false; // Allow the default behavior when IS_BIN_TAG_SCANNED is true
            }
        });
        binding.emptyPalletbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IS_BIN_TAG_SCANNED) {
                    if (!IS_EMPTY_PALLET) {
                        binding.emptyPalletbtn.setChecked(true);
                        IS_EMPTY_PALLET = true;
                        IS_PALLET_TAG_SCANNED = true;
                        binding.edtPalletName.setText("EMPTY");
                    } else {
                        binding.emptyPalletbtn.setChecked(false);
                        IS_EMPTY_PALLET = false;
                        IS_PALLET_TAG_SCANNED = false;
                        binding.edtPalletName.setText("");
                    }
                }
            }
        });
        binding.emptybinBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!IS_BIN_TAG_SCANNED) {
                    // Show error message when bin is not scanned
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) { // Ensure error shows only once on release
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Scan a bin first");
                    }
                    return true; // Consume the touch event, preventing toggle
                }
                return false; // Allow the default behavior when IS_BIN_TAG_SCANNED is true
            }
        });

        binding.emptybinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_BIN_TAG_SCANNED) {
                    if (!IS_EMPTY_BIN) {
                        binding.emptybinBtn.setChecked(true);
                        IS_EMPTY_BIN = true;
                        allow_trigger_to_press = false;

                        // Hide other UI elements
                        binding.llBinDetails.setVisibility(View.INVISIBLE);
                        clearData();
                    } else {
                        binding.emptybinBtn.setChecked(false);
                        IS_EMPTY_BIN = false;
                        allow_trigger_to_press = true;

                        // Show other UI elements
                        binding.llBinDetails.setVisibility(View.VISIBLE);
                        clearData();
                    }
                }
            }
        });
        binding.edtskuCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SKU_CODE = binding.edtskuCode.getText().toString();
                if (!SKU_CODE.equalsIgnoreCase("")) {
                    if (db.isSKUExist(SKU_CODE)) {
                        binding.edtskuName.setText(db.getItemNameByItemCode(SKU_CODE));
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, SKU_CODE + "doesn't exist. Please enter correct SKU code");
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SKU_CODE = binding.edtskuCode.getText().toString();
                if(!SKU_CODE.equalsIgnoreCase("")) {
                    if (db.isSKUExist(SKU_CODE)) {
                        binding.edtskuName.setText(db.getItemNameByItemCode(SKU_CODE));
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, SKU_CODE + "doesn't exist. Please enter correct SKU code");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SKU_CODE = binding.edtskuCode.getText().toString();
                if (!SKU_CODE.equalsIgnoreCase("")) {
                    if (db.isSKUExist(SKU_CODE)) {
                        binding.edtskuName.setText(db.getItemNameByItemCode(SKU_CODE));
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, SKU_CODE + "doesn't exist. Please enter correct SKU code");
                    }
                }
            }
        });
        binding.batchID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BATCH_ID = binding.batchID.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BATCH_ID = binding.batchID.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                BATCH_ID = binding.batchID.getText().toString();
            }
        });
        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IS_BIN_TAG_SCANNED){
                    if(IS_EMPTY_BIN){
                        showCustomConfirmationDialog("Do you want to save the data?", "UPLOAD");
                    } else if(IS_PALLET_TAG_SCANNED){
                        if(!binding.edtskuCode.getText().equals("")){
                            if(!binding.batchID.getText().equals("")){
                                if(!binding.edtQty.getText().equals("") && !binding.edtQty.getText().equals("0")){
                                    Qty = binding.edtQty.getText().toString();
                                    showCustomConfirmationDialog("Do you want to save the data?", "UPLOAD");
                                } else{
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid quantity");
                                }
                            } else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter batch ID");
                            }
                        }
                        else{
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan an item or enter item code");
                        }
                    }
                    else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a pallet");
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a bin");
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
                        if(allow_trigger_to_press){
                            if (IS_BIN_TAG_SCANNED && IS_PALLET_TAG_SCANNED) {
                                    if (!IS_SCANNING_LOCKED) {
                                        //OPEN BARCODE SCANNER
                                        startScanning();
                                    }
                            } else{
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
                        scanner = ScannerFactory.getScanner(context);
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
                                }
                            }
                            if (maxRssiEpc != null) {
                                SCANNED_EPC = maxRssiEpc;
                                Log.e("CURRENT_EPC", SCANNED_EPC);
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
                scanner.setDecodeInfoCallBack(this);
                scanner.enable();
                scanner.startScan();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
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
    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Do you want to back?", "BACK");
        //super.onBackPressed();
    }
    public void setDefault() {

        CURRENT_EPC = "";
        SCANNED_EPC = "";
        IS_SCANNING_LOCKED = false;
        allow_trigger_to_press = true;
        binding.edtBinName.setText("");
        binding.edtPalletName.setText("");
        binding.edtskuCode.setText("");
        binding.edtskuName.setText("");
        binding.batchID.setText("");
        binding.edtQty.setText("");
        Qty  = "";
        binding.batchID.setEnabled(true);
        binding.edtskuCode.setEnabled(true);
        binding.llBinDetails.setVisibility(View.VISIBLE);
        IS_BIN_TAG_SCANNED = false;
        IS_PALLET_TAG_SCANNED = false;
        IS_EMPTY_BIN = false;
        IS_EMPTY_PALLET = false;
        SKU_CODE = "";
        SKU_NAME = "";
        BATCH_ID = "";
        BIN_NAME = "";
        PALLET_NAME = "";
        binding.edtskuCode.setEnabled(true);
        binding.batchID.setEnabled(true);
    }
    public void clearData() {
        PALLET_NAME = "";
        SKU_CODE = "";
        SKU_NAME = "";
        BATCH_ID = "";
        Qty = "";
        binding.edtPalletName.setText("");
        binding.edtskuCode.setText("");
        binding.edtskuName.setText("");
        binding.batchID.setText("");
        binding.edtQty.setText("");
        IS_PALLET_TAG_SCANNED = false;
        IS_EMPTY_PALLET = false;
        binding.edtskuCode.setEnabled(true);
        binding.batchID.setEnabled(true);
    }
    private void startInventory() {
        if (allow_trigger_to_press) {
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
    public void stopInventoryAndDoValidations() {
        allow_trigger_to_press = true;
        hideProgressDialog();
        try {
            if (SCANNED_EPC != null && !SCANNED_EPC.isEmpty() && SCANNED_EPC.length() >= 24) {
                CURRENT_EPC = SCANNED_EPC;
                Log.e("EPC", CURRENT_EPC);
                CURRENT_EPC = CURRENT_EPC.substring(0, 24);
                String companycode = CURRENT_EPC.substring(0, 2);
                Log.e("companyCode", companycode);
                String assettpid = CURRENT_EPC.substring(2, 4);
                if (companycode.equalsIgnoreCase(SharedPreferencesManager.getCompanyCode(context))) {
                   if(!IS_BIN_TAG_SCANNED){
                       if(assettpid.equalsIgnoreCase("03")){
                           String binName = db.getProductNameByProductTagId(CURRENT_EPC);
                           if(!binName.equalsIgnoreCase(AppConstants.UNKNOWN_ASSET)){
                               binding.edtBinName.setText(binName);
                               IS_BIN_TAG_SCANNED = true;
                               BIN_NAME = binName;
                           }
                       }
                   } else{
                           if(assettpid.equalsIgnoreCase("02")) {
                               String PalletName = db.getProductNameByProductTagId(CURRENT_EPC);
                               if(!PalletName.equalsIgnoreCase(AppConstants.UNKNOWN_ASSET)){
                                   binding.edtPalletName.setText(PalletName);
                                   IS_PALLET_TAG_SCANNED = true;
                                   PALLET_NAME = PalletName;
                               }else{
                                   AssetUtils.showCommonBottomSheetErrorDialog(context, "Please check the pallet Tag ID");
                               }
                           }

                   }

                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                }
            } else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
            }
        } catch (Exception e) {
            Log.e("INEXCEPTION", "" + e.getMessage());
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
        }
    }
    private void addBarcodeToList(String Qrcode){
        hideProgressDialog();
        allow_trigger_to_press = true;
        String[] parts;
        parts = Qrcode.split("[,\\s]+");
        String batchID = "";
        String skuCode = "";
        if (parts.length >3 && parts.length<6) {

            if(Qrcode.contains(",")){
                parts = Qrcode.split("[,]+");
                batchID = parts[3].trim().replaceAll("^0*", "");
                skuCode = parts[1].trim().replaceAll("^0*", "");

            }
            else if(Qrcode.contains(" ")){
                parts = Qrcode.split("[\\s]+");
                batchID = parts[4].trim();
                skuCode = parts[2].trim();
                Log.e("BatchID", batchID);
            }
            if(db.isSKUExist(skuCode)){
                mediaPlayer.start();
                binding.edtskuCode.setText(skuCode);
                binding.batchID.setText(batchID);
                binding.edtskuName.setText(db.getItemNameByItemCode(skuCode));
                SKU_CODE = skuCode;
                SKU_NAME = db.getItemNameByItemCode(skuCode);
                BATCH_ID = batchID;
                binding.edtskuCode.setEnabled(false);
                binding.batchID.setEnabled(false);
            }
            else{
                mediaPlayerErr.start();
                AssetUtils.showCommonBottomSheetErrorDialog(context, skuCode+" doesn't exist. Please contact admin.");
            }

            try {
                if (scanner != null) {
                    scanner.stopScan();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {
            // Barcode format does not match, handle accordingly
            mediaPlayerErr.start();
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Barcode format does not match the expected format");
        }
    }
    private void uploadInventoryToServer() {
            new CollectInventoryData().execute("ABC");
    }
    public class CollectInventoryData extends AsyncTask<String, String, JSONObject> {
        protected void onPreExecute() {
            showProgress(context, "Collectiong Data To Upload");
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... params) {
                try {
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, "StockRecording");
                    jsonobject.put("BinName", BIN_NAME);
                    jsonobject.put("PalletName", PALLET_NAME);
                    jsonobject.put("SKUCode", SKU_CODE);
                    jsonobject.put("SKUName", SKU_NAME);
                    jsonobject.put("BatchID", BATCH_ID);
                    jsonobject.put("Qty", Qty);
                    jsonobject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
                    return jsonobject;

                } catch (JSONException e) {

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
                        Log.e("UploadReq", result.toString());
                        //uploadInventory(result, APIConstants.M_UPLOAD_ITEM_QR_MANUAL, "Please wait...\n" + " Mapping is in progress");

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
}