package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AutoCompleteSourceBinSpinnerAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationPickedProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessModel;
import com.psl.pallettracking.adapters.DispatchPalletCreationAdapter;
import com.psl.pallettracking.adapters.DispatchPalletPickedAdapter;
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityDispatchPalletCreationBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.AuthorizationWMS;
import com.psl.pallettracking.helper.PickListBin;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.helper.StringUtils;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.psl.pallettracking.viewHolder.ItemDetailsList;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.uhf.EPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DispatchPalletCreationActivity extends AppCompatActivity implements DecodeInfoCallBack {
    private Context context = this;
    ActivityDispatchPalletCreationBinding binding;
    private List<ItemDetailsList> orderList, originalOrderList;
    private List<ItemDetailsList> pickedOrderList, pickedAdapterList;
    private List<ItemDetailsList> filteredList = new ArrayList<>();
    DispatchPalletCreationAdapter adapter;
    DispatchPalletPickedAdapter pickedAdapter;
    String workOrderNumber = "";
    String workOrderType = "";
    String DRNNo = "";
    String PALLET_TAG_ID = "";
    String LOCATION_TAG_ID = "1404000186A150534C202020";
    String LOCATION_NAME = "R100";
    String BIN_TAG_ID = "";
    String BIN_NAME = "";
    String DC_TAG_ID = "";
    String SCANNED_EPC = "";
    String qty = "";
    double scannedQty = 0.0;
    private boolean PALLET_TAG_SCANNED = false;
    private boolean DC_TAG_SCANNED = false;
    private boolean BIN_TAG_SCANNED = false;
    private ItemDetailsList selectedItemObject = new ItemDetailsList();
    private SeuicGlobalRfidHandler rfidHandler;
    private DatabaseHandler db;
    Scanner scanner;
    boolean IS_QR_CODE_SCANNED = false;
    private List<ItemDetailsList> tempScannedItems = new ArrayList<>();
    ArrayList<String> barcodes = new ArrayList<>();
     boolean IS_SCANNED = false;
     boolean IS_SELECTED = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dispatch_pallet_creation);
        setTitle("DISPATCH PALLET CREATION");
        getSupportActionBar().hide();
        db = new DatabaseHandler(context);
        // setContentView(R.layout.activity_bin_partial_pallet_map_process);
        SharedPreferencesManager.setPower(context,10);
        workOrderNumber = getIntent().getStringExtra("WorkOrderNumber");
        workOrderType = getIntent().getStringExtra("WorkOrderType");
        DRNNo = getIntent().getStringExtra("DRN");
        binding.textDCNo.setText(DRNNo);
        AssetUtils.getTimer(new AssetUtils.TimerCallback() {
            @Override
            public void onTimerUpdate(String timerString) {
                //Log.d("Timer", "Timer string: " + timerString);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.timer.setText(timerString);
                    }
                });
            }
        });
        setAllVisibility();
        getWorkOrderItemDetails(workOrderNumber, workOrderType);
        binding.rvOrderList.setLayoutManager(new GridLayoutManager(context, 1));
        binding.rvPickedOrder.setLayoutManager(new GridLayoutManager(context, 1));
        if (orderList != null) {
            orderList.clear();
        }
        if (pickedOrderList != null) {
            pickedOrderList.clear();
        }
        if (originalOrderList != null) {
            originalOrderList.clear();
        }
        orderList = new ArrayList<>();
        pickedOrderList = new ArrayList<>();
        pickedAdapterList = new ArrayList<>();
        originalOrderList = new ArrayList<>();
        adapter = new DispatchPalletCreationAdapter(context, orderList);
        adapter.notifyDataSetChanged();
        binding.rvOrderList.setAdapter(adapter);
        pickedAdapter = new DispatchPalletPickedAdapter(context, pickedAdapterList);
        pickedAdapter.notifyDataSetChanged();
        binding.rvPickedOrder.setAdapter(pickedAdapter);
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
       binding.textScanBin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Convert the text to uppercase
                // Update BIN_NAME directly from the EditText
                BIN_NAME = s.toString(); // Assuming the user only inputs uppercase text
                Log.e("BINNAME", BIN_NAME);

                // Set BIN_TAG_SCANNED based on whether BIN_NAME is empty or not
                BIN_TAG_SCANNED = !BIN_NAME.isEmpty();
                Log.e("BINNAMESCA", String.valueOf(BIN_TAG_SCANNED));
            }
        });

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
                    for (ItemDetailsList item : orderList) {
                        if (item.getItemDesc().toLowerCase(Locale.getDefault()).contains(searchText)) {
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
        adapter.setOnItemClickListener(new DispatchPalletCreationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(BIN_TAG_SCANNED) {
                    // Handle item click here
                    String searchText = binding.edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                    if (searchText.length()==0) {
                        ItemDetailsList clickedItem = orderList.get(position);
                        int originalPosition = orderList.indexOf(clickedItem); // Find original position in unfiltered list
                        if (originalPosition != -1) {
                            if(!clickedItem.getPickedQty().equalsIgnoreCase("0.0")){
                                binding.textItemDesc.setText(clickedItem.getItemDesc());
                                binding.edtPickedQty.setText(clickedItem.getPickedQty());
                                binding.textBatchID.setText(clickedItem.getBatchID());
                                selectedItemObject = orderList.get(originalPosition);
                            } else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "This item has already been picked.");
                            }

                        }

                    } else
                    {
                        ItemDetailsList clickedItemFilter = filteredList.get(position);// Use filtered list
                        int originalPosition1 = filteredList.indexOf(clickedItemFilter); // Find original position in unfiltered list
                        if (originalPosition1 != -1) {
                            if(!clickedItemFilter.getPickedQty().equalsIgnoreCase("0.0")){
                                binding.textItemDesc.setText(clickedItemFilter.getItemDesc());
                                binding.edtPickedQty.setText(clickedItemFilter.getPickedQty());
                                binding.textBatchID.setText(clickedItemFilter.getBatchID());
                                selectedItemObject = filteredList.get(originalPosition1); // Use original position
                            }
                            else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "This item has already been picked.");
                            }

                        } else {
                            // Item not found in original list
                            // Handle this case if needed
                        }

                    }
                    //tempScannedItems.add(selectedItemObject);
                    IS_SELECTED = true;
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a Bin");
                }
            }
        });
        binding.btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.textScanBin.setText("");
                binding.textItemDesc.setText("");
                binding.edtPickedQty.setText("");
                binding.textBatchID.setText("");
                tempScannedItems.clear();
                selectedItemObject = null;
                barcodes.clear();
                scannedQty = 0.0;
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
                if (tempScannedItems != null || selectedItemObject != null) {
                    BIN_NAME = binding.textScanBin.getText().toString();
                    qty = binding.edtPickedQty.getText().toString();
                    if (qty.equals("")) {
                        //please add qty
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please add item quantity");
                    } else if (!qty.equalsIgnoreCase("0.0")) {
                        if(IS_SCANNED){
                            for(ItemDetailsList obj1: tempScannedItems){
                                ItemDetailsList scannedItem = new ItemDetailsList();
                                scannedItem.setSerialNo(obj1.getSerialNo());
                                scannedItem.setItemDesc(obj1.getItemDesc());
                                scannedItem.setBatchID(obj1.getBatchID());
                                scannedItem.setSkuCode(obj1.getSkuCode());
                                scannedItem.setBinName(BIN_NAME);
                                scannedItem.setScannedQty(obj1.getScannedQty());
                                Log.e("temp1", obj1.getItemDesc());
                                Log.e("temp1", obj1.getBinName());
                                Log.e("temp1", obj1.getBatchID());
                                Log.e("temp1", obj1.getScannedQty());
                                pickedOrderList.add(scannedItem);
                                for (ItemDetailsList sam: pickedOrderList){
                                    Log.e("Pick1", sam.getItemDesc());
                                    Log.e("Pick1", sam.getBinName());
                                    Log.e("Pick1", sam.getBatchID());
                                    Log.e("Pick1", sam.getScannedQty());
                                }
                            }
                        } else if(IS_SELECTED){
                            ItemDetailsList selectedItem = new ItemDetailsList();
                            selectedItem.setItemDesc(selectedItemObject.getItemDesc());
                            selectedItem.setBinName(BIN_NAME);
                            selectedItem.setSerialNo(selectedItemObject.getSerialNo());
                            selectedItem.setSkuCode(selectedItemObject.getSkuCode());
                            selectedItem.setScannedQty(qty);
                            selectedItem.setBatchID(selectedItemObject.getBatchID());
                            pickedOrderList.add(selectedItem);
                            for (ItemDetailsList sam: pickedOrderList){
                                Log.e("Pick2", sam.getItemDesc());
                                Log.e("Pick2", sam.getBinName());
                                Log.e("Pick2", sam.getBatchID());
                                Log.e("Pick2", sam.getScannedQty());
                            }
                        }

                        ItemDetailsList obj = new ItemDetailsList();
                        obj.setPickedQty(qty);
                        obj.setItemDesc(selectedItemObject.getItemDesc());
                        obj.setBinName(BIN_NAME);
                        obj.setSerialNo(selectedItemObject.getSerialNo());
                        obj.setSkuCode(selectedItemObject.getSkuCode());
                        obj.setScannedQty(qty);
                        obj.setBatchID(selectedItemObject.getBatchID());
                        pickedAdapterList.add(obj);
                    pickedAdapter.notifyDataSetChanged();
                    double TotalQty = Double.parseDouble(binding.textTotalQty.getText().toString()) + Double.parseDouble(qty);
                    binding.textTotalQty.setText(String.valueOf(TotalQty));
                            // Update the PickedQty in orderList
                        for (ItemDetailsList orderItem : orderList) {
                            if (orderItem.getSerialNo().equals(selectedItemObject.getSerialNo())) {
                                // Update the PickedQty
                                double diffQty = Double.parseDouble(orderItem.getPickedQty()) - Double.parseDouble(qty);
                                orderItem.setPickedQty(String.valueOf(diffQty));
                                orderItem.setScannedQty(String.valueOf(Double.parseDouble(qty)));
                                adapter.notifyItemChanged(orderList.indexOf(orderItem));
                                adapter.notifyItemChanged(filteredList.indexOf(orderItem));
                                break; // Exit the loop once the item is found and updated
                            }
                        }
                        scannedQty = 0.0;
                        BIN_TAG_SCANNED = false;
                        IS_SCANNED = false;
                        IS_SELECTED = false;
                    selectedItemObject = null;
                    tempScannedItems.clear();
                    binding.edtPickedQty.setText("");
                    binding.textItemDesc.setText("");
                    binding.textBatchID.setText("");
                    binding.textScanBin.setText("");
                    } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid quantity");
                    }

                }else
                {
                 AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan an item");
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
//                SCANNED_EPC = "";
//                startInventory();
//                new Handler().postDelayed(() -> {
//                    hideProgressDialog();
//                    stopInventory();
//                    stopInventoryAndDoValidations();
//                }, 2000);
                if(pickedOrderList.size()>0){
                    showCustomConfirmationDialog("Do you want to upload the data?", "UPLOAD");
                }
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
                        if(!BIN_TAG_SCANNED){
                            startInventory();
                            new Handler().postDelayed(() -> {
                                hideProgressDialog();
                                stopInventory();
                                stopInventoryAndDoValidations();
                            }, 2000);
                        } else{
                            startScanning();
                        }

                    }
                });
            }

            @Override
            public void RFIDInitializationStatus(boolean status) {
                runOnUiThread(() -> {
                    hideProgressDialog();
                    if (status) {
                        scanner = ScannerFactory.getScanner(DispatchPalletCreationActivity.this);
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
                    if (rfifList != null && rfifList.size() > 0) {
                        int maxRssi = Integer.MIN_VALUE;
                        String maxRssiEpc = null;
                        for (int i = 0; i < rfifList.size(); i++) {
                            String epc = rfifList.get(i).getId();
                            int rssivalue = rfifList.get(i).rssi;
                            if (rssivalue > maxRssi) {
                                maxRssi = rssivalue;
                                maxRssiEpc = epc;
                            }
                        }
                        if (maxRssiEpc != null) {
                            Log.e("Max RSSI EPC", maxRssiEpc);
                            SCANNED_EPC = maxRssiEpc;
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

    public void stopInventoryAndDoValidations() {
        hideProgressDialog();
        try {
            if (SCANNED_EPC != null) {
                if (!SCANNED_EPC.isEmpty()) {
                    if (SCANNED_EPC.length() >= 24) {
                        SCANNED_EPC = SCANNED_EPC.substring(0, 24);
                        String companycode = SCANNED_EPC.substring(0, 2);
                        String companycode1 = AssetUtils.hexToNumber(companycode);
                        if (companycode.equalsIgnoreCase(SharedPreferencesManager.getCompanyCode(context))) {
                            if (AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_PALLET)) {
                                if(!DC_TAG_SCANNED){
                                    if(db.checkAssetNameByProductTagId(SCANNED_EPC)){
                                        DC_TAG_SCANNED = true;
                                        DC_TAG_ID = SCANNED_EPC;
                                        String DC = db.getProductNameByProductTagId(DC_TAG_ID);
                                        binding.textScanDC.setText(DC);
                                    }
                                    else{
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a dc tag");
                                    }
                                } else{
                                    if(!db.checkAssetNameByProductTagId(SCANNED_EPC)){
                                        PALLET_TAG_SCANNED = true;
                                        PALLET_TAG_ID = SCANNED_EPC;
                                        String PalletName = db.getProductNameByProductTagId(PALLET_TAG_ID);
                                        binding.textScanPallet.setText(PalletName);
                                    } else{
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "DC Tag already scanned. Please scan a pallet tag");
                                    }
                                }
                            } else if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_BEAN)){
                                if(DC_TAG_SCANNED){
                                    if(PALLET_TAG_SCANNED){
                                        BIN_TAG_SCANNED =true;
                                        BIN_TAG_ID = SCANNED_EPC;
                                        BIN_NAME = db.getProductNameByProductTagId(BIN_TAG_ID);
                                        binding.textScanBin.setText(BIN_NAME);
                                    }
                                    else{
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a pallet tag");
                                    }
                                }
                                else{
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a DC tag");
                                }
                            } else if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_TEMPORARY_STORAGE)){
                                if(pickedOrderList.size()>0){
                                    showCustomConfirmationDialog("Are you sure you want to upload the items","UPLOAD");
                                }
                                else{
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Pick items");
                                }
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
            } else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.invalid_rfid_error));
            }
        } catch (Exception e) {

            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_rfid_error));
        }
    }

    private void checkForValidationsAndComplete() {
        if(!PALLET_TAG_SCANNED){
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Scan DC tag");
        }else if(pickedOrderList.size()<=0){
            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please Pick items");
        }else{
            showCustomConfirmationDialog("Are you sure you want to upload the items","UPLOAD");
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
                    AssetUtils.stopTimer();

                }
                else if (action.equals("BACK")) {
                    clearAll();
                    AssetUtils.stopTimer();
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
        customConfirmationDialog.show();
    }

    private void uploadInventoryToServer() {
        try {
            showProgress(context, "Please wait...\nUploading in progress");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
            jsonObject.put(APIConstants.K_TRANSACTION_DATE_TIME, AssetUtils.getSystemDateTimeInFormatt());
            jsonObject.put(APIConstants.PALLET_TAG_ID, PALLET_TAG_ID);
            jsonObject.put(APIConstants.CURRENT_PALLET_NAME, db.getProductNameByProductTagId(PALLET_TAG_ID));
            jsonObject.put("LocationTagID", LOCATION_TAG_ID);
            jsonObject.put("LocationCategoryID", "04");
            jsonObject.put("LocationName", LOCATION_NAME);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_NUMBER, workOrderNumber);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_TYPE, workOrderType);
            jsonObject.put(APIConstants.K_DC_NO, DRNNo);
            jsonObject.put("DCTagID", DC_TAG_ID);
            jsonObject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<pickedOrderList.size();i++){
                JSONObject dataObject = new JSONObject();
                ItemDetailsList obj = pickedOrderList.get(i);
                dataObject.put("BinName",obj.getBinName());
                if(obj.getBatchID().equalsIgnoreCase(null)){
                    dataObject.put("BatchID",null);
                } else{
                    dataObject.put("BatchID",obj.getBatchID());
                }
                dataObject.put(APIConstants.K_ITEM_DESCRIPTION,obj.getItemDesc());
                dataObject.put("ItemName",obj.getSkuCode());
                dataObject.put("PickedItemID",obj.getSerialNo());
                dataObject.put("Qty",obj.getScannedQty());
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
                                        AssetUtils.stopTimer();
//                                        Intent intent = new Intent(BinPartialPalletMapProcessActivity.this, BinPartialPalletMappingActivity.class);
//                                        startActivity(intent);
                                        clearAll();
                                        finish();
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
        SCANNED_EPC = "";
        showProgress(context, "Please wait...Scanning Rfid Tag");
        setFilterandStartInventory();
    }

    private void stopInventory() {
        rfidHandler.stopInventory();
        //adapter.notifyDataSetChanged();
    }

    private void setFilterandStartInventory() {
        int rfpower = SharedPreferencesManager.getPower(context);
        rfidHandler.setRFPower(rfpower);
        rfidHandler.startInventory();
    }

    private void clearAll() {
       DC_TAG_ID = "";
       PALLET_TAG_ID = "";
       BIN_TAG_ID = "";
       binding.textItemDesc.setText("");
       binding.textBatchID.setText("");
        binding.textScanBin.setText("");
        if (pickedOrderList != null) {
            pickedOrderList.clear();
        }

        binding.rvOrderList.setAdapter(null);
        orderList.clear();
        for (ItemDetailsList model : originalOrderList) {
            ItemDetailsList newModel = new ItemDetailsList();
            newModel.setSerialNo(model.getSerialNo());
            newModel.setItemDesc(model.getItemDesc());
            newModel.setSkuCode(model.getSkuCode());
            newModel.setBinName(model.getBinName());
            newModel.setBatchID(model.getBatchID());
            newModel.setOriginalPickedQty(model.getOriginalPickedQty());
            newModel.setPickedQty(model.getOriginalPickedQty()); // restore the original quantity
            orderList.add(newModel);
        }
        adapter = new DispatchPalletCreationAdapter(context, orderList);
        binding.rvOrderList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        selectedItemObject = null;
        binding.edtPickedQty.setText("");
        binding.textTotalQty.setText("0.0");
        binding.textScanPallet.setText("");
        PALLET_TAG_ID = "";
        SCANNED_EPC = "";
        IS_QR_CODE_SCANNED = false;
        BIN_NAME = "";
        qty = "";
        scannedQty = 0.0;
        PALLET_TAG_SCANNED = false;
        DC_TAG_SCANNED = false;
        BIN_TAG_SCANNED = false;
        tempScannedItems.clear();
        barcodes.clear();
        IS_SCANNED = false;
        IS_SELECTED = false;
        binding.textScanDC.setText("");
        if(pickedAdapterList != null){
            pickedAdapterList.clear();
        }
        pickedAdapter.notifyDataSetChanged();
    }
    private void clearSpecData() {
        PALLET_TAG_ID = "";
        BIN_TAG_ID = "";
        binding.textItemDesc.setText("");
        binding.textBatchID.setText("");
        binding.textScanBin.setText("");
        if (pickedOrderList != null) {
            pickedOrderList.clear();
        }
        pickedAdapterList.clear();
        pickedAdapter.notifyDataSetChanged();
        selectedItemObject = null;
        binding.edtPickedQty.setText("");
        binding.textTotalQty.setText("0.0");
        binding.textScanPallet.setText("");
        PALLET_TAG_ID = "";
        SCANNED_EPC = "";
        IS_QR_CODE_SCANNED = false;
        BIN_NAME = "";
        qty = "";
        scannedQty = 0.0;
        PALLET_TAG_SCANNED = false;
        DC_TAG_SCANNED = false;
        BIN_TAG_SCANNED = false;
        tempScannedItems.clear();
        barcodes.clear();
    }

    private void setAllVisibility() {
        binding.textEnlargePickedItems.setText("+");
        binding.textEnlargeMainItems.setText("+");
        binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.llPicked.setVisibility(View.VISIBLE);
        binding.llMain.setVisibility(View.VISIBLE);
        binding.llSourceBin.setVisibility(View.VISIBLE);
        binding.llSelectBin.setVisibility(View.VISIBLE);
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

    private void parseWorkDetailsObjectAndDoAction(JSONArray dataArray) {
        if (orderList != null) {
            orderList.clear();
        }
        if (originalOrderList != null) {
            originalOrderList.clear();
        }
        if (dataArray.length() > 0) {
            try {
                for (int i = 0; i < dataArray.length(); i++) {

                    ItemDetailsList orderItems = new ItemDetailsList();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String itemID = dataObject.getString("PickedItemID");
                    String itemDesc = dataObject.getString("ItemDescription");
                    String itemName = dataObject.getString("ItemName");
                    String binName = dataObject.getString("BinName");
                    String batchID = dataObject.getString("BatchID");
                    String pickUpQty = dataObject.getString("PickUpQty");
                    orderItems.setSerialNo(itemID);
                    orderItems.setItemDesc(itemDesc);
                    orderItems.setSkuCode(itemName);
                    orderItems.setBinName(binName);
                    orderItems.setBatchID(batchID);
                    orderItems.setPickedQty(pickUpQty);
                    orderItems.setOriginalPickedQty(pickUpQty);
                    orderList.add(orderItems);
                    originalOrderList.add(orderItems);
                }

            } catch (JSONException e) {
                adapter.notifyDataSetChanged();
                hideProgressDialog();
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
            }
        }
        adapter.notifyDataSetChanged();
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
        rfidHandler.onDestroy();
        AssetUtils.stopTimer();
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

    @Override
    public void onDecodeComplete(DecodeInfo info) {
        String barcode = info.barcode;
        if (barcode != null && !barcode.isEmpty()) {
            Log.e("Barcode", info.barcode);
            addBarcodeToList(barcode);
        }
    }
    private void addBarcodeToList(String barcode) {
        hideProgressDialog();
        String skuCode = "";
        String batchID = "";
        String[] parts = barcode.split("[,\\s]+");
        if (parts.length > 3 && parts.length < 6) {
//            int index = checkIsBarcodeExist(barcode);
//            if (index == -1) {
                if (barcode.contains(",")) {
                    parts = barcode.split("[,]+");
                    skuCode = parts[1].trim().replaceAll("^0*", "");
                    batchID = parts[3].trim().replaceAll("^0*", "");
                } else if (barcode.contains(" ")) {
                    parts = barcode.split("\\s+");
                    skuCode = parts[2].trim();
                    batchID = parts[4].trim();
                }
            if (!barcodes.contains(barcode)) {
                if(db.isSKUExist(skuCode)){
                    ItemDetailsList item = getItemBySkuCode(skuCode);
                    if (item != null) {
                        Log.e("item", item.getItemDesc());
                        scannedQty = Double.parseDouble(item.getScannedQty());
                        Log.e("ScannedQt", String.valueOf(scannedQty));
                        if (Double.parseDouble(item.getPickedQty()) >= scannedQty ) {
                            String SKUName = db.getItemNameByItemCode(skuCode);
                            IS_QR_CODE_SCANNED = true;

                            ItemDetailsList scannedItem = new ItemDetailsList();
                            scannedItem.setItemDesc(SKUName);
                            scannedItem.setBatchID(batchID);
                            scannedItem.setSkuCode(skuCode);
                            scannedItem.setSerialNo(item.getSerialNo());
                            scannedItem.setScannedQty("1.0"); // Update scanned quantity
                            if(binding.textItemDesc.getText().equals("") || !binding.textItemDesc.getText().equals(SKUName)){
                                binding.textItemDesc.setText(SKUName);
                                binding.textBatchID.setText(batchID);
                                binding.edtPickedQty.setText("1.0");
                                if(barcodes!= null){
                                    barcodes.clear();
                                }
                            } else{
                                binding.textBatchID.setText(batchID);
                                String qty1 = binding.edtPickedQty.getText().toString();
                                Double CurrQty = Double.parseDouble(qty1) + 1.0;
                                binding.edtPickedQty.setText(String.valueOf(CurrQty));
                            }
                            barcodes.add(barcode);
                            scannedQty = scannedQty + 1.0;
                            item.setScannedQty(String.valueOf(scannedQty));
                            selectedItemObject = item;
                            tempScannedItems.add(scannedItem);
                            IS_SCANNED = true;
                            for(ItemDetailsList obj1: tempScannedItems){
                                Log.e("temp2", obj1.getItemDesc());
                                Log.e("temp2", obj1.getBinName());
                                Log.e("temp2", obj1.getBatchID());
                                Log.e("temp2", obj1.getScannedQty());
                                Log.e("temp2", obj1.getSkuCode());
                            }
                        }
                        else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "All expected items are already scanned for this SKU");
                        }

                    }
                    else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "The Scanned sku "+skuCode+" doesn't match with the pick list item");
                    }
                }
                else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, skuCode+" doesn't exist.");
                }
            }
            else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.barcode_already_scanned));
            }



//            }else {
//                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.barcode_already_scanned));
//            }
            adapter.notifyDataSetChanged();
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
    private ItemDetailsList getItemBySkuCode(String skuCode) {
        for (ItemDetailsList item : orderList) {
            if (item.getSkuCode().equals(skuCode) && Double.parseDouble(item.getPickedQty()) > 0.0) {
                return item;
            }
        }
        return null;
    }
}