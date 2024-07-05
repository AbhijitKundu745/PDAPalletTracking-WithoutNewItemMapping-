package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AutoCompleteBinSpinnerAdapter;
import com.psl.pallettracking.adapters.AutoCompleteSourceBinSpinnerAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationPickedProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessModel;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityBinPartialPalletMapProcessBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;
import com.seuic.uhf.EPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;
import okhttp3.OkHttpClient;

public class BinPartialPalletMapProcessActivity extends AppCompatActivity {

    private ActivityBinPartialPalletMapProcessBinding binding;
    BinPartialPalletMappingCreationProcessAdapter adapter;
    BinPartialPalletMappingCreationPickedProcessAdapter pickedAdapter;
    private List<BinPartialPalletMappingCreationProcessModel> orderList;
    private List<BinPartialPalletMappingCreationProcessModel> pickedOrderList;
    private List<BinPartialPalletMappingCreationProcessModel> filteredList = new ArrayList<>();
    private Context context = this;
    String workOrderNumber = "";
    String workOrderType = "";
    String DRNNo = "";
    String SELECTED_BIN = "";
    String PALLET_TAG_ID = "";
    String LOCATION_TAG_ID = "";
    String SCANNED_EPC = "";
    String qty = "";
    public AutoCompleteBinSpinnerAdapter binSpinnerAdapter;
    AutoCompleteSourceBinSpinnerAdapter binSourceAdapter;
    ArrayList<String> binList = new ArrayList<>();

    private boolean PALLET_TAG_SCANNED = false;
    private boolean BIN_TAG_SCANNED = false;
    private BinPartialPalletMappingCreationProcessModel selectedSourceBinObject;
    List<BinPartialPalletMappingCreationProcessModel> binObjectListForSourceSpinner = new ArrayList<>();

