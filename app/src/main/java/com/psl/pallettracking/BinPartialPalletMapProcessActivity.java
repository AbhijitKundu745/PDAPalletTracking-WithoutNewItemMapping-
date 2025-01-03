package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AutoCompleteSourceBinSpinnerAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationPickedProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessAdapter;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationProcessModel;
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityBinPartialPalletMapProcessBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.AuthorizationWMS;
import com.psl.pallettracking.helper.PickListBin;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.rfid.RFIDInterface;
import com.psl.pallettracking.rfid.SeuicGlobalRfidHandler;
import com.seuic.uhf.EPC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    AutoCompleteSourceBinSpinnerAdapter binSourceAdapter;
    ArrayList<String> binList = new ArrayList<>();

    private boolean PALLET_TAG_SCANNED = false;
    private boolean BIN_TAG_SCANNED = false;
    private BinPartialPalletMappingCreationProcessModel selectedSourceBinObject;
    List<BinPartialPalletMappingCreationProcessModel> binObjectListForSourceSpinner = new ArrayList<>();

    private SeuicGlobalRfidHandler rfidHandler;
    private DatabaseHandler db;
    private List<BinPartialPalletMappingCreationProcessModel> originalOrderList;
    String token = "";
    List<PickListBin > pickedBinDetails = new ArrayList<>();
    Dialog dialog;
    SearchableAdapter searchableAdapter;

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
        getWorkOrderItemDetails(workOrderNumber, workOrderType);
        GetAuthorizationToken();
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
        originalOrderList = new ArrayList<>();
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
                            if(clickedItem.getPickedQty()!=0){
                                // Perform actions based on the clicked item from the unfiltered list
                                binding.textScanBin.setText(clickedItem.getBinNumber());
                                binding.textItemDesc1.setText(clickedItem.getBinDescription());
                                binding.edtPickedQty.setText("" + clickedItem.getPickedQty());
                                selectedSourceBinObject = orderList.get(originalPosition);
                                //GetBinNameForSKU(clickedItem.getItemName());
                                GetBinDetails(clickedItem.getItemName());
                            } else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "This item has already been picked.");
                            }

                        }

                    } else
                    {
                        BinPartialPalletMappingCreationProcessModel clickedItemFilter = filteredList.get(position);// Use filtered list
                        int originalPosition1 = filteredList.indexOf(clickedItemFilter); // Find original position in unfiltered list
                        if (originalPosition1 != -1) {
                            if(clickedItemFilter.getPickedQty()!=0){
                                // Perform actions based on the clicked item from the unfiltered list
                                binding.textScanBin.setText(clickedItemFilter.getBinNumber());
                                binding.textItemDesc1.setText(clickedItemFilter.getBinDescription());
                                binding.edtPickedQty.setText("" + clickedItemFilter.getPickedQty());
                                selectedSourceBinObject = filteredList.get(originalPosition1); // Use original position
                                //GetBinNameForSKU(clickedItemFilter.getItemName());
                                GetBinDetails(clickedItemFilter.getItemName());
                            }
                            else{
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "This item has already been picked.");
                            }

                        } else {
                            // Item not found in original list
                            // Handle this case if needed
                        }
                    }
                } else{
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan DC tag");
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
        binding.spBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.textItemDesc1.getText().equals("")) {
                    // Initialize dialog
                    dialog = new Dialog(context);

                    // set custom dialog
                    dialog.setContentView(R.layout.dialog_searchable_spinner);

                    // set custom height and width
                    dialog.getWindow().setLayout(1000, 800);

                    // set transparent background
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    // show dialog
                    dialog.show();

                    // Initialize and assign variable
                    EditText editText = dialog.findViewById(R.id.edit_text);
                    ListView listView = dialog.findViewById(R.id.list_view);

                    // Initialize array adapter
                    searchableAdapter = new SearchableAdapter(context, binList);

                    // set adapter
                    listView.setAdapter(searchableAdapter);
                    editText.setVisibility(View.GONE);
//                    editText.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//                            searchableAdapter.getFilter().filter(s);
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//
//                        }
//                    });

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            // when item selected from list
                            // set selected item on textView
                            // Dismiss dialog
                            dialog.dismiss();
                            SELECTED_BIN = (String) searchableAdapter.getItem(position);
                            binding.spBin.setText(SELECTED_BIN);
                            if (SELECTED_BIN.equalsIgnoreCase("Select Bin") || SELECTED_BIN.equalsIgnoreCase("")) {
                                SELECTED_BIN = "";
                                BIN_TAG_SCANNED = false;

                            } else {
                                //TODO call here API to get BIN Details FROM Server
                                BIN_TAG_SCANNED = true;
                                String BinName = Objects.requireNonNull(GetBinNameByLabel(SELECTED_BIN)).getBinName();
                                binding.textScanBin.setText(BinName);
                            }

                        }
                    });

                }
                else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select an item");
                }

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
binding.btnRefresh.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        SELECTED_BIN = "";
        binList.clear();
        binding.textScanBin.setText("");
        binding.textItemDesc1.setText("");
        binding.edtPickedQty.setText("");
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
                    String FromBin = binding.textScanBin.getText().toString();
                    if(!TextUtils.isEmpty(FromBin)){
                        qty = binding.edtPickedQty.getText().toString();

                        if (qty.equals("")) {
                            //please add qty
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please add item quantity");
                        } else if(!qty.equalsIgnoreCase("0")){

                            Log.e("LIST", "UPDATED:");
                            BinPartialPalletMappingCreationProcessModel obj = new BinPartialPalletMappingCreationProcessModel();
                            obj.setPickedQty(Double.parseDouble(qty));
                            obj.setBinDescription(selectedSourceBinObject.getBinDescription());
                            //obj.setBinNumber(selectedSourceBinObject.getBinNumber());
                            obj.setBinNumber(FromBin);
                            obj.setBatchId(selectedSourceBinObject.getBatchId());
                            obj.setItemID(selectedSourceBinObject.getItemID());
                            obj.setItemName(selectedSourceBinObject.getItemName());
                            if(binList.isEmpty()){
                                obj.setStockBinId(null);
                            }
                            else{
                                PickListBin item = GetIDByLabel(FromBin);
                                if(item!= null){
                                    int stockBinID = GetIDByLabel(FromBin).getStockBinId();
                                    if(stockBinID == 0){
                                        obj.setStockBinId(null);
                                    }
                                    else{
                                        obj.setStockBinId(stockBinID);
                                    }
                                } else{
                                    obj.setStockBinId(null);
                                }
                            }
                            // if (!AssetUtils.isItemAlreadyAdded(selectedSourceBinObject.getBinDescription(), pickedOrderList)) {
                            BinPartialPalletMappingCreationProcessModel itemObj = AssetUtils.getItemObject(selectedSourceBinObject.getBinDescription(), selectedSourceBinObject.getBinNumber(), selectedSourceBinObject.getItemID(), orderList);
                            if(itemObj!=null){
                                if(itemObj.getPickedQty() >= Double.parseDouble(qty)){
                                    double prevQty = Double.parseDouble(qty);
                                    double TotalQty = 0;
                                    for (BinPartialPalletMappingCreationProcessModel item : pickedOrderList) {
                                        TotalQty += item.getPickedQty();
                                    }
                                    TotalQty += prevQty;
                                    double finalTotalQty = TotalQty;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.textTotalQty.setText(""+ finalTotalQty);
                                        }
                                    });
                                    pickedOrderList.add(obj);
                                    //orderList.remove(itemObj);
                                    double originalQty = itemObj.getPickedQty();
                                    double diff = originalQty-Double.parseDouble(qty);
                                    itemObj.setPickedQty(diff);
                                    //orderList.add(itemObj);

                                    //adapter.notifyDataSetChanged();
                                    adapter.notifyItemChanged(orderList.indexOf(itemObj));
                                    adapter.notifyItemChanged(filteredList.indexOf(itemObj));
                                    //binding.spBin.setSelectedItem(0);
                                    binding.spBin.setText("Select Bin");
                                    //binding.spSourceBin.setSelection(0);
                                    binding.textScanBin.setText("");

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
                            binList.clear();
                        }
                        else{
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid quantity");
                        }

                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select a bin");
                    }
                }
                else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select an item");
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
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "DC Tag Already scanned");
                                }else if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_TEMPORARY_STORAGE)){
                                    //TODO location Tag
                                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Location tag scanned");
                                    if(pickedOrderList.size()>0){
                                        LOCATION_TAG_ID=SCANNED_EPC;
                                        checkForValidationsAndComplete();
                                    }
                                }
                                else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a DC tag");
                                }
                            }

                            if (!PALLET_TAG_SCANNED) {
                                if(AssetUtils.getTagType(SCANNED_EPC).equals(AssetUtils.TYPE_PALLET)){
                                    //TODO
                                    if(db.checkAssetNameByProductTagId(SCANNED_EPC)){
                                        PALLET_TAG_SCANNED = true;
                                        PALLET_TAG_ID = SCANNED_EPC;
                                        String palletname = db.getProductNameByProductTagId(SCANNED_EPC);
                                        binding.textScanPallet.setText(palletname);
                                    } else {
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan a DC tag");
                                    }

                                } else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please scan DC tag");
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
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
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
            jsonObject.put("LocationName", db.getProductNameByProductTagId(LOCATION_TAG_ID));
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_NUMBER, workOrderNumber);
            jsonObject.put(APIConstants.CURRENT_WORK_ORDER_TYPE, workOrderType);
            jsonObject.put(APIConstants.K_DC_NO, DRNNo);
            jsonObject.put("DCTagID", PALLET_TAG_ID);
            jsonObject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<pickedOrderList.size();i++){
                JSONObject dataObject = new JSONObject();
                BinPartialPalletMappingCreationProcessModel obj = pickedOrderList.get(i);
                dataObject.put("BinName",obj.getBinNumber());
                String BinName = obj.getBinNumber().toString();
                dataObject.put("stockBinId", obj.getStockBinId());
                if(obj.getBatchId().equalsIgnoreCase(null)){
                    dataObject.put("BatchID",null);
                } else{
                    dataObject.put("BatchID",obj.getBatchId());
                }
                dataObject.put(APIConstants.K_ITEM_DESCRIPTION,obj.getBinDescription());
                dataObject.put("ItemName",obj.getItemName());
                dataObject.put("PickedItemID",obj.getItemID());
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
                                        AssetUtils.stopTimer();
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
        binding.spBin.setText("Select Bin");
        binding.textScanBin.setText("");
        if (pickedOrderList != null) {
            pickedOrderList.clear();
        }
        pickedAdapter.notifyDataSetChanged();
//        if (binObjectListForSourceSpinner != null) {
//            binObjectListForSourceSpinner.clear();
//        }
        //binSourceAdapter.notifyDataSetChanged();//changed
        binding.rvPallet.setAdapter(null);
        orderList.clear();
        for (BinPartialPalletMappingCreationProcessModel model : originalOrderList) {
            BinPartialPalletMappingCreationProcessModel newModel = new BinPartialPalletMappingCreationProcessModel();
            newModel.setItemID(model.getItemID());
            newModel.setBinDescription(model.getBinDescription());
            newModel.setItemName(model.getItemName());
            newModel.setBinNumber(model.getBinNumber());
            newModel.setBatchId(model.getBatchId());
            newModel.setOriginalPickedQty(model.getOriginalPickedQty());
            newModel.setPickedQty(model.getOriginalPickedQty()); // restore the original quantity
            orderList.add(newModel);
        }
        adapter = new BinPartialPalletMappingCreationProcessAdapter(context, orderList);
        binding.rvPallet.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        selectedSourceBinObject = null;
        binding.edtPickedQty.setText("");
        binding.textTotalQty.setText("");
        binding.textScanPallet.setText("");
        PALLET_TAG_ID = "";
        LOCATION_TAG_ID = "";
        SELECTED_BIN = "";
        SCANNED_EPC = "";
        if (epcs != null) {
            epcs.clear();
        }
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
        if (originalOrderList != null) {
            originalOrderList.clear();
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
                    String itemID = dataObject.getString("PickedItemID");
                    String itemDesc = dataObject.getString("ItemDescription");
                    String itemName = dataObject.getString("ItemName");
                    String binName = dataObject.getString("BinName");
                    String batchID = dataObject.getString("BatchID");
                    String pickUpQty = dataObject.getString("PickUpQty");
                    //binList.add(binName);
                    binPartialPalletMappingCreationProcessModel.setItemID(itemID);
                    binPartialPalletMappingCreationProcessModel.setBinDescription(itemDesc);
                    binPartialPalletMappingCreationProcessModel.setItemName(itemName);
                    binPartialPalletMappingCreationProcessModel.setBinNumber(binName);
                    binPartialPalletMappingCreationProcessModel.setBatchId(batchID);//changed
                    binPartialPalletMappingCreationProcessModel.setPickedQty(Double.parseDouble(pickUpQty));
                    binPartialPalletMappingCreationProcessModel.setOriginalPickedQty(Double.parseDouble(pickUpQty));
                    //binPartialPalletMappingCreationProcessModel.setClickedEnable(true);
                    orderList.add(binPartialPalletMappingCreationProcessModel);
                    originalOrderList.add(binPartialPalletMappingCreationProcessModel);
                    binObjectListForSourceSpinner.add(binPartialPalletMappingCreationProcessModel);
                }
                //binList = db.getBinName();
                if (binObjectListForSourceSpinner != null) {
                    if (binObjectListForSourceSpinner.size() > 0) {
                        BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                        binPartialPalletMappingCreationProcessModel.setBinDescription("Select Item");
                        binPartialPalletMappingCreationProcessModel.setBinNumber("");
                        binPartialPalletMappingCreationProcessModel.setPickedQty(0.0);
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
                    String pickUpQty = dataObject.getString("Qty");
                    String batchId = dataObject.getString("BatchID");

                    binPartialPalletMappingCreationProcessModel.setBinDescription(itemDesc);
                    binPartialPalletMappingCreationProcessModel.setBinNumber(binName);
                    binPartialPalletMappingCreationProcessModel.setPickedQty(Double.parseDouble(pickUpQty));
                    binPartialPalletMappingCreationProcessModel.setBatchId(batchId);
                    binObjectListForSourceSpinner.add(binPartialPalletMappingCreationProcessModel);
                }
                if (binObjectListForSourceSpinner != null) {
                    if (binObjectListForSourceSpinner.size() > 0) {
                        BinPartialPalletMappingCreationProcessModel binPartialPalletMappingCreationProcessModel = new BinPartialPalletMappingCreationProcessModel();
                        binPartialPalletMappingCreationProcessModel.setBinDescription("Select Item");
                        binPartialPalletMappingCreationProcessModel.setBinNumber("");
                        binPartialPalletMappingCreationProcessModel.setPickedQty(0.0);
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
    private void GetAuthorizationToken()
    {
        AuthorizationWMS authorization = new AuthorizationWMS();
        String  authorizationUrl ="http://192.168.100.18:8082/wms/api/auth/authorize";
        String username ="ashutosh.sahib@psl.co.in";
        String password ="pass123";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            Log.e("AuthReq", jsonObject.toString());
            AndroidNetworking.post(authorizationUrl).addJSONObjectBody(jsonObject)
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
                                    if (response.has("access_token")) {
                                        String accessToken = response.getString("access_token");
                                          authorization.setAccess_token(accessToken);
                                          token = accessToken;


                                    }
                                    if (response.has("name")) {
                                        if(!response.getString("name").equals(null))
                                        {
                                            String  name = response.getString("name");
                                            authorization.setName(name);
                                        }
                                    }
                                    if (response.has("username")) {
                                        if(!response.getString("username").equals(null))
                                        {
                                            String  username = response.getString("username");
                                            authorization.setUsername(username);
                                        }
                                    }
                                    if (response.has("phone")) {
                                        if(!response.getString("phone").equals(null))
                                        {
                                            String  phone = response.getString("phone");
                                            authorization.setPhone(phone);
                                        }
                                    }
                                    if (response.has("email")) {
                                        if(!response.getString("email").equals(null))
                                        {
                                            String  email = response.getString("email");
                                            authorization.setEmail(email);
                                        }
                                    }
                                    if (response.has("id")) {

                                            int  id = response.getInt("id");
                                            authorization.setId(id);
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
                                //AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                            } else {
                                //AssetUtils.showCommonBottomSheetErrorDialog(context, "Something went wrong.");
                            }

                        }
                    });
        } catch (JSONException e) {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
            Log.d("Err", e.getMessage());
        }
    }
    private void GetBinNameForSKU(String skuCode)
    {
        showProgress(context, "Please wait while fetching respective bins");
        if(binList != null){
            binList.clear();
        }
        if(pickedBinDetails != null){
            pickedBinDetails.clear();
        }

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        //String url = "http://192.168.100.18:8082/wms/api/v1/stocks/bins?page=0&size=1000&skuCode="+skuCode;
        String url = "http://192.168.100.18:8082/wms/api/v1/stocks/bins/skuCode/"+skuCode;
        Log.e("BinURL", url);
        AndroidNetworking.get(url)
                .addHeaders("Authorization", "Bearer "+token)
                .addHeaders("X-TenantId", "1")
                .addHeaders("Accept", "application/json")
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray result) {
Log.e("res",result.toString());
                        if (result != null) {
                            hideProgressDialog();
                            try {
                                if(result.length()>0){
                                    for(int i = 0; i< result.length(); i++){
                                        JSONObject data = result.getJSONObject(i);
                                        PickListBin bins = new PickListBin();
                                        if(data.has("id")){
                                            int id = data.getInt("id");
                                            bins.setStockBinId(id);
                                            Log.e("STOCKID", ""+id);
                                        }
                                        else{
                                            bins.setStockBinId(0);
                                        }
                                        if(data.has("label")){
                                            String label = data.getString("label");
                                            if(!binList.contains(label)){
                                                binList.add(label);
                                            }
                                            bins.setLabel(label);
                                            Log.e("STOCKLabel", label);
                                        }
                                        if(data.has("binName")){
                                            String bin = data.getString("binName");
                                            bins.setBinName(bin);
                                            Log.e("STOCKBin", bin);
                                        }
                                        if(data.has("batchMonth")){
                                            String batchMonth = data.getString("batchMonth");
                                            bins.setBatchMonth(batchMonth);
                                        }
                                        if(data.has("batchDateTime")){
                                            String batchDateTime = data.getString("batchDateTime");
                                            bins.setBatchDateTime(batchDateTime);
                                        }
                                        if(data.has("bayName")){
                                            String bayName = data.getString("bayName");
                                           bins.setBayName(bayName);
                                        }
                                        if(data.has("shelf")){
                                            String shelf = data.getString("shelf");
                                            bins.setShelf(shelf);
                                        }
                                        if(data.has("binNumber")){
                                            String binNumber = data.getString("binNumber");
                                            bins.setBinNumber(binNumber);
                                        }
                                        if(data.has("qty")){
                                            double qty = data.getDouble("qty");
                                            bins.setQty(qty);
                                        }
                                        pickedBinDetails.add(bins);
                                    }
                                }

                            } catch (JSONException e) {
                                hideProgressDialog();
                                throw new RuntimeException(e);
                            }

                        } else {
                            hideProgressDialog();
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        Log.e("ERROR", anError.getErrorDetail());
                        if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Having issue while connecting to server");
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Something went wrong.");
                        }
                    }
                });

    }
    private PickListBin GetBinNameByLabel(String label)
    {
        for (PickListBin item : pickedBinDetails) {
            if (item.getLabel().equals(label)) {
                return item;
            }
        }
        return null;
    }
    private PickListBin GetIDByLabel(String BinName)
    {
        for (PickListBin item : pickedBinDetails) {
            if (item.getBinName().equals(BinName)) {
                return item;
            }
        }
        return null;
    }
    private void GetBinDetails(String skucode) {

        if (!skucode.equals(null)) {
            new CollectInventoryData().execute(skucode);
        }
    }

    public class CollectInventoryData extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            showProgress(context, "Please wait while fetching Bin details");
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            String SKU = params[0];
            return SKU;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                    try {
                        hideProgressDialog();
                        GetBinNameForSKU(result);

                    } catch (OutOfMemoryError e) {
                        hideProgressDialog();
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Huge Data cannot be uploaded");
                    }


            } else {
                hideProgressDialog();
                AssetUtils.showCommonBottomSheetErrorDialog(context, "Something went wrong");
            }

        }

    }
}