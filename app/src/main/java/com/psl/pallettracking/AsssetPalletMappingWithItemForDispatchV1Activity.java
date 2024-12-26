package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AssetPalletWithItemDispatchAdapter;
import com.psl.pallettracking.adapters.skuitemListAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAsssetPalletMappingWithItemForDispatchV1Binding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AsssetPalletMappingWithItemForDispatchV1Activity extends AppCompatActivity {
    private Context context = this;
    ActivityAsssetPalletMappingWithItemForDispatchV1Binding binding;
    DatabaseHandler db;
    ConnectionDetector cd;
    skuitemListAdapter ItemAdapter, confirmationAdapter;
    AssetPalletWithItemDispatchAdapter selectedItemAdapter;
    List<ItemDetailsList> itemList, selectedItemList, filteredList, originalItemList;
    ItemDetailsList selectedItemObject;
    private String DC_TAG_ID = "";
    private String DC_NO = "";
    private String qty = "";
    private String START_DATE = "";
    private String END_DATE = "";
    private String Password = "PASSDISPATCH007";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_assset_pallet_mapping_with_item_for_dispatch_v1);
        binding = DataBindingUtil.setContentView(AsssetPalletMappingWithItemForDispatchV1Activity.this, R.layout.activity_assset_pallet_mapping_with_item_for_dispatch_v1);
        getSupportActionBar().hide();

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
        START_DATE = AssetUtils.getSystemDateTimeInFormatt();
        setAllVisibility();
        binding.rvPallet.setLayoutManager(new GridLayoutManager(context, 1));
        if (itemList != null) {
            itemList.clear();
        }
        itemList = new ArrayList<>();
        originalItemList = new ArrayList<>();
        ItemAdapter = new skuitemListAdapter(context, itemList);
        ItemAdapter.notifyDataSetChanged();
        binding.rvPallet.setAdapter(ItemAdapter);


        binding.rvPicked.setLayoutManager(new GridLayoutManager(context, 1));
        selectedItemList = new ArrayList<>();
        selectedItemAdapter = new AssetPalletWithItemDispatchAdapter(context, selectedItemList);
        binding.rvPicked.setAdapter(selectedItemAdapter);
        selectedItemAdapter.notifyDataSetChanged();


        filteredList = new ArrayList<>();
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
                    filteredList.addAll(itemList);
                } else {
                    // Filter items based on search query
                    for (ItemDetailsList item : itemList) {
                        if (item.getItemDesc().toLowerCase(Locale.getDefault()).contains(searchText)) {
                            filteredList.add(item);
                        }
                    }
                }
                ItemAdapter.filterList(filteredList);
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
        ItemAdapter.setOnItemClickListener(new skuitemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!areAllItemsScanned()) {
                    // Handle item click here
                    String searchText = binding.edtSearch.getText().toString().toLowerCase(Locale.getDefault());

                    if (searchText.length() == 0) {
                        ItemDetailsList clickedItem = itemList.get(position);
                        ItemDetailsList item = getItemByItemSKU(clickedItem.getItemDesc(), clickedItem.getBatchID(), clickedItem.getBinName());
                        if (item != null) {
                            int originalPosition = itemList.indexOf(clickedItem); // Find original position in unfiltered list
                            if (Double.parseDouble(item.getPickedQty()) > Double.parseDouble(item.getScannedQty())) {

                                if (originalPosition != -1) {
                                    // Perform actions based on the clicked item from the unfiltered list
                                    binding.textItemDesc1.setText(clickedItem.getItemDesc());
                                    binding.edtPickedQty.setText("" + clickedItem.getPickedQty());
                                    selectedItemObject = itemList.get(originalPosition);
                                }
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "All expected items have already selected for this SKU");
                            }
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, "Item not found");
                        }
                    } else {
                        ItemDetailsList clickedItemFilter = filteredList.get(position);// Use filtered list
                        ItemDetailsList item = getItemByItemSKU(clickedItemFilter.getItemDesc(), clickedItemFilter.getBatchID(), clickedItemFilter.getBinName());
                    if(item != null){
                        int originalPosition1 = filteredList.indexOf(clickedItemFilter); // Find original position in unfiltered list
                      if (Double.parseDouble(item.getPickedQty()) > Double.parseDouble(item.getScannedQty())) {
                        if (originalPosition1 != -1) {
                            // Perform actions based on the clicked item from the unfiltered list
                            binding.textItemDesc1.setText(clickedItemFilter.getItemDesc());
                            binding.edtPickedQty.setText("" + clickedItemFilter.getPickedQty());
                            selectedItemObject = filteredList.get(originalPosition1); // Use original position
                        }
                      }
                      else {
                          AssetUtils.showCommonBottomSheetErrorDialog(context, "All expected items have already selected for this SKU");
                      }
                    }
                    else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Item not found");
                    }
                }
                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "All items have already selected");
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
                    binding.llSourceItem.setVisibility(View.GONE);
                }
                if (text.equals("-")) {
                    binding.textEnlargeMainItems.setText("+");
                    binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_green));
                    binding.llPicked.setVisibility(View.VISIBLE);
                    binding.llSourceItem.setVisibility(View.VISIBLE);
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
                    binding.llSourceItem.setVisibility(View.GONE);
                }
                if (text.equals("-")) {
                    binding.textEnlargePickedItems.setText("+");
                    binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_green));
                    binding.llMain.setVisibility(View.VISIBLE);
                    binding.llSourceItem.setVisibility(View.VISIBLE);
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
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedItemObject != null) {

                    if (qty.equals("")) {
                        //please add qty
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please add item quantity");
                    } else if (!qty.equalsIgnoreCase("0")) {
                        String batchID = binding.batchID.getText().toString();
                        double prevQty = Double.parseDouble(qty);
                        double TotalQty = 0;
                        for (ItemDetailsList item1 : selectedItemList) {
                            TotalQty += Double.parseDouble(item1.getScannedQty());
                        }
                        TotalQty += prevQty;
                        double finalTotalQty = TotalQty;
                        ItemDetailsList obj = new ItemDetailsList();
                        obj.setScannedQty(qty);
                        obj.setItemDesc(selectedItemObject.getItemDesc());
                        obj.setBatchID(batchID);
                        obj.setSkuCode(selectedItemObject.getSkuCode());
                        obj.setBinName(selectedItemObject.getBinName());

                        ItemDetailsList item = getItemByItemSKU(selectedItemObject.getItemDesc(), selectedItemObject.getBatchID(), selectedItemObject.getBinName());
                        if (item != null) {
                            if (Double.parseDouble(item.getPickedQty()) >= Double.parseDouble(qty)) {
                                selectedItemList.add(obj);
                                selectedItemAdapter.notifyItemInserted(selectedItemList.size() - 1);
                                double scannedQty = Double.parseDouble(item.getScannedQty()) + Double.parseDouble(qty);
                                double remainingQty = Double.parseDouble(item.getPickedQty()) - scannedQty;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.textTotalQty.setText("" + finalTotalQty);
                                    }
                                });
                                item.setScannedQty(String.valueOf(scannedQty));
                                item.setRemainingQty(String.valueOf(remainingQty));
                                ItemAdapter.notifyItemChanged(itemList.indexOf(item));
                                ItemAdapter.notifyItemChanged(filteredList.indexOf(item));
                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "The selected qty cannot be greater than the original qty");
                            }
                        }

                        selectedItemAdapter.notifyDataSetChanged();
                        selectedItemObject = null;

                        binding.edtPickedQty.setText("");
                        binding.textItemDesc1.setText("");
                        binding.batchID.setText("");
                    } else {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter a valid quantity");
                    }

                } else {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select an item");
                }
            }
        });
        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomConfirmationDialog("Are you sure you want to clear data", "CLEAR");
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomConfirmationDialog("Are you sure you want to go back", "BACK");
            }
        });
        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (areAllItemsScanned()) {
                    END_DATE = AssetUtils.getSystemDateTimeInFormatt();
                    uploadInventoryToServer();

                } else {
                    showCustomConfirmationDialogForSpecial("Are you sure want to save the data without scanning all the items?", "UPLOAD");
                }
            }
        });

    }

    Dialog customConfirmationDialog, customConfirmationDialogSpec;

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
                } else if (action.equals("CLEAR")) {
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
                if (customConfirmationDialog != null && customConfirmationDialog.isShowing()) {
                    customConfirmationDialog.dismiss();
                }
            }
        });
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }

    private void getSKUDetails(String DC_NO) {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put(APIConstants.K_DC_NO, DC_NO);
            jsonObject.put("Type", "DISPATCH_WITHOUTQR");
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
                                        ItemAdapter.notifyDataSetChanged();
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
        if (itemList != null) {
            itemList.clear();
        }
        if (originalItemList != null) {
            originalItemList.clear();
        }
        if (dataArray.length() > 0) {
            for (int i = 0; i < dataArray.length(); i++) {
                try {
                    ItemDetailsList itemList1 = new ItemDetailsList();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String SKUCode = dataObject.getString("SkuCode");
                    String ItemDesc = dataObject.getString("ItemDesc");
                    String PickedQty = dataObject.getString("PickedQty");
                    String BatchID = dataObject.getString("BatchID");
                    String BinName = dataObject.getString("BinName");
                    itemList1.setItemDesc(ItemDesc);
                    itemList1.setSkuCode(SKUCode);
                    itemList1.setPickedQty(PickedQty);
                    itemList1.setOriginalPickedQty(PickedQty);
                    itemList1.setBatchID(BatchID);
                    itemList1.setBinName(BinName);
                    itemList.add(itemList1);
                    originalItemList.add(itemList1);

                } catch (JSONException e) {
                    ItemAdapter.notifyDataSetChanged();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            }
        }
        ItemAdapter.notifyDataSetChanged();
    }

    private void setAllVisibility() {
        binding.textEnlargePickedItems.setText("+");
        binding.textEnlargeMainItems.setText("+");
        binding.textEnlargePickedItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.textEnlargeMainItems.setBackground(getDrawable(R.drawable.round_button_green));
        binding.llPicked.setVisibility(View.VISIBLE);
        binding.llMain.setVisibility(View.VISIBLE);
        binding.llSourceItem.setVisibility(View.VISIBLE);
    }

    private ItemDetailsList getItemByItemSKU(String itemDesc, String batchID, String BinName) {
        for (ItemDetailsList item : itemList) {
            if (item.getItemDesc().equals(itemDesc) && item.getBatchID().equals(batchID) && item.getBinName().equals(BinName)) {
                return item;
            }
        }
        return null;
    }
    private boolean areAllItemsScanned() {
        for (ItemDetailsList item : itemList) {
            if (Double.parseDouble(item.getRemainingQty())>0) {
                return false;
            }
        }
        return true;
    }

    private void uploadInventoryToServer() {

        if (selectedItemList.size() > 0) {
            new CollectInventoryData().execute("ABC");
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, "No data to upload");
        }

    }

    public class CollectInventoryData extends AsyncTask<String, String, JSONObject> {
        protected void onPreExecute() {
            showProgress(context, "Collectiong Data To Upload");
            super.onPreExecute();
        }

        protected JSONObject doInBackground(String... params) {
            if (selectedItemList.size() > 0) {
                try {
                    JSONObject jsonobject = null;
                    jsonobject = new JSONObject();
                    jsonobject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
                    jsonobject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
                    jsonobject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, "AssetPalletMapping");
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, selectedItemList.size());
                    jsonobject.put(APIConstants.K_PARENT_TAG_ID, DC_TAG_ID);
                    jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "DC");
                    jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
                    jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));
                    jsonobject.put(APIConstants.K_DRN, DC_NO);
                    jsonobject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < selectedItemList.size(); i++) {
                        JSONObject barcodeObject = new JSONObject();
                        ItemDetailsList obj = selectedItemList.get(i);
                        String items = obj.getItemDesc();
                        String batchID = obj.getBatchID();
                        String itemCode = obj.getSkuCode();
                        String qty = obj.getScannedQty();
                        String itemDescription = items + ","+ itemCode;
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, itemDescription);
                        barcodeObject.put("Qty", qty);
                        barcodeObject.put("BatchID", batchID);

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
                        hideProgressDialog();
                        // uploadInventory(result, APIConstants.M_UPLOAD_ASSET_PALLET_MAPPING, "Please wait...\n" + " Mapping is in progress");
                        uploadInventory(result, APIConstants.M_UPLOAD_ITEM_DETAILS, "Please wait...\n" + " Saving is in progress");

                    } catch (OutOfMemoryError e) {
                        hideProgressDialog();
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Huge Data cannot be uploaded");
                    }

                } else {
                    hideProgressDialog();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            } else {
                hideProgressDialog();
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
                        if (result != null) {
                            try {
                                Log.e("ASSETPALLETMAPRES", result.toString());
                                String status = result.getString(APIConstants.K_STATUS);
                                String message = result.getString(APIConstants.K_MESSAGE);

                                if (status.equalsIgnoreCase("true")) {
                                    setDefault();
                                    selectedItemAdapter.notifyDataSetChanged();
                                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Mapping Done Successfully");
                                    hideProgressDialog();
                                    finish();
                                } else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                }
                            } catch (JSONException e) {
                                hideProgressDialog();
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
                            }
                        } else {
                            hideProgressDialog();
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
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
        confirmationAdapter = new skuitemListAdapter(context, itemList);
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
                            END_DATE = AssetUtils.getSystemDateTimeInFormatt();
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

    private void setDefault() {
        if (selectedItemList != null) {
            selectedItemList.clear();
        }
        if(filteredList != null){
            filteredList.clear();
        }
        selectedItemAdapter.notifyDataSetChanged();
        binding.rvPallet.setAdapter(null);
        itemList.clear();
        for (ItemDetailsList model : originalItemList) {
            ItemDetailsList newModel = new ItemDetailsList();
            newModel.setItemDesc(model.getItemDesc());
            newModel.setSkuCode(model.getSkuCode());
            newModel.setOriginalPickedQty(model.getOriginalPickedQty());
            newModel.setPickedQty(model.getOriginalPickedQty());
            newModel.setBatchID(model.getBatchID());
            newModel.setBinName(model.getBinName());
            newModel.setScannedQty("0");
            newModel.setRemainingQty(model.getOriginalPickedQty());
            itemList.add(newModel);
        }
        ItemAdapter = new skuitemListAdapter(context, itemList);
        binding.rvPallet.setAdapter(ItemAdapter);
        ItemAdapter.notifyDataSetChanged();
        selectedItemObject = null;
        binding.edtPickedQty.setText("");
        binding.textTotalQty.setText("");
        binding.batchID.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (selectedItemList != null) {
            selectedItemList.clear();
            selectedItemObject = null;
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Are you sure you want to go back", "BACK");
    }
}