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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.AssetPalletMapWithoutQRAdapter;
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityAssetPalletMappingWithItemForDispatchBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;
import com.psl.pallettracking.helper.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AssetPalletMappingWithItemForDispatchActivity extends AppCompatActivity {
    private Context context = this;
    private ActivityAssetPalletMappingWithItemForDispatchBinding binding;
    private AssetPalletMapWithoutQRAdapter qrAdapter;
    private ConnectionDetector cd;
    private DatabaseHandler db;
    String START_DATE = "";
    String END_DATE = "";

    public ArrayList<HashMap<String, String>> barcodeList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> barcodeHashMap = new HashMap<>();
    ArrayList<String> barcodes = new ArrayList<>();
    String menu_id = AppConstants.MENU_ID_CARTON_PALLET_MAPPING;
    String activity_type = "";
    String DC_NO = "";
    String DC_TAG_ID = "";

    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_asset_pallet_with_item);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_pallet_mapping_with_item_for_dispatch);
        getSupportActionBar().hide();
        cd = new ConnectionDetector(context);
        db = new DatabaseHandler(context);
        getItemDescriptionList();
        Intent intent = getIntent();
        DC_NO = intent.getStringExtra("DRN");
        DC_TAG_ID = intent.getStringExtra("DC_TAG_ID");
        Log.e("DC_TAG_ID", DC_TAG_ID);
        binding.TruckNumber.setText(SharedPreferencesManager.getTruckNumber(context));
        binding.TruckNumber.setSelected(true);
        binding.LocationName.setText(SharedPreferencesManager.getLocationName(context));
        binding.LocationName.setSelected(true);
        binding.DRN.setText(DC_NO);
        binding.DRN.setSelected(true);


        activity_type = db.getMenuActivityNameByMenuID(menu_id);
        Log.e("TYPE", activity_type);

        qrAdapter = new AssetPalletMapWithoutQRAdapter(context, barcodeList);
        binding.LvTags.setAdapter(qrAdapter);
        qrAdapter.notifyDataSetChanged();
        START_DATE = AssetUtils.getSystemDateTimeInFormatt();

        SharedPreferencesManager.setPower(context, 10);

        binding.btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomConfirmationDialog("Are you sure you want to upload", "UPLOAD");
            }
        });

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "CANCEL");
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomConfirmationDialog(getResources().getString(R.string.confirm_cancel_scanning), "BACK");
            }
        });


        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if (SELECTED_ITEM.equalsIgnoreCase("") || SELECTED_ITEM.equalsIgnoreCase(default_source_item)) {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select Item");
                } else {
                    String count = binding.edtQty.getText().toString();
                    if (count.equalsIgnoreCase("0") || count.equalsIgnoreCase("")) {
                        AssetUtils.showCommonBottomSheetErrorDialog(context, "Please enter valid quantity");
                    } else {
                        addBarcodeToList(SELECTED_ITEM, count);
                        int prevCount = Integer.parseInt(binding.totalQty.getText().toString());
                        int currCount = Integer.parseInt(count);
                        int totalCount = prevCount + currCount;
                        binding.totalQty.setText("" + totalCount);
                        binding.edtQty.setText("");
                    }
                }
            }
        });

        binding.searchableTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize dialog
                dialog = new Dialog(AssetPalletMappingWithItemForDispatchActivity.this);

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
                searchableAdapter = new SearchableAdapter(AssetPalletMappingWithItemForDispatchActivity.this, barcodes);

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
                        // when item selected from list
                        // set selected item on textView
                        // Dismiss dialog
                        dialog.dismiss();
                        SELECTED_ITEM = (String) searchableAdapter.getItem(position);
                        binding.searchableTextView.setText(SELECTED_ITEM);
                        if (SELECTED_ITEM.equalsIgnoreCase(default_source_item) || SELECTED_ITEM.equalsIgnoreCase("")) {
                            SELECTED_ITEM = "";

                            binding.edtQty.setText("");
                            binding.btnAdd.setVisibility(View.INVISIBLE);
                            binding.edtQty.setVisibility(View.INVISIBLE);

                        } else {

                            binding.edtQty.setVisibility(View.VISIBLE);
                            binding.btnAdd.setVisibility(View.VISIBLE);

                        }
                    }
                });
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();

        // If no view currently has focus, create a new one, just so we can grab a window token from it.
        if (view == null) {
            view = new View(this);
        }

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String SELECTED_ITEM = "";
    private String default_source_item = "Select Item";
    Dialog dialog;
    SearchableAdapter searchableAdapter;
    int CURRENT_INDEX = -1;

    //HashMap<String, String> hashMap = new HashMap<>();//,tagList.get(position).get("MESSAGE")
    public void onListItemClicked(HashMap<String, String> hashmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = checkIsBarcodeExist(hashmap.get("BARCODE"));
                if (index == -1) {

                } else {
                    CURRENT_INDEX = index;
                    showCustomConfirmationDialog("Are you sure you want to delete", "DELETE");

                }
                Toast.makeText(context, hashmap.get("MESSAGE"), Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void setDefault() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.textCount.setVisibility(View.GONE);
                binding.btnAdd.setVisibility(View.GONE);
                binding.edtQty.setVisibility(View.GONE);
                binding.edtQty.setText("");
                SELECTED_ITEM = "";
                if (barcodeList != null) {
                    barcodeList.clear();
                }

                binding.textCount.setText("Count : " + barcodeList.size());
                binding.totalQty.setText("" + 0);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
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
    }

    public int checkIsBarcodeExist(String barcode) {
//        if (StringUtils.isEmpty(barcode)) {
//            return -1;
//        }
//        return binarySearch(barcodes, barcode);
        Log.e("searchbarcode", barcode);

        int index = -1;
        if (barcodeList.size() == 0) {
            return -1;
        }
        for (int i = 0; i < barcodeList.size(); i++) {
            String existingBarcode = barcodeList.get(i).get("BARCODE");
            Log.e("existingbarcode", "existing:" + existingBarcode);
            if (existingBarcode != null && existingBarcode.equals(barcode)) {
                return i;
            }

            if (StringUtils.isEmpty(barcode)) {
                return -1;
            }
        }
        return -1;
        // return binarySearch(barcodes, barcode);
    }

    /**
     * 二分查找，找到该值在数组中的下标，否则为-1
     */

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
                    uploadInventoryToServer();
                } else if (action.equals("CANCEL")) {
                    setDefault();
                } else if (action.equals("BACK")) {
                    setDefault();
                    finish();

                } else if (action.equals("DELETE")) {
                    barcodeList.remove(CURRENT_INDEX);

                    CURRENT_INDEX = -1;
                    binding.textCount.setText("Count : " + barcodeList.size());
                    qrAdapter.notifyDataSetChanged();
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
                    jsonobject.put(APIConstants.K_ACTIVITY_TYPE, activity_type);
                    jsonobject.put(APIConstants.K_INVENTORY_START_DATE_TIME, START_DATE);
                    jsonobject.put(APIConstants.K_INVENTORY_END_DATE_TIME, END_DATE);
                    jsonobject.put(APIConstants.K_TOUCH_POINT_ID, "1");
                    jsonobject.put(APIConstants.K_INVENTORY_COUNT, barcodeList.size());
                    jsonobject.put(APIConstants.K_PARENT_TAG_ID, DC_TAG_ID);
                    jsonobject.put(APIConstants.K_PARENT_ASSET_TYPE, "DC");
                    jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
                    jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));
                    jsonobject.put(APIConstants.K_DRN, DC_NO);
                    //jsonobject.put(APIConstants.K_PALLET_ID, CURRENT_EPC);
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < barcodeList.size(); i++) {
                        JSONObject barcodeObject = new JSONObject();
                        String epc = barcodeList.get(i).get("BARCODE");
                        String qty = barcodeList.get(i).get("COUNT");
                        //barcodeObject.put(APIConstants.K_ACTIVITY_DETAILS_ID, epc + AssetUtils.getSystemDateTimeInFormatt());
                        barcodeObject.put(APIConstants.K_ITEM_DESCRIPTION, epc);
                        barcodeObject.put("Qty", qty);

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

    private void getItemDescriptionList() {
        try {
            JSONObject jsonobject = null;
            jsonobject = new JSONObject();

            jsonobject.put(APIConstants.K_TRUCK_NUMBER, SharedPreferencesManager.getTruckNumber(context));
            jsonobject.put(APIConstants.K_DRN, SharedPreferencesManager.getDRN(context));
            jsonobject.put(APIConstants.K_PROCESS_TYPE, SharedPreferencesManager.getProcessType(context));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.edtQty.setText("");
                    binding.edtQty.setVisibility(View.GONE);
                    binding.btnAdd.setVisibility(View.GONE);
                    SELECTED_ITEM = "";
                    if (barcodes != null) {
                        barcodes.clear();
                    }
                    showProgress(context, "Please wait...\nGetting Item Description List");
                }
            });


            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();

            Log.e("GETITEMDESCURL", SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_ITEM_DETAILS);
            Log.e("GETITEMDESCREQ", jsonobject.toString());
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_ITEM_DETAILS).addJSONObjectBody(jsonobject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                }
                            });

                            //allow_trigger_to_press = true;
                            if (result != null) {
                                try {
                                    Log.e("GETITEMDESCRES", result.toString());
                                    String status = result.getString(APIConstants.K_STATUS);
                                    String message = result.getString(APIConstants.K_MESSAGE);

                                    if (status.equalsIgnoreCase("true")) {
                                        //allow_trigger_to_press = false;

                                        if (result.has(APIConstants.K_DATA)) {
                                            JSONArray dataArray = result.getJSONArray(APIConstants.K_DATA);
                                            if (dataArray.length() > 0) {
                                                for (int i = 0; i < dataArray.length(); i++) {
                                                    JSONObject dataObject = dataArray.getJSONObject(i);
                                                    String itemDescr = dataObject.getString(APIConstants.K_ITEM_DESCRIPTION);
                                                    barcodes.add(itemDescr);
                                                }
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AssetUtils.showCommonBottomSheetErrorDialog(context, "No Item Description data found");
                                                    }
                                                });
                                            }
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "No Item Description data found");
                                                }
                                            });

                                            //error:-  No Item Description data found
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AssetUtils.showCommonBottomSheetErrorDialog(context, message);

                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));

                                        }
                                    });


                                }
                            } else {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressDialog();
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.communication_error));

                                    }
                                });
                            }
                            //barcodes.add("ABCD1");
                        }

                        @Override
                        public void onError(ANError anError) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
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
                    });
        } catch (JSONException ex) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));

                }
            });

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
                                    qrAdapter.notifyDataSetChanged();
                                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Mapping Done Successfully");
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

    private void addBarcodeToList(String barcode, String count) {
        hideProgressDialog();
        Log.e("BARCODECOUNT", "BARCODE:" + barcode + " COUNT:" + count);
        barcodeHashMap = new HashMap<>();
        barcodeHashMap.put("EPC", "EPC");
        barcodeHashMap.put("BARCODE", barcode);
        barcodeHashMap.put("ASSETNAME", barcode);
        barcodeHashMap.put("COUNT", count);
        barcodeHashMap.put("STATUS", "true");
        barcodeHashMap.put("MESSAGE", "");
        int index = checkIsBarcodeExist(barcode);
        Log.e("BARCODEINDEX", "" + index);
        Log.e("BARCODESIZE", "" + barcodeList.size());

        if (index == -1) {
            barcodeList.add(barcodeHashMap);
            if (!barcodes.contains(barcode)) {
                barcodes.add(barcode);

            }
        } else {

            AssetUtils.showCommonBottomSheetErrorDialog(context, "Item already added");


        }
        binding.textCount.setText("Count : " + barcodeList.size());
        qrAdapter.notifyDataSetChanged();
        END_DATE = AssetUtils.getSystemDateTimeInFormatt();

    }
}