    private SeuicGlobalRfidHandler rfidHandler;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bin_partial_pallet_map_process);
        setTitle("USER LOGIN");
        getSupportActionBar().hide();

        db = new DatabaseHandler(context);
        // setContentView(R.layout.activity_bin_partial_pallet_map_process);
        SharedPreferencesManager.setPower(context,10);
        workOrderNumber = getIntent().getStringExtra("WorkOrderNumber");
        workOrderType = getIntent().getStringExtra("WorkOrderType");
        DRNNo = getIntent().getStringExtra("DRN");
        binding.textDCNo.setText(DRNNo);

        getWorkOrderItemDetails(workOrderNumber, workOrderType);
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString().toLowerCase(Locale.getDefault());
                filteredList.clear(); // Clear the filtered list before filtering again
                if (searchText.length() == 0) {
                    // If search query is empty, show all items
                    filteredList.addAll(orderList);
                } else {
                    // Filter items based on search query
                    for (BinPartialPalletMappingCreationProcessModel item : orderList) {
                        if (item.getBinDescription().toLowerCase(Locale.getDefault()).contains(searchText)) {
                            filteredList.add(item);
                        }
                    }
                }
                adapter.filterList(filteredList);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.edtSearch.setText("");
            }
        });

        binding.textPickQty.setSelected(true);
        setAllVisibility();
        pickedOrderList = new ArrayList<>();

        binding.rvPicked.setLayoutManager(new GridLayoutManager(context, 1));
        pickedAdapter = new BinPartialPalletMappingCreationPickedProcessAdapter(context, pickedOrderList);
        binding.rvPicked.setAdapter(pickedAdapter);
        pickedAdapter.notifyDataSetChanged();


        binding.rvPallet.setLayoutManager(new GridLayoutManager(context, 1));
        if (orderList != null) {
            orderList.clear();
        }
        orderList = new ArrayList<>();
        adapter = new BinPartialPalletMappingCreationProcessAdapter(context, orderList);
        binding.rvPallet.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        adapter.setOnItemClickListener(new BinPartialPalletMappingCreationProcessAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(PALLET_TAG_SCANNED) {
                    // Handle item click here
                    String searchText = binding.edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                    if (searchText.length()==0) {
                        BinPartialPalletMappingCreationProcessModel clickedItem = orderList.get(position);
                        int originalPosition = orderList.indexOf(clickedItem); // Find original position in unfiltered list
                        if (originalPosition != -1) {
                            // Perform actions based on the clicked item from the unfiltered list
                            binding.textScanBin.setText(clickedItem.getBinNumber());
                            binding.textItemDesc1.setText(clickedItem.getBinDescription());
                            binding.edtPickedQty.setText("" + clickedItem.getPickedQty());
                            selectedSourceBinObject = orderList.get(originalPosition);
                        }

                    } else
                    {
                        BinPartialPalletMappingCreationProcessModel clickedItemFilter = filteredList.get(position);// Use filtered list
                        int originalPosition1 = filteredList.indexOf(clickedItemFilter); // Find original position in unfiltered list
                        if (originalPosition1 != -1) {
                            // Perform actions based on the clicked item from the unfiltered list
                            binding.textScanBin.setText(clickedItemFilter.getBinNumber());
                            binding.textItemDesc1.setText(clickedItemFilter.getBinDescription());
                            binding.edtPickedQty.setText("" + clickedItemFilter.getPickedQty());
                            selectedSourceBinObject = filteredList.get(originalPosition1); // Use original position
                        } else {
                            // Item not found in original list
                            // Handle this case if needed
                        }


                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan pallet tag");
                }
            }
        });
        binding.edtPickedQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                qty = binding.edtPickedQty.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                qty = binding.edtPickedQty.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                qty = binding.edtPickedQty.getText().toString();
            }
        });

        binding.spBin.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position, long id) {
                if (position == 0) {
                    SELECTED_BIN = "";
                    BIN_TAG_SCANNED = false;
                    binding.textScanBin.setText("Scan Bin");
                } else {
                    SELECTED_BIN = binding.spBin.getSelectedItem().toString();
                    //TODO call here API to get BIN Details FROM Server
                    BIN_TAG_SCANNED = true;
                    binding.textScanBin.setText(SELECTED_BIN);
                    //getBinDetails(SELECTED_BIN);
                }
            }

            @Override
            public void onNothingSelected() {
                SELECTED_BIN = "";
                binding.textScanBin.setText(SELECTED_BIN);
            }
        });

        binding.textEnlargeMainItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = binding.textEnlargeMainItems.getText().toString().trim();
                if (text.equals("+")) {
                    binding.textEnlargeMainItems.setText("-");
                    binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_red));
                    binding.llPicked.setVisibility(View.GONE);
                    binding.llSelectBin.setVisibility(View.GONE);
                    binding.llSourceBin.setVisibility(View.GONE);
                    binding.llButtons.setVisibility(View.GONE);
                }
                if (text.equals("-")) {
                    binding.textEnlargeMainItems.setText("+");
                    binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_green));
                    binding.llPicked.setVisibility(View.VISIBLE);
                    binding.llSelectBin.setVisibility(View.VISIBLE);
                    binding.llSourceBin.setVisibility(View.VISIBLE);
                    binding.llButtons.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.textEnlargePickedItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = binding.textEnlargePickedItems.getText().toString().trim();
                if (text.equals("+")) {
                    binding.textEnlargePickedItems.setText("-");
                    binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_red));
                    binding.llMain.setVisibility(View.GONE);
                    binding.llSelectBin.setVisibility(View.GONE);
                    binding.llSourceBin.setVisibility(View.GONE);
                    binding.llButtons.setVisibility(View.GONE);
                }
                if (text.equals("-")) {
                    binding.textEnlargePickedItems.setText("+");
                    binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_green));
                    binding.llMain.setVisibility(View.VISIBLE);
                    binding.llSelectBin.setVisibility(View.VISIBLE);
                    binding.llSourceBin.setVisibility(View.VISIBLE);
                    binding.llButtons.setVisibility(View.VISIBLE);
                }
            }
        });


        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomConfirmationDialog("Are you sure you want to clear data","CLEAR");
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomConfirmationDialog("Are you sure you want to go back","BACK");
            }
        });
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSourceBinObject != null) {
                    //String qty = binding.edtPickedQty.getText().toString();
                    if (qty.equals("")) {
                        //please add qty
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please add item quantity");
                    } else {
                        int prevQty = Integer.parseInt(qty);
                        int TotalQty = 0;
                        for (BinPartialPalletMappingCreationProcessModel item : pickedOrderList) {
                            TotalQty += item.getPickedQty();
                        }
                        TotalQty += prevQty;
                        int finalTotalQty = TotalQty;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.textTotalQty.setText(""+ finalTotalQty);
                            }
                        });
                        Log.e("LIST", "UPDATED:");
                        BinPartialPalletMappingCreationProcessModel obj = new BinPartialPalletMappingCreationProcessModel();
                        obj.setPickedQty(Integer.parseInt(qty));
                        obj.setBinDescription(selectedSourceBinObject.getBinDescription());
                        obj.setBinNumber(selectedSourceBinObject.getBinNumber());
                        obj.setBatchId(selectedSourceBinObject.getBatchId());

                       // if (!AssetUtils.isItemAlreadyAdded(selectedSourceBinObject.getBinDescription(), pickedOrderList)) {
                            BinPartialPalletMappingCreationProcessModel itemObj = AssetUtils.getItemObject(selectedSourceBinObject.getBinDescription(), selectedSourceBinObject.getBinNumber(), orderList);
                            if(itemObj!=null){
                                if(itemObj.getPickedQty() >= Integer.parseInt(qty)){
                                    pickedOrderList.add(obj);
                                    orderList.remove(itemObj);
                                    int originalQty = itemObj.getPickedQty();
                                    int diff = originalQty-Integer.parseInt(qty);
                                    itemObj.setPickedQty(diff);
                                    orderList.add(itemObj);

                                    adapter.notifyDataSetChanged();
                                    binding.spBin.setSelectedItem(0);
                                    //binding.spSourceBin.setSelection(0);
                                    binding.textScanBin.setText("Bin Name");
                                }else{
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Item Picking Qty cannot be larger than original qty");
                                }

                            }else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "Invalid Item.");
                            }

//                        } else {
//                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Item already added");
//                        }
                        pickedAdapter.notifyDataSetChanged();
                        selectedSourceBinObject = null;
                        //binding.spSourceBin.setSelection(0);
                        binding.edtPickedQty.setText("");
                        binding.textItemDesc1.setText("");


                    }
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Source Bin Details not selected");
                }
            }
        });
        binding.btnPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetUtils.openPowerSettingDialog(context, rfidHandler);
            }
        });
        binding.btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SCANNED_EPC = "";
                startInventory();
                new Handler().postDelayed(() -> {
                    hideProgressDialog();
                    stopInventory();
                    stopInventoryAndDoValidations();
                }, 2000);
            }
        });
        initUHF();
    }

    private void initUHF() {
        AssetUtils.showProgress(context, getResources().getString(R.string.uhf_initialization));
        rfidHandler = new SeuicGlobalRfidHandler();
        rfidHandler.onCreate(context, new RFIDInterface() {
            @Override
            public void handleTriggerPress(boolean pressed) {
                runOnUiThread(() -> {
                    if (pressed) {

                        SCANNED_EPC = "";
                        startInventory();
                        new Handler().postDelayed(() -> {
                            hideProgressDialog();
                            stopInventory();
                            stopInventoryAndDoValidations();
                        }, 2000);


                    }
                });
            }

            @Override
            public void RFIDInitializationStatus(boolean status) {
                runOnUiThread(() -> {
                    hideProgressDialog();
                    if (status) {
                        //getWorkOrderItemDetails(workOrderNumber, workOrderType);
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "RFID initialization failed");
                        finish();
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
                    if (rfifList != null && rfifList.size() > 0) {
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
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            if (!epcs.contains(epc)) {
                                epcs.add(epc);
                            }
                        }
                        // Show the EPC with the highest RSSI value
                        if (maxRssiEpc != null) {
                            Log.e("Max RSSI EPC", maxRssiEpc);
                            // Update your UI with the EPC with the highest RSSI value
                            SCANNED_EPC = maxRssiEpc;
                        }//changed
                    }
                });
            }
        });
    }

    private List<String> epcs = new ArrayList<>();

    public void stopInventoryAndDoValidations() {
        hideProgressDialog();
        adapter.notifyDataSetChanged();
        // if (epcs.size() == 1) {//changed
        hideProgressDialog();
        try {
            if (SCANNED_EPC != null) {
                if (!SCANNED_EPC.isEmpty()) {
                    if (SCANNED_EPC.length() >= 24) {

                        SCANNED_EPC = SCANNED_EPC.substring(0, 24);
                        if (AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_PALLET)
                                || AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_TEMPORARY_STORAGE)) {

                            //TODO
                            if (PALLET_TAG_SCANNED) {
                                if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_PALLET)){
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Pallet Tag Already scanned");
                                }else if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_TEMPORARY_STORAGE)){
                                    //TODO location Tag
                                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Location tag scanned");
                                    if(pickedOrderList.size()>0){
                                        LOCATION_TAG_ID=SCANNED_EPC;
                                        checkForValidationsAndComplete();
                                    }
                                }
                            }

                            if (!PALLET_TAG_SCANNED && !BIN_TAG_SCANNED) {
                                if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_PALLET)){
                                    PALLET_TAG_SCANNED = true;
                                    PALLET_TAG_ID = SCANNED_EPC;
                                    //TODO
                                    String palletname = db.getProductNameByProductTagId(SCANNED_EPC);
                                    binding.textScanPallet.setText(palletname);
                                }else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan pallet tag");
                                }
                            }
                            epcs.clear();
                            SCANNED_EPC = "";
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                        }
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                    }
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
                }
            } else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
            }
        } catch (Exception e) {

            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
        }
    }

    private void checkForValidationsAndComplete() {
        if(!PALLET_TAG_SCANNED){
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Scan Pallet tag");
        }else if(pickedOrderList.size()<=0){
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Pick items");
        }else{
            showCustomConfirmationDialog("Are you sure you want to upload pallet items","UPLOAD");
        }
    }

    Dialog customConfirmationDialog;

    public void showCustomConfirmationDialog(String msg, final String action) {
        if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
            customConfirmationDialog.dismiss();
        }
        customConfirmationDialog = new Dialog(context);
        if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
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
                if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
                    customConfirmationDialog.dismiss();
                }
                if (action.equals("UPLOAD")) {
                    //uploadInventoryToOffline();
                    AssetUtils.dismissDialog();
                    uploadInventoryToServer();

                }
                else if (action.equals("CLEAR")) {
                    clearAll();
                }
                else if (action.equals("BACK")) {
                    finish();
                }
            }
        });
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
                    customConfirmationDialog.dismiss();
                }
            }
        });
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }

    private void uploadInventoryToServer() {
        try {
            showProgress(context, "Please wait...\nUploading in progress");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put("CustomerID", SharedPreferencesManager.getCustomerId(context));
            jsonObject.put("TransactionDateTime", AssetUtils.getSystemDateTimeInFormatt());
            jsonObject.put("PalletTagID", PALLET_TAG_ID);
            jsonObject.put("PalletName", db.getProductNameByProductTagId(PALLET_TAG_ID));
            jsonObject.put("LocationTagID", LOCATION_TAG_ID);
            jsonObject.put("LocationCategoryID", "04");
            jsonObject.put("WorkorderNumber", workOrderNumber);
            jsonObject.put("WorkorderType", workOrderType);
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<pickedOrderList.size();i++){
                JSONObject dataObject = new JSONObject();
                BinPartialPalletMappingCreationProcessModel obj = pickedOrderList.get(i);
                dataObject.put("BinName",obj.getBinNumber());
                if(obj.getBatchId().equalsIgnoreCase(null)){
                    dataObject.put("BatchID",null);
                } else{
                    dataObject.put("BatchID",obj.getBatchId());
                }
                dataObject.put("ItemDescription",obj.getBinDescription());
                dataObject.put("Qty",obj.getPickedQty());
                jsonArray.put(dataObject);
            }
            jsonObject.put("ItemDetails",jsonArray);
            Log.e("JSONREQL0", jsonObject.toString());

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_UPLOAD_PARTIAL_WORK_ORDERS_DETAILS).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response", response.toString());
                            hideProgressDialog();
                            if (response != null) {
                                try {
                                    if (response.getBoolean("status")) {
                                        //AssetUtils.showCommonBottomSheetSuccessDialog(context,"Work Order Details Uploaded Successfully");
                                        clearAll();
                                        if (customConfirmationDialog != null&& customConfirmationDialog.isShowing()) {
                                            customConfirmationDialog.dismiss();
                                        }
                                        finish();
//                                        Intent intent = new Intent(BinPartialPalletMapProcessActivity.this, BinPartialPalletMappingActivity.class);
//                                        startActivity(intent);
                                    } else {
                                        String message = response.getString("message");
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                    }
                                } catch (JSONException e) {
                                    hideProgressDialog();
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                                }
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            hideProgressDialog();
                            // if (BuildConfig.DEBUG) {
                            // do something for a debug build
                            String orderDetailsString = AssetUtils.getJsonFromAssets(context, "getWorkOrderDetails.json");
                            try {
                                JSONObject response = new JSONObject(orderDetailsString);
                                if (response.getBoolean("status")) {
                                    JSONArray dataArray = response.getJSONArray("data");
                                    parseWorkDetailsObjectAndDoAction(dataArray);
                                } else {
                                    String message = response.getString("message");
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            // }else {

                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            }
                            //}
                        }
                    });
        } catch (JSONException e) {
            hideProgressDialog();
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    private void startInventory() {
        if (epcs != null) {
            epcs.clear();
        }
        SCANNED_EPC = "";
        showProgress(context, "Please wait...Scanning Rfid Tag");
        setFilterandStartInventory();
    }

    private void stopInventory() {
        rfidHandler.stopInventory();
        adapter.notifyDataSetChanged();
    }

    private void setFilterandStartInventory() {
        int rfpower = SharedPreferencesManager.getPower(context);
        rfidHandler.setRFPower(rfpower);
        rfidHandler.startInventory();
    }

    private void clearAll() {
        PALLET_TAG_SCANNED = false;
        BIN_TAG_SCANNED = false;
        binding.spBin.setSelectedItem(0);
        binding.textScanBin.setText("Bin Name");
        if (pickedOrderList != null) {
            pickedOrderList.clear();
        }
        pickedAdapter.notifyDataSetChanged();
//        if (binObjectListForSourceSpinner != null) {
//            binObjectListForSourceSpinner.clear();
//        }
        //binSourceAdapter.notifyDataSetChanged();//changed

        selectedSourceBinObject = null;
        binding.edtPickedQty.setText("");
        binding.textTotalQty.setText("");
        binding.textScanPallet.setText("Scan Pallet");
        PALLET_TAG_ID = "";
        LOCATION_TAG_ID = "";
        SELECTED_BIN = "";
        SCANNED_EPC = "";
        if (epcs != null) {
            epcs.clear();
        }
    }

    private void notifyBinSpinnerAdapter() {
        binding.spBin.setEnabled(false);
        binSpinnerAdapter = new AutoCompleteBinSpinnerAdapter(context, binList);
        binding.spBin.setAdapter(binSpinnerAdapter);
        binding.spBin.setEnabled(true);

    }

    private void setAllVisibility() {
        binding.textEnlargePickedItems.setText("+");
        binding.textEnlargeMainItems.setText("+");
        binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.llPicked.setVisibility(View.VISIBLE);
        binding.llMain.setVisibility(View.VISIBLE);
        binding.llSelectBin.setVisibility(View.VISIBLE);
        binding.llSourceBin.setVisibility(View.VISIBLE);
        binding.llButtons.setVisibility(View.VISIBLE);
    }

    private void getWorkOrderItemDetails(String workOrderNumber, String workOrderType) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put("WorkorderNumber", workOrderNumber);
            jsonObject.put("WorkorderType", workOrderType);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_PARTIAL_WORK_ORDERS_DETAILS).addJSONObjectBody(jsonObject)
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
                                        parseWorkDetailsObjectAndDoAction(dataArray);
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
                            // if (BuildConfig.DEBUG) {
                            // do something for a debug build
                           /* String orderDetailsString = AssetUtils.getJsonFromAssets(context, "getWorkOrderDetails.json");
                            try {
                                JSONObject response = new JSONObject(orderDetailsString);
                                if (response.getBoolean("status")) {
                                    JSONArray dataArray = response.getJSONArray("data");
                                    parseWorkDetailsObjectAndDoAction(dataArray);
                                } else {
                                    String message = response.getString("message");
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }*/
                            // }else {

                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            }
                            //}
                        }
                    });
        } catch (JSONException e) {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    private void parseWorkDetailsObjectAndDoAction(JSONArray dataArray) {
        if (orderList != null) {
            orderList.clear();
        }
        if (binObjectListForSourceSpinner != null) {
            binObjectListForSourceSpinner.clear();
        }//changed
        if (dataArray.length() > 0) {
            try {
                if (binList != null) {
                    binList.clear();
                }
                for (int i = 0; i < dataArray.length(); i++) {

                    BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String itemDesc = dataObject.getString("ItemDescription");
                    String binName = dataObject.getString("BinName");
                    String batchID = dataObject.getString("BatchID");
                    int pickUpQty = dataObject.getInt("PickUpQty");
                    binList.add(binName);
                    binPartialPalletMappingCreationProcessModel.setBinDescription(itemDesc);
                    binPartialPalletMappingCreationProcessModel.setBinNumber(binName);
                    binPartialPalletMappingCreationProcessModel.setBatchId(batchID);//changed
                    binPartialPalletMappingCreationProcessModel.setPickedQty(pickUpQty);
                    orderList.add(binPartialPalletMappingCreationProcessModel);
                    binObjectListForSourceSpinner.add(binPartialPalletMappingCreationProcessModel);


                }
                if (binObjectListForSourceSpinner != null) {
                    if (binObjectListForSourceSpinner.size() > 0) {
                        BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                        binPartialPalletMappingCreationProcessModel.setBinDescription("Select Item");
                        binPartialPalletMappingCreationProcessModel.setBinNumber("");
                        binPartialPalletMappingCreationProcessModel.setPickedQty(0);
                        binPartialPalletMappingCreationProcessModel.setBatchId("BatchId");
                        binObjectListForSourceSpinner.add(0, binPartialPalletMappingCreationProcessModel);
                    }
                }//changed
            } catch (JSONException e) {
                adapter.notifyDataSetChanged();
                //binSpinnerAdapter.notifyDataSetChanged();
                //notifyBinSpinnerAdapter();
                hideProgressDialog();
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
            }
        }
        binSourceAdapter = new AutoCompleteSourceBinSpinnerAdapter(context, binObjectListForSourceSpinner);//changed
        //binding.spSourceBin.setAdapter(binSourceAdapter);//changed
        binSourceAdapter.notifyDataSetChanged();//changed
        adapter.notifyDataSetChanged();
        //binSpinnerAdapter.notifyDataSetChanged();
        notifyBinSpinnerAdapter();
    }

    private void parseBinAndDoAction(JSONArray dataArray) {
        if (binObjectListForSourceSpinner != null) {
            binObjectListForSourceSpinner.clear();
        }
        if (dataArray.length() > 0) {
            try {

                for (int i = 0; i < dataArray.length(); i++) {

                    BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String itemDesc = dataObject.getString("ItemDescription");
                    String binName = dataObject.getString("BinName");
                    int pickUpQty = dataObject.getInt("Qty");
                    String batchId = dataObject.getString("BatchID");

                    binPartialPalletMappingCreationProcessModel.setBinDescription(itemDesc);
                    binPartialPalletMappingCreationProcessModel.setBinNumber(binName);
                    binPartialPalletMappingCreationProcessModel.setPickedQty(pickUpQty);
                    binPartialPalletMappingCreationProcessModel.setBatchId(batchId);
                    binObjectListForSourceSpinner.add(binPartialPalletMappingCreationProcessModel);
                }
                if (binObjectListForSourceSpinner != null) {
                    if (binObjectListForSourceSpinner.size() > 0) {
                        BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                        binPartialPalletMappingCreationProcessModel.setBinDescription("Select Item");
                        binPartialPalletMappingCreationProcessModel.setBinNumber("");
                        binPartialPalletMappingCreationProcessModel.setPickedQty(0);
                        binPartialPalletMappingCreationProcessModel.setBatchId("BatchId");
                        binObjectListForSourceSpinner.add(0, binPartialPalletMappingCreationProcessModel);
                    }
                }
            } catch (JSONException e) {
                hideProgressDialog();
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
            }
        }
        binSourceAdapter = new AutoCompleteSourceBinSpinnerAdapter(context, binObjectListForSourceSpinner);
        //binding.spSourceBin.setAdapter(binSourceAdapter);
        binSourceAdapter.notifyDataSetChanged();
    }

    private void getBinDetails(String binNumber) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("BinName", binNumber);
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            showProgress(context, "Please wait...\nGetting Bin Details");
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_BIN_DETAILS).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response", response.toString());
                            hideProgressDialog();
                            if (response != null) {
                                try {
                                    if (response.getBoolean("status")) {
                                        JSONArray dataArray = response.getJSONArray("data");
                                        parseBinAndDoAction(dataArray);
                                    } else {
                                        String message = response.getString("message");
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                    }
                                } catch (JSONException e) {
                                    hideProgressDialog();
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                                }
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            hideProgressDialog();
                            // if (BuildConfig.DEBUG) {
                            // do something for a debug build
                            String orderDetailsString = AssetUtils.getJsonFromAssets(context, "getBinDetails.json");
                            try {
                                JSONObject response = new JSONObject(orderDetailsString);
                                if (response.getBoolean("status")) {
                                    JSONArray dataArray = response.getJSONArray("data");
                                    parseBinAndDoAction(dataArray);
                                } else {
                                    String message = response.getString("message");
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            // }else {

                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                            }
                            //}
                        }
                    });
        } catch (Exception e) {
            hideProgressDialog();
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            rfidHandler.onResume();
        } catch (Exception e) {
            Log.e("onresumesxc", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (epcs != null) {
            epcs.clear();

        }
        rfidHandler.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {

            rfidHandler.onPause();
        if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
            customConfirmationDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Are you sure you want to go back","BACK");
        //super.onBackPressed();
    }
}