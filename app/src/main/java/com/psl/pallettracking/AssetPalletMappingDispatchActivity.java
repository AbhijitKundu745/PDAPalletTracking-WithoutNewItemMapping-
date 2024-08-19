package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.BinPartialPalletMappingAdapterModel;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessModel;
import com.psl.pallettracking.adapters.skuitemListAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAssetPalletMappingDispatchBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.helper.StringUtils;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

public class AssetPalletMappingDispatchActivity extends AppCompatActivity implements DecodeInfoCallBack {
    private Context context = this;
    ActivityAssetPalletMappingDispatchBinding binding;
    private SeuicGlobalRfidHandler rfidHandler;
    Scanner scanner;
    DatabaseHandler db;
    ConnectionDetector cd;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerErr;
    private String DC_NO = "";
    private int CURRENT_INDEX = -1;
    private boolean allow_trigger_to_press = true;
    private boolean IS_SCANNING_LOCKED = false;
    public ArrayList<HashMap<String, String>> barcodeList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> barcodeHashMap = new HashMap<>();
    private List<String> barcodes = new ArrayList<>();
    private String START_DATE = "";
    private String END_DATE = "";
    private String CURRENT_EPC = "";
    private String DC_TAG_ID = "";
    List<ItemDetailsList> itemDetailsLists, originalItemList;

    skuitemListAdapter adapter, confirmationAdapter;
    private String SKUCode = "";
    private String Password = "PASS007";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_asset_pallet_mapping_dispatch);
        getSupportActionBar().hide();
        binding = DataBindingUtil.setContentView(AssetPalletMappingDispatchActivity.this, R.layout.activity_asset_pallet_mapping_dispatch);
        db = new DatabaseHandler(context);
        cd = new ConnectionDetector(context);

        Intent intent = getIntent();
        DC_NO = intent.getStringExtra("DRN");
        DC_TAG_ID = intent.getStringExtra("DC_TAG_ID");
        binding.TruckNumber.setText(SharedPreferencesManager.getTruckNumber(context));
        binding.TruckNumber.setSelected(true);
        binding.LocationName.setText(SharedPreferencesManager.getLocationName(context));
        binding.LocationName.setSelected(true);
        binding.DRN.setText(DC_NO);
        binding.DRN.setSelected(true);

        getSKUDetails(DC_NO);
        mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayerErr = MediaPlayer.create(context, R.raw.error);
        START_DATE = AssetUtils.getSystemDateTimeInFormatt();
        binding.rvPallet.setLayoutManager(new GridLayoutManager(context, 1));
        if (itemDetailsLists != null) {
            itemDetailsLists.clear();
        }
        itemDetailsLists = new ArrayList<>();
        originalItemList = new ArrayList<>();
        adapter = new skuitemListAdapter(context, itemDetailsLists);
        adapter.notifyDataSetChanged();
        binding.rvPallet.setAdapter(adapter);
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeList.size() > 0) {
                    if (areAllItemsScanned()) {
                        showCustomConfirmationDialog("Are you sure you want to upload", "UPLOAD");
                    } else {
                        showCustomConfirmationDialogForSpecial("Are you sure want to save the data without scanning all the items?", "UPLOAD");
                    }

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
                            //OPEN BARCODE SCANNER
                            startScanning();
                        }
                    } else {
                        new Handler().postDelayed(() -> {
                            hideProgressDialog();
                            allow_trigger_to_press = true;
                        }, 2000);
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
                        scanner = ScannerFactory.getScanner(AssetPalletMappingDispatchActivity.this);
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

                });
            }
        });
    }

    private void startScanning() {
        try {
            if (scanner != null) {
                scanner.open();
                scanner.setDecodeInfoCallBack(AssetPalletMappingDispatchActivity.this);
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

    public int checkIsBarcodeExist(String barcode) {
//        if (StringUtils.isEmpty(barcode)) {
//            return -1;
//        }
//        return binarySearch(barcodes, barcode);
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

    /**
     * 二分查找，找到该值在数组中的下标，否则为-1
     */
    static int binarySearch(List<String> array, String src) {
        int left = 0;
        int right = array.size() - 1;
        // 这里必须是 <=
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

    Dialog customConfirmationDialog, customConfirmationDialogSpec;

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
                    binding.recCount.setText("Count : " + barcodeList.size());
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
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }

    private void uploadInventoryToServer() {

        if (barcodeList.size() > 0) {

            new AssetPalletMappingDispatchActivity.CollectInventoryData().execute("ABC");
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
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
                    jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    //jsonobject.put(APIConstants.K_ACTIVITY_ID, "AssetPallet" + SharedPreferencesManager.getDeviceId(context) + AssetUtils.getSystemDateTimeInFormatt());
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, "AssetMappingDispatch");
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, barcodeList.size());
                    jsonobject.put(APIConstants.K_PARENT_TAG_ID, DC_TAG_ID);
                    jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "Pallet");
                    jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
                    jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));
                    jsonobject.put(APIConstants.K_DRN, DC_NO);
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < barcodeList.size(); i++) {
                        JSONObject barcodeObject = new JSONObject();
                        String epc = barcodeList.get(i).get("BARCODE");
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, epc);

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
                        // uploadInventory(result, APIConstants.M_UPLOAD_ASSET_PALLET_MAPPING, "Please wait...\n" + " Mapping is in progress");
                        uploadInventory(result, APIConstants.M_UPLOAD_INVENTORY, "Please wait...\n" + " Mapping is in progress");

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
                                    finish();
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
                        //Log.e("ERROR", anError.getErrorDetail());
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
        binding.TruckNumber.setText("");
        binding.LocationName.setText("");
        binding.DRN.setText("");
        itemDetailsLists.clear();
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

    private void addBarcodeToList(String barcode) {
        hideProgressDialog();
        allow_trigger_to_press = true;
        String skuCode = "";
        String[] parts = barcode.split("[,\\s]+");
        if (parts.length > 3 && parts.length < 6) {
            barcodeHashMap = new HashMap<>();
            barcodeHashMap.put("EPC", CURRENT_EPC);
            barcodeHashMap.put("BARCODE", barcode);
            barcodeHashMap.put("ASSETNAME", barcode);
            barcodeHashMap.put("COUNT", "1");
            barcodeHashMap.put("STATUS", "true");
            barcodeHashMap.put("MESSAGE", "");
            if (barcode.contains(",")) {
                parts = barcode.split("[,]+");
                skuCode = parts[1].trim().replaceAll("^0*", "");
            } else if (barcode.contains(" ")) {
                parts = barcode.split("\\s+");
                skuCode = parts[2].trim();
            }
            int index = checkIsBarcodeExist(barcode);
            if (index == -1) {
                if (!barcodes.contains(barcode)) {

                    ItemDetailsList item = getItemBySkuCode(skuCode);
                    if (item != null) {
                        if (Integer.parseInt(item.getPickedQty()) > Integer.parseInt(item.getScannedQty())) {
                            barcodeList.add(barcodeHashMap);
                            int scannedQty = Integer.parseInt(item.getScannedQty()) + 1;
                            int remainingQty = Integer.parseInt(item.getPickedQty()) - scannedQty;
                            item.setScannedQty(String.valueOf(scannedQty));
                            item.setRemainingQty(String.valueOf(remainingQty));
                            adapter.notifyItemChanged(itemDetailsLists.indexOf(item));
                            barcodes.add(barcode);
                            mediaPlayer.start();
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "All expected items are already scanned for this SKU");
                        }
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "The Scanned item doesn't match with the picked item");
                    }
                }
            } else {
                int tagCount = Integer.parseInt(barcodeList.get(index).get("COUNT"), 10) + 1;
                barcodeHashMap.put("COUNT", String.valueOf(tagCount));
                barcodeList.set(index, barcodeHashMap);
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.barcode_already_scanned));
                mediaPlayerErr.start();

            }
            binding.recCount.setText("" + barcodeList.size());
            adapter.notifyDataSetChanged();
            END_DATE = AssetUtils.getSystemDateTimeInFormatt();
            Log.e("BarcodesSize", "" + barcodeList.size());
            try {
                if (scanner != null) {
                    scanner.stopScan();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            // Barcode format does not match, handle accordingly
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Barcode format does not match the expected format");
        }
    }

    private void getSKUDetails(String DC_NO) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put(APIConstants.K_DC_NO, DC_NO);
            jsonObject.put("Type", "DISPATCH_QR");
            Log.e("JSONReq", SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_SKU_DETAILS);
            Log.e("JSONReq1", jsonObject.toString());
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_SKU_DETAILS).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response", response.toString());
                            if (response != null) {
                                try {
                                    if (response.getBoolean("status")) {
                                        JSONArray dataArray = response.getJSONArray("data");
                                        parseSKUDetails(dataArray);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        String message = response.getString("message");
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                    }
                                } catch (JSONException e) {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                                }
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                           /* String orderDetailsString = AssetUtils.getJsonFromAssets(context, "updateworkorderstatus.json");
                            try {
                                JSONObject mainObject = new JSONObject(orderDetailsString);
                                parseWorkDetailsObjectAndDoAction(mainObject);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }*/
                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            }
                        }
                    });
        } catch (JSONException e) {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    private void parseSKUDetails(JSONArray dataArray) {
        if (itemDetailsLists != null) {
            itemDetailsLists.clear();

        }
        if (dataArray.length() > 0) {
            for (int i = 0; i < dataArray.length(); i++) {
                try {
                    ItemDetailsList itemList = new ItemDetailsList();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String SKUCode = dataObject.getString("SkuCode");
                    String ItemDesc = dataObject.getString("ItemDesc");
                    String PickedQty = dataObject.getString("PickedQty");
                    itemList.setItemDesc(ItemDesc);
                    itemList.setSkuCode(SKUCode);
                    itemList.setPickedQty(PickedQty);
                    itemList.setOriginalPickedQty(PickedQty);
                    itemDetailsLists.add(itemList);
                    originalItemList.add(itemList);

                } catch (JSONException e) {
                    adapter.notifyDataSetChanged();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void setDefault() {
                CURRENT_EPC = "";
                IS_SCANNING_LOCKED = false;
                allow_trigger_to_press = true;
                if (barcodeList != null) {
                    barcodeList.clear();
                }
                if (barcodes != null) {
                    barcodes.clear();
                }
        binding.rvPallet.setAdapter(null);
        itemDetailsLists.clear();
        for (ItemDetailsList model : originalItemList) {
            ItemDetailsList newModel = new ItemDetailsList();
            newModel.setItemDesc(model.getItemDesc());
            newModel.setSkuCode(model.getSkuCode());
            newModel.setOriginalPickedQty(model.getOriginalPickedQty());
            newModel.setPickedQty(model.getOriginalPickedQty());
            newModel.setScannedQty("0");
            newModel.setRemainingQty(model.getOriginalPickedQty());
            itemDetailsLists.add(newModel);
        }
        adapter = new skuitemListAdapter(context, itemDetailsLists);
        binding.rvPallet.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        binding.recCount.setText("" + barcodeList.size());

    }

    private ItemDetailsList getItemBySkuCode(String skuCode) {
        for (ItemDetailsList item : itemDetailsLists) {
            if (item.getSkuCode().equals(skuCode)) {
                return item;
            }
        }
        return null;
    }

    private boolean areAllItemsScanned() {
        for (ItemDetailsList item : itemDetailsLists) {
            if (!item.getRemainingQty().equals("0")) {
                return false;
            }
        }
        return true;
    }

    public void showCustomConfirmationDialogForSpecial(String msg, final String action) {
        if (customConfirmationDialogSpec != null) {
            customConfirmationDialogSpec.dismiss();
        }
        customConfirmationDialogSpec = new Dialog(context);
        if (customConfirmationDialogSpec != null) {
            customConfirmationDialogSpec.dismiss();
        }
        customConfirmationDialogSpec.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customConfirmationDialogSpec.setCancelable(false);
        customConfirmationDialogSpec.setContentView(R.layout.custom_alert_dialog_layout4);
        TextView text = (TextView) customConfirmationDialogSpec.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) customConfirmationDialogSpec.findViewById(R.id.btnUpload);
        Button dialogButtonCancel = (Button) customConfirmationDialogSpec.findViewById(R.id.btnCancel);
        EditText dialogPassword = (EditText) customConfirmationDialogSpec.findViewById(R.id.password);
        RecyclerView dialogItem = (RecyclerView) customConfirmationDialogSpec.findViewById(R.id.recycler_view);
        dialogItem.setLayoutManager(new GridLayoutManager(context, 1));
        confirmationAdapter = new skuitemListAdapter(context, itemDetailsLists);
        confirmationAdapter.notifyDataSetChanged();
        dialogItem.setAdapter(confirmationAdapter);
        dialogButton.setText("YES");
        dialogButtonCancel.setText("NO");
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfirmationDialogSpec.dismiss();
                if (action.equals("UPLOAD")) {
                    if (!dialogPassword.getText().toString().equals("")) {
                        Log.e("Password", dialogPassword.getText().toString());
                        if (dialogPassword.getText().toString().equals(Password)) {
                            allow_trigger_to_press = false;
                            uploadInventoryToServer();
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid password to proceed");
                        }
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid password to proceed");
                    }
                }
            }
        });
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfirmationDialogSpec.dismiss();
            }
        });
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialogSpec.show();
    }

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Do you want to back?", "BACK");
        //super.onBackPressed();
    }
}