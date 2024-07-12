package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityLoadingItemMappingBinding;
import com.psl.pallettracking.databinding.ActivityTruckMappingForDispatchBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.seuic.uhf.EPC;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoadingItemMappingActivity extends AppCompatActivity {
    private Context context = this;
    private SeuicGlobalRfidHandler rfidHandler;
    private ConnectionDetector cd;
    private ActivityLoadingItemMappingBinding binding;
    boolean IS_SCANNING_LOCKED = false;
    private boolean allow_trigger_to_press = true;
    boolean IS_DC_TAG_SCANNED = false;
    String CURRENT_EPC = "";
    String DC_TAG_ID = "";
    String DC_NO = "";
    HashMap<String, String> hashMap = new HashMap<>();
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_loading_item_mapping);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loading_item_mapping);
        getSupportActionBar().hide();
        db = new DatabaseHandler(context);
        cd = new ConnectionDetector(context);

        setDefault();
        binding.btnTruckRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IS_DC_TAG_SCANNED){
                    if(!TextUtils.isEmpty(binding.textDCNumber.getText())){
                        showProgress(context, "Processing");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent AssetPalletMappingIntent = new Intent(LoadingItemMappingActivity.this, AssetPalletMappingDispatchActivity.class);
                                AssetPalletMappingIntent.putExtra("DRN", DC_NO);
                                AssetPalletMappingIntent.putExtra("DC_TAG_ID", DC_TAG_ID);
                                startActivity(AssetPalletMappingIntent);
                            }
                        }, 1000);
                    }
                    else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan valid DC tag to proceed");
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan DC tag");
                }
            }
        });
        binding.btnTruckRegisterWithoutQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(IS_DC_TAG_SCANNED) {
                    if(!TextUtils.isEmpty(binding.textDCNumber.getText())){
                    //if(!TextUtils.isEmpty(DC_NO)) {
                    showProgress(context, "Processing");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Intent AssetPalletMappingIntent = new Intent(LoadingItemMappingActivity.this, AsssetPalletMappingWithItemForDispatchV1Activity.class);
                            AssetPalletMappingIntent.putExtra("DRN", DC_NO);
                            AssetPalletMappingIntent.putExtra("DC_TAG_ID", DC_TAG_ID);
                            //AssetPalletMappingIntent.putExtra("TruckNumber", SharedPreferencesManager.getTruckNumber(context));
                            //AssetPalletMappingIntent.putExtra("LocationName", SharedPreferencesManager.getLocationName(context));

                            startActivity(AssetPalletMappingIntent);
                        }
                    }, 1000);
//                    } else{
//                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select a DC No");
//                    }
                    }
                    else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan valid DC tag to proceed");
                    }
                }
                else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan DC tag");
                }
                //hideProgressDialog();
            }
        });
        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allow_trigger_to_press) {
                    setDefault();
                }
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allow_trigger_to_press) {
                    showCustomConfirmationDialog(getResources().getString(R.string.confirm_back), "BACK");
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
        rfidHandler = new SeuicGlobalRfidHandler();
        rfidHandler.onCreate(context, new RFIDInterface() {
            @Override
            public void handleTriggerPress(boolean pressed) {
                runOnUiThread(() -> {
                    if (pressed) {
                        if (!IS_SCANNING_LOCKED) {
                            startInventory();
                            new Handler().postDelayed(() -> {
                                hideProgressDialog();
                                allow_trigger_to_press = true;
                                stopInventory();
                                stopInventoryAndDoValidations();
                            }, 2000);
                        }
                    }
                });
            }

            @Override
            public void RFIDInitializationStatus(boolean status) {
                runOnUiThread(() -> {
                    hideProgressDialog();
                });
            }

            @Override
            public void handleLocateTagResponse(int value, int size) {
                runOnUiThread(() -> {
                });
            }

            @Override
            public void onDataReceived(List<EPC> epcList) {
                runOnUiThread(() -> {
                    if (epcList != null) {
                        if (epcList.size() > 0) {
                            int maxRssi = Integer.MIN_VALUE;//changed
                            String maxRssiEpc = null;//changed
                            for (int i = 0; i < epcList.size(); i++) {
                                String epc = epcList.get(i).getId();
                                String tid = "FFFFFFFFFFFFFFFFFFFFFFFF";
                                int rssivalue = epcList.get(i).rssi;//changed
                                if (rssivalue > maxRssi) {
                                    maxRssi = rssivalue;
                                    maxRssiEpc = epc;
                                }//changed


                            }
                            if (maxRssiEpc != null) {
                                if (!allow_trigger_to_press) {
                                    SCANNED_EPC = maxRssiEpc;
                                    CURRENT_EPC = SCANNED_EPC;
                                    hashMap = new HashMap<>();
                                    hashMap.put("EPC", maxRssiEpc);
                                    hashMap.put("TID", maxRssiEpc);
                                    hashMap.put("STATUS", "0");
                                    hashMap.put("MESSAGE", "");
                                    tagList.add(hashMap);
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    private String SCANNED_EPC = "";

    public void stopInventoryAndDoValidations() {
        hideProgressDialog();
        allow_trigger_to_press = true;
        if (tagList.size() > 0) {
            hideProgressDialog();
            try {
                if (SCANNED_EPC != null) {
                    if (!SCANNED_EPC.isEmpty()) {
                        CURRENT_EPC = SCANNED_EPC;
                        SCANNED_EPC = "";
                        Log.e("EPC", CURRENT_EPC);
                        CURRENT_EPC = CURRENT_EPC.substring(0, 24);
                        String companycode = CURRENT_EPC.substring(0, 2);
                        companycode = AssetUtils.hexToNumber(companycode);
                        String assettpid = CURRENT_EPC.substring(2, 4);
                        String serialnumber = CURRENT_EPC.substring(4, 12);
                        if(AssetUtils.getTagType(CURRENT_EPC).equals(AssetUtils.TYPE_PALLET)){
                            if(db.checkAssetNameByProductTagId(CURRENT_EPC)){
                                IS_DC_TAG_SCANNED = true;
                                DC_TAG_ID = CURRENT_EPC;
                                binding.edtDCTag.setText(db.getProductNameByProductTagId(DC_TAG_ID));
                                getDCDetails();
                            }
                            else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a DC tag");
                            }
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a DC tag");
                        }
                        //binding.edtTruckID.setText(SharedPreferencesManager.getTruckNumber(context));
                    }
                }
            } catch (Exception e) {
                Log.e("INEXCEPTION", "" + e.getMessage());
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
            }

        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
        }
    }
    private void getDCDetails() {
        if (cd.isConnectingToInternet()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(APIConstants.K_DC_TAG_ID, CURRENT_EPC);
                fetchDCDetails(jsonObject, APIConstants.M_DC_DETAILS, "Please wait...\n" + "Getting DC Details");
            } catch (Exception ex) {

            }
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    public void fetchDCDetails(JSONObject request, String METHOD_NAME, String progress_message) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        showProgress(context, progress_message);
        AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME).addJSONObjectBody(request)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build().getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {
                            try {
                                hideProgressDialog();
                                Log.e("DCDETAILS", response.toString());
                                if (response.has(APIConstants.K_STATUS)) {
                                    if (response.getBoolean(APIConstants.K_STATUS)) {
                                        Log.e("Status", response.getString(APIConstants.K_STATUS));
                                        if (response.has(APIConstants.K_DATA)) {
                                            Log.e("DATAARRAY1", response.getString(APIConstants.K_DATA));
                                            JSONObject dataObject;
                                            dataObject = response.getJSONObject(APIConstants.K_DATA);
                                            Log.e("DATAARRAY", dataObject.toString());
                                            if (dataObject != null) {
                                                if (dataObject.length() > 0) {
                                                    parseTruckDetailsFetchAndDoAction(dataObject);
                                                } else {
                                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "No Asset Master Found");
                                                }
                                            } else {
                                                AssetUtils.showCommonBottomSheetErrorDialog(context, "No Asset Master Found");
                                            }
                                        }

                                    } else {
                                        String message = response.getString(APIConstants.K_MESSAGE);
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                    }
                                }
                            } catch (JSONException e) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "Invalid RFID got scanned, Please scan truck tag");
                            }
                        } else {
                            hideProgressDialog();
                            // Toast.makeText(context,"Communication Error",Toast.LENGTH_SHORT).show();
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
        //TODO CALL Without Barcode API
    }

    private void parseTruckDetailsFetchAndDoAction(JSONObject responseObject) {
        if (responseObject.length() > 0) {
            try {
                String truck_number = "";
                String DRN = "";
                String process_type = "";
                String location_name = "";
                if (responseObject.has("TruckNumber")) {
                    truck_number = responseObject.getString("TruckNumber");
                    binding.textTruckNumber.setText(truck_number);
                    SharedPreferencesManager.setTruckNumber(context, truck_number);
                }
                if (responseObject.has("DRN")) {
                    DRN = responseObject.getString("DRN");
                    binding.textDCNumber.setText(DRN);
                    DC_NO = DRN;
                    SharedPreferencesManager.setDRN(context, DRN);
                }
                if (responseObject.has("ProcessType")) {
                    process_type = responseObject.getString("ProcessType");
                    SharedPreferencesManager.setProcessType(context, process_type);
                }
                if (responseObject.has("LocationName")) {
                    location_name = responseObject.getString("LocationName");
                    binding.textLocationName.setText(location_name);
                    SharedPreferencesManager.setLocationName(context, location_name);
                }
            } catch (Exception ex) {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
            }
        }
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
    public void setDefault() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DC_TAG_ID = "";
                CURRENT_EPC = "";
                IS_SCANNING_LOCKED = false;
                binding.edtDCTag.setText("");
                binding.textTruckNumber.setText("");
                //binding.searchableTextView.setText(default_source_item);
                binding.textLocationName.setText("");
                binding.textDCNumber.setText("");
                allow_trigger_to_press = true;
                if (tagList != null) {
                    tagList.clear();
                }
                DC_NO = "";
            }
        });
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
        //setDefault();
    }
    @Override
    public void onDestroy() {
        rfidHandler.onDestroy();
        super.onDestroy();
        finish();
    }
    @Override
    public void onPause() {
        super.onPause();
        binding.btnClear.setVisibility(View.GONE);
        binding.btnPower.setVisibility(View.GONE);
        binding.btnBack.setVisibility(View.VISIBLE);
        rfidHandler.onPause();
    }

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog(getResources().getString(R.string.confirm_back), "BACK");
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
                if (action.equals("BACK")) {
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

}