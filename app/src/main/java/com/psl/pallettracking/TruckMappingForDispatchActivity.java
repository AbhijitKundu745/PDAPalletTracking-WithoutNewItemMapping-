package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.databinding.ActivityTruckMappingBinding;
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

public class TruckMappingForDispatchActivity extends AppCompatActivity {
    private Context context = this;
    private SeuicGlobalRfidHandler rfidHandler;
    private ConnectionDetector cd;
    private ActivityTruckMappingForDispatchBinding binding;
    boolean IS_SCANNING_LOCKED = false;
    private boolean allow_trigger_to_press = true;
    boolean IS_TRUCK_TAG_SCANNED = false;
    private String SELECTED_ITEM = "";
    private String default_source_item = "Select DC No";
    String CURRENT_EPC = "";
    String TRUCK_TAG_ID = "";
    String DC_NO = "";
    HashMap<String, String> hashMap = new HashMap<>();
    public ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    Dialog dialog;
    SearchableAdapter searchableAdapter;
    ArrayList<String> DRNList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_truck_mapping_for_dispatch);
        Log.d("TruckMappingForDispatchActivity", "onCreate called");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_truck_mapping_for_dispatch);
        getSupportActionBar().hide();
        cd = new ConnectionDetector(context);

        setDefault();
        binding.btnTruckRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IS_TRUCK_TAG_SCANNED){
                    if(!TextUtils.isEmpty(DC_NO)){
                        showProgress(context, "Processing");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent AssetPalletMappingIntent = new Intent(TruckMappingForDispatchActivity.this, AssetPalletMappingActivity.class);
                                AssetPalletMappingIntent.putExtra("DRN", DC_NO);
                                //AssetPalletMappingIntent.putExtra("LocationName", SharedPreferencesManager.getLocationName(context));

                                startActivity(AssetPalletMappingIntent);
                            }
                        }, 1000);
                    } else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select a DC No");
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan truck tag");
                }
            }
        });
        binding.btnTruckRegisterWithoutQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(IS_TRUCK_TAG_SCANNED) {
                    if(!TextUtils.isEmpty(DC_NO)) {
                        showProgress(context, "Processing");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent AssetPalletMappingIntent = new Intent(TruckMappingForDispatchActivity.this, AssetPalletWithItemActivity.class);
                                AssetPalletMappingIntent.putExtra("DRN", DC_NO);
                                //AssetPalletMappingIntent.putExtra("TruckNumber", SharedPreferencesManager.getTruckNumber(context));
                                //AssetPalletMappingIntent.putExtra("LocationName", SharedPreferencesManager.getLocationName(context));

                                startActivity(AssetPalletMappingIntent);
                            }
                        }, 1000);
                    } else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select a DC No");
                    }

                }else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan truck tag");
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

        binding.searchableTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize dialog
                dialog = new Dialog(TruckMappingForDispatchActivity.this);

                // set custom dialog
                dialog.setContentView(R.layout.dialog_searchable_spinner);

                // set custom height and width
                dialog.getWindow().setLayout(650, 800);

                // set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // show dialog
                dialog.show();

                // Initialize and assign variable
                EditText editText = dialog.findViewById(R.id.edit_text);
                ListView listView = dialog.findViewById(R.id.list_view);

                // Initialize array adapter
                searchableAdapter = new SearchableAdapter(TruckMappingForDispatchActivity.this, DRNList);

                // set adapter
                listView.setAdapter(searchableAdapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        searchableAdapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(IS_TRUCK_TAG_SCANNED) {
                            // when item selected from list
                            // set selected item on textView
                            // Dismiss dialog
                            dialog.dismiss();
                            SELECTED_ITEM = (String) searchableAdapter.getItem(position);
                            binding.searchableTextView.setText(SELECTED_ITEM);
                            if (SELECTED_ITEM.equalsIgnoreCase(default_source_item) || SELECTED_ITEM.equalsIgnoreCase("")) {
                                SELECTED_ITEM = "";

                            } else {
                                SharedPreferencesManager.setDRN(context, SELECTED_ITEM);
                                DC_NO = SELECTED_ITEM;
                            }
                        } else{
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan truck tag");
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
                        TRUCK_TAG_ID = CURRENT_EPC;
                        binding.edtTruckID.setText(TRUCK_TAG_ID);
                        //binding.edtTruckID.setText(SharedPreferencesManager.getTruckNumber(context));
                        IS_TRUCK_TAG_SCANNED = true;
                        getTruckDetails();
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

    private void getTruckDetails() {
        if (cd.isConnectingToInternet()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(APIConstants.K_TRUCK_TAG_ID, CURRENT_EPC);
                fetchTruckDetails(jsonObject, APIConstants.M_TRUCK_DETAILS, "Please wait...\n" + "Getting Truck Details");
            } catch (Exception ex) {

            }
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    public void fetchTruckDetails(JSONObject request, String METHOD_NAME, String progress_message) {
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
                                Log.e("TRUCKDETAILS", response.toString());
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
                    if(DRN.contains(",")){
                        String[] drnArray = DRN.split(",");
                        for (String drn : drnArray) {
                            DRNList.add(drn);
                        }
                    } else{
                        DRNList.add(DRN);
                    }
                    //binding.textDRN.setText(DRN);
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
                TRUCK_TAG_ID = "";
                CURRENT_EPC = "";
                IS_SCANNING_LOCKED = false;
                binding.edtTruckID.setText("");
                binding.textTruckNumber.setText("");
                binding.searchableTextView.setText(default_source_item);
                binding.textLocationName.setText("");
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
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }
}