package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.psl.pallettracking.adapters.AssetPalletMapAdapter;
import com.psl.pallettracking.adapters.skuitemListAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAssetPalletMappingBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.MqttPublisher;
import com.psl.pallettracking.helper.MqttSubscriber;
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

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

public class AssetPalletMappingActivity extends AppCompatActivity implements DecodeInfoCallBack {

    private Context context = this;
    private SeuicGlobalRfidHandler rfidHandler;
    private ActivityAssetPalletMappingBinding binding;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerErr;
    private ConnectionDetector cd;
    private DatabaseHandler db;
    String PALLET_TAG_ID = "";
    String CURRENT_EPC = "";
    String START_DATE = "";
    String END_DATE = "";
    boolean IS_PALLET_TAG_SCANNED = false;
    boolean IS_SCANNING_LOCKED = false;
    boolean IS_SCANNING_ALREADY_STARTED = false;
    private boolean allow_trigger_to_press = true;
//    MqttPublisher publisher;
//    MqttSubscriber subscriber;

    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    public ArrayList<HashMap<String, String>> barcodeList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> hashMap = new HashMap<>();
    HashMap<String, String> barcodeHashMap = new HashMap<>();
    private List<String> epcs = new ArrayList<>();
    private List<String> barcodes = new ArrayList<>();
    Scanner scanner;
    AssetPalletMapAdapter adapter;
    String[] barcodeArray = {"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10"};
    String menu_id = AppConstants.MENU_ID_CARTON_PALLET_MAPPING;
    String activity_type = "";
    String DC_NO = "";
    //String Location_Name = "";
    String processType = null;
    List<ItemDetailsList> itemDetailsLists, originalItemList;
    private String Password = "PASSRECEIVING007";
    private boolean IS_DIFFERENT_SKU = false;
    private boolean IS_QTY_EXCEED = false;
    private int EXCEEDED_QTY = 0;
    private String DIFF_SKU = "";
    private String EXCEEDED_SKU = "";

    @Override
    public void onBackPressed() {

        if (allow_trigger_to_press) {
            showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK","","");
        } else {
            stopInventory();
            showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK","","");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_asset_pallet_mapping);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_pallet_mapping);
        getSupportActionBar().hide();
        cd = new ConnectionDetector(context);
        db = new DatabaseHandler(context);


        activity_type = db.getMenuActivityNameByMenuID(menu_id);

        adapter = new AssetPalletMapAdapter(context, barcodeList);
        binding.LvTags.setAdapter(adapter);
        if (itemDetailsLists != null) {
            itemDetailsLists.clear();
        }
        itemDetailsLists = new ArrayList<>();
        originalItemList = new ArrayList<>();
        //Truck_Number = getIntent().getStringExtra("TruckNumber");
        //Location_Name = getIntent().getStringExtra("LocationName");
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
                if (barcodeList.size() > 0) {
                    if(!IS_DIFFERENT_SKU){
                        if(!IS_QTY_EXCEED){
                            showCustomConfirmationDialog("Are you sure you want to upload", "UPLOAD","","");
                        }
                        else{
                            showCustomConfirmationDialogForSpecial("Do you want to save excess "+EXCEEDED_QTY+" quantity for "+EXCEEDED_SKU+" ?", "UPLOAD");
                        }
                    }
                    else{
                        showCustomConfirmationDialogForSpecial("Do you want to save the different SKU?\n" + "SKU: "+ DIFF_SKU, "UPLOAD");
                    }
                }
                else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "There are no items to upload");
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
        binding.btnClear.setOnClickListener(v -> {
            if (allow_trigger_to_press) {
                showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "CANCEL","","");
            }
        });
        binding.btnBack.setOnClickListener(v -> {
            if (allow_trigger_to_press) {
                showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK","","");
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
                        scanner = ScannerFactory.getScanner(AssetPalletMappingActivity.this);
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
                                String tid = "FFFFFFFFFFFFFFFFFFFFFFFF";
                                if (!allow_trigger_to_press) {

                                    SCANNED_EPC = maxRssiEpc;
                                    //changed
                                    //SCANNED_EPC = epc;
                                    CURRENT_EPC = SCANNED_EPC;
                                    hashMap = new HashMap<>();
                                    hashMap.put("EPC", maxRssiEpc);
                                    hashMap.put("TID", tid);
                                    hashMap.put("COUNT", "1");
                                    hashMap.put("STATUS", "0");
                                    hashMap.put("MESSAGE", "");
                                    int index = checkIsExist(maxRssiEpc);
                                    if (index == -1) {
                                        tagList.add(hashMap);
                                        if (!epcs.contains(maxRssiEpc)) {
                                            epcs.add(maxRssiEpc);
                                        }
                                    } else {
                                        int tagCount = Integer.parseInt(tagList.get(index).get("COUNT"), 10) + 1;
                                        hashMap.put("COUNT", String.valueOf(tagCount));
                                        tagList.set(index, hashMap);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
//        subscriber = new MqttSubscriber();
//        subscriber.subscribe("test/topic", new MqttCallback() {
//            @Override
//            public void disconnected(MqttDisconnectResponse disconnectResponse) {
//                Log.d("Disconnected Message", disconnectResponse.getReasonString());
//            }
//
//            @Override
//            public void mqttErrorOccurred(MqttException exception) {
//                Log.e("Exception Message", exception.getMessage());
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d("Received Message", new String(message.getPayload()));
//                String receivedMessage =  new String(message.getPayload());
//                Log.d("Received Message1", receivedMessage);
//                Toast.makeText(context, "rec", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void deliveryComplete(IMqttToken token) {
//                Log.d("Delivery Complete", "Message delivered");
//            }
//
//            @Override
//            public void connectComplete(boolean reconnect, String serverURI) {
//                Log.d("Connected Message", "connected to: " + serverURI);
//            }
//
//            @Override
//            public void authPacketArrived(int reasonCode, MqttProperties properties) {
//                Log.d("Auth Packet Arrived", "Auth reason: " + reasonCode);
//            }
//        });
    }

    @Override
    public void onDecodeComplete(DecodeInfo info) {

        String barcode = info.barcode;
        if (barcode != null && !barcode.isEmpty()) {
            Log.e("Barcode", info.barcode);
            addBarcodeToList(barcode);
        }
    }


    private void startScanning() {
        try {
            if (scanner != null) {
                scanner.open();
                scanner.setDecodeInfoCallBack(AssetPalletMappingActivity.this);
                scanner.enable();
                scanner.startScan();
                //showProgress(context, "Please wait...Scanning Asset Barcode/QR");
                //mediaPlayer.start();
                //new Handler().postDelayed(() -> {
                    //hideProgressDialog();
                    //allow_trigger_to_press = true;
                    //scanner.stopScan();
                    //END_DATE = AssetUtils.getSystemDateTimeInFormatt();
                //}, 2000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int CURRENT_INDEX = -1;

    //HashMap<String, String> hashMap = new HashMap<>();//,tagList.get(position).get("MESSAGE")
    public void onListItemClicked(HashMap<String, String> hashmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (allow_trigger_to_press) {
                    int index = checkIsBarcodeExist(hashmap.get("BARCODE"));
                    if (index == -1) {

                    } else {
                        CURRENT_INDEX = index;
                        showCustomConfirmationDialog("Are you sure you want to delete", "DELETE",hashmap.get("COUNT"),hashmap.get("BARCODE"));

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
                                    //binding.edtRfidNumber.setText(PALLET_TAG_ID);
                                    String PalletName = db.getProductNameByProductTagId(PALLET_TAG_ID);
                                    if(!PalletName.equalsIgnoreCase(AppConstants.UNKNOWN_ASSET)){
                                        binding.edtRfidNumber.setText(PalletName);
                                        IS_PALLET_TAG_SCANNED = true;
                                        binding.textHint.setVisibility(View.VISIBLE);
                                        binding.textCount.setVisibility(View.VISIBLE);
                                    } else{
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Invalid Pallet Name. Please scan another Pallet");
                                    }
                                } else {
                                    changeImageStatusToRfidScan();
                                    if (assettpid.equalsIgnoreCase("03")) {
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.scan_pallet_tag_error));
                                    } else {
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                                    }
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
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                    }
                } else {
                    changeImageStatusToRfidScan();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));

                }
            } catch (Exception e) {
                Log.e("INEXCEPTION", "" + e.getMessage());
                changeImageStatusToRfidScan();
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
            }

//        } else if (tagList.size() == 0) {
//            changeImageStatusToRfidScan();
//            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
//            //Toast.makeText(getActivity(),"Invalid RFID Tag0",Toast.LENGTH_SHORT).show();
//        } else if (tagList.size() > 1) {
//            changeImageStatusToRfidScan();
//            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.multiple_rfid_error));
//            //Toast.makeText(getActivity(),"Invalid RFID Tag0",Toast.LENGTH_SHORT).show();
//
//        }
    }

    public void setDefault() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSKUDetails(DC_NO);
                CURRENT_EPC = "";
                EXCEEDED_SKU = "";
                EXCEEDED_QTY = 0;
                DIFF_SKU = "";
                IS_DIFFERENT_SKU = false;
                IS_QTY_EXCEED = false;
                IS_SCANNING_LOCKED = false;
                IS_SCANNING_ALREADY_STARTED = false;
                changeImageStatusToRfidScan();
                binding.imgStatus.setImageDrawable(getDrawable(R.drawable.rfidscan));
                allow_trigger_to_press = true;
                binding.textHint.setVisibility(View.GONE);
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
                END_DATE = "";
                if(SharedPreferencesManager.getWarehouseId(context) == 4){
                    IS_PALLET_TAG_SCANNED = true;
                    binding.edtRfidNumber.setText("CWC_P1");
                    START_DATE = AssetUtils.getSystemDateTimeInFormatt();
                    PALLET_TAG_ID = "14020000000150534C202020";
                } else{
                    PALLET_TAG_ID = "";
                    IS_PALLET_TAG_SCANNED = false;
                    START_DATE = "";
                    binding.edtRfidNumber.setText("");
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
                //PALLET_TAG_ID = "";
                binding.edtRfidNumber.setText("");
                binding.imgStatus.setImageDrawable(getDrawable(R.drawable.rfidscan));
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


    public int checkIsExist(String epc) {
        if (StringUtils.isEmpty(epc)) {
            return -1;
        }
        return binarySearch(epcs, epc);
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

    Dialog customConfirmationDialog,customConfirmationDialogSpec;

    public void showCustomConfirmationDialog(String msg, final String action, String count, String barcode) {
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
                        String[] parts;
                        String skuCode = "";
                        if (barcode.contains(",")) {
                            parts = barcode.split("[,]+");
                            skuCode = parts[1].trim().replaceAll("^0*", "");
                        } else if (barcode.contains(" ")) {
                            parts = barcode.split("\\s+");
                            skuCode = parts[2].trim().replaceAll("^0*", "");
                        }
                        ItemDetailsList item = getItemBySkuCode(skuCode);
                        if (item != null) {
                                EXCEEDED_QTY = EXCEEDED_QTY - 1;
                                if(EXCEEDED_QTY == 0){
                                    IS_QTY_EXCEED = false;
                                    EXCEEDED_SKU = "";
                                }
                        }
                        else{
                            IS_DIFFERENT_SKU = false;
                            DIFF_SKU = "";
                        }
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
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }

    private void uploadInventoryToServer() {

        if (barcodeList.size() > 0) {
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
            if (barcodeList.size() > 0) {
                try {
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
                    jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    //jsonobject.put(APIConstants.K_ACTIVITY_ID, "AssetPallet" + SharedPreferencesManager.getDeviceId(context) + AssetUtils.getSystemDateTimeInFormatt());
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, activity_type);
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, barcodeList.size());
                    jsonobject.put(APIConstants.K_PARENT_TAG_ID, PALLET_TAG_ID);
                    jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "Pallet");
                    jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
                    jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));
                    jsonobject.put(APIConstants.K_DRN, DC_NO);
                    jsonobject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
                    //jsonobject.put(APIConstants.K_PALLET_ID, CURRENT_EPC);
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < barcodeList.size(); i++) {
                        JSONObject barcodeObject = new JSONObject();
                        String epc = barcodeList.get(i).get("BARCODE");
                        String Status =  barcodeList.get(i).get("STATUS");
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, epc);
                        barcodeObject.put(APIConstants.K_ITEM_STATUS, Status);
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
//publisher = new MqttPublisher();
//publisher.connectAndPublish("test/topic", result.toString());
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

    String[] barcodesss = {
            "GRB020PSL0823SKU001",
            "GRB020PSL0823SKU002",
            "GRB020PSL0823SKU0011",
            "GRB020PSL0823SKU0012"
    };

    private void uploadDummyData() {
        try {
            JSONObject jsonobject = null;
            jsonobject = new JSONObject();
            jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
            jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
            jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonobject.put(APIConstants.K_ACTIVITY_ID, "AssetPallet" + SharedPreferencesManager.getDeviceId(context) + AssetUtils.getSystemDateTimeInFormatt());
            jsonobject.put(APIConstants.K_ACTIVITY_TYPE, activity_type);
            jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());
            jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());
            jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
            jsonobject.put(APIConstants.K_INVENTORY_COUNT, 4);
            jsonobject.put(APIConstants.K_PARENT_TAG_ID, "0F0212341234123412341234");
            jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "Pallet");
            jsonobject.put(APIConstants.K_PALLET_ID, CURRENT_EPC);
            JSONArray js = new JSONArray();
            for (int i = 0; i < 4; i++) {
                JSONObject barcodeObject = new JSONObject();
                String epc = barcodesss[i];
                barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                barcodeObject.put(APIConstants.K_ASSET_TYPE_NAME, epc);
                barcodeObject.put(APIConstants.K_ACTIVITY_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                barcodeObject.put(APIConstants.K_TRANSACTION_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());

                js.put(barcodeObject);
            }
            jsonobject.put(APIConstants.K_DATA, js);

            uploadInventory(jsonobject, APIConstants.M_UPLOAD_INVENTORY, "Please wait...\n" + " Mapping is in progress");


        } catch (JSONException e) {

            //return null;
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

    private void checkResponseAndDovalidations(JSONArray dataArray) {
        if (dataArray.length() > 0) {
            boolean workStatus = true;
            for (int i = 0; i < dataArray.length(); i++) {
                try {
                    JSONObject dataObject = dataArray.getJSONObject(i);

                    String asset_number = "";
                    String asset_name = "";
                    String pallet_tag_id = "";
                    String pallet_name = "";
                    if (dataObject.has("ParentTagID")) {
                        pallet_tag_id = dataObject.getString("ParentTagID").trim();
                    }

                    if (dataObject.has("ParentAssetName")) {
                        pallet_name = dataObject.getString("ParentAssetName").trim();
                    }

//                    if (dataObject.has("ChildAssetID")) {
//                        asset_number = dataObject.getString("ChildAssetID").trim();
//                    }
                    if (dataObject.has("ItemName")) {
                        asset_name = dataObject.getString("ItemName").trim();
                    }

                    String status = dataObject.getString("status").trim();
                    String message = dataObject.getString("message").trim();
                    if (status.equalsIgnoreCase("false")) {
                        workStatus = false;
                    }
                    if (status.equalsIgnoreCase("true")) {
                        workStatus = true;
                    }
                    barcodeHashMap = new HashMap<>();
                    barcodeHashMap.put("EPC", pallet_tag_id);
                    barcodeHashMap.put("BARCODE", asset_number);
                    barcodeHashMap.put("ASSETNAME", asset_number);
                    barcodeHashMap.put("COUNT", "1");
                    barcodeHashMap.put("STATUS", status);
                    barcodeHashMap.put("MESSAGE", message);
                    int index = checkIsBarcodeExist(asset_name);
                    if (index == -1) {
                        barcodeHashMap.put("STATUS", status);
                        barcodeList.add(barcodeHashMap);
                        if (!barcodes.contains(asset_number)) {
                            barcodes.add(asset_number);
                        }
                    } else {
                        int tagCount = Integer.parseInt(barcodeList.get(index).get("COUNT"), 10) + 1;
                        barcodeHashMap.put("COUNT", String.valueOf(tagCount));
                        barcodeList.set(index, barcodeHashMap);
                    }

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
                    adapter.notifyDataSetChanged();
                    binding.textCount.setText("Count : " + barcodeList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    allow_trigger_to_press = true;
                }
            }
            if (workStatus) {
                setDefault();
                AssetUtils.showCommonBottomSheetSuccessDialog(context, "Mapping Done Successfully");
            }
        }

    }

    private void addBarcodeToList(String barcode) {
        hideProgressDialog();
        allow_trigger_to_press = true;
        String skuCode = "";
        String[] parts = barcode.split("[,\\s]+");
        if (parts.length >3 && parts.length<6) {
        barcodeHashMap = new HashMap<>();
        barcodeHashMap.put("EPC", CURRENT_EPC);
        barcodeHashMap.put("BARCODE", barcode);
        barcodeHashMap.put("ASSETNAME", barcode);
        barcodeHashMap.put("COUNT", "1");
        barcodeHashMap.put("STATUS", "Good");
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
                if(db.isSKUExist(skuCode)){
                    barcodeList.add(barcodeHashMap);
                barcodes.add(barcode);
                mediaPlayer.start();
                    ItemDetailsList item = getItemBySkuCode(skuCode);
                    if (item != null) {
                        int scannedQty = 0;
                        if (Integer.parseInt(item.getPickedQty()) > Integer.parseInt(item.getScannedQty())) {
                            scannedQty = Integer.parseInt(item.getScannedQty()) + 1;
                            item.setScannedQty(String.valueOf(scannedQty));
                        } else {
                            IS_QTY_EXCEED = true;
                            scannedQty = Integer.parseInt(item.getScannedQty()) + 1;
                            item.setScannedQty(String.valueOf(scannedQty));
                            int pickedQty = Integer.parseInt(item.getPickedQty());
                            pickedQty = pickedQty <= 0 ? 0 : pickedQty;
                            EXCEEDED_QTY = scannedQty - pickedQty;
                            EXCEEDED_SKU = skuCode;
                        }
                    } else {
                        DIFF_SKU = db.getItemNameByItemCode(skuCode);
                        IS_DIFFERENT_SKU = true;
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, skuCode+" doesn't exist. Please contact admin.");
                }

            }
        } else {
            int tagCount = Integer.parseInt(barcodeList.get(index).get("COUNT"), 10) + 1;
            barcodeHashMap.put("COUNT", String.valueOf(tagCount));
            //barcodeList.set(index, barcodeHashMap);
            mediaPlayerErr.start();
            showDuplicateBarcodeDialog(barcode, index);
            //AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.barcode_already_scanned));

        }
        Log.e("Barcode", barcodeHashMap.toString());
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
            if(IS_SCANNING_ALREADY_STARTED){
                IS_SCANNING_ALREADY_STARTED = false;
               // startScanning();
            }
        }, 500);
        } else {
            // Barcode format does not match, handle accordingly
            // For example, show an error message
            mediaPlayerErr.start();
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Barcode format does not match the expected format");
        }
    }
    private void getSKUDetails(String DC_NO) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put(APIConstants.K_DC_NO, DC_NO);
            jsonObject.put("Type", "RECEIVING_QR");
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
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            }
        }
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
        customConfirmationDialogSpec.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customConfirmationDialogSpec.setContentView(R.layout.custom_alert_dialog_layout5);
        TextView text = (TextView) customConfirmationDialogSpec.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) customConfirmationDialogSpec.findViewById(R.id.btnUpload);
        Button dialogButtonCancel = (Button) customConfirmationDialogSpec.findViewById(R.id.btnCancel);
        EditText dialogPassword = (EditText) customConfirmationDialogSpec.findViewById(R.id.password);
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
    private ItemDetailsList getItemBySkuCode(String skuCode) {
        for (ItemDetailsList item : itemDetailsLists) {
            if (item.getSkuCode().equals(skuCode)) {
                return item;
            }
        }
        return null;
    }
    private void showDuplicateBarcodeDialog(final String barcode, final int index) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.custom_bottom_sheet_duplicate_barcode_dialog);
        dialog.setCancelable(false);

        TextView errorMessage = dialog.findViewById(R.id.textMessage);


        Button button = dialog.findViewById(R.id.damagedBtn);
        if(barcodeList.get(index).get("STATUS").equalsIgnoreCase("Good")){
            errorMessage.setText("Barcode/Qr code already scanned. Mark as Damage?");
            button.setVisibility(View.VISIBLE);
        } else{
            errorMessage.setText("Barcode/Qr code already scanned and marked as damage.");
            button.setVisibility(View.INVISIBLE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    // Mark as Damaged
                    barcodeList.get(index).put("STATUS", "Damage");
                    Log.e("Stat", barcodeList.toString());
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
            }
        });

        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    // Mark as Good if no action taken
                    if(barcodeList.get(index).get("STATUS").equalsIgnoreCase("Good")){
                        barcodeList.get(index).put("STATUS", "Good");
                    } else {
                        barcodeList.get(index).put("STATUS", "Damage");
                    }
                    Log.e("Stat", barcodeList.toString());
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                }
            }
        }, 5000); // 5 seconds
    }
}