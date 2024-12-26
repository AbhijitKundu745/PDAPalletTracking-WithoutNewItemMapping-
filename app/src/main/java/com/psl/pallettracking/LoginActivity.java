package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.DashboardModel;
import com.psl.pallettracking.adapters.SearchableAdapter;
import com.psl.pallettracking.database.AssetMaster;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityLoginBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Context context = this;
    private DatabaseHandler db;
    private ConnectionDetector cd;
    private HashMap<String, Integer> warehouseList = new HashMap<>();
    List<String> warehouseNames = new ArrayList<>();
    Dialog dialog;
    private String SELECTED_ITEM = "";
    private String default_source_item = "Select Warehouse";
    SearchableAdapter searchableAdapter;
    private Boolean IS_WAREHOSE_SELECTED = false;
    private Integer warehouseID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setTitle("USER LOGIN");
        getSupportActionBar().hide();

        cd = new ConnectionDetector(context);
        db = new DatabaseHandler(context);
       /* AssetUtils.getUTCSystemDateTimeInFormatt();
        String dt = AssetUtils.getSystemDateTimeInFormatt();
        Log.e("NORMALDT",dt);*/


        String androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        SharedPreferencesManager.setDeviceId(context, androidID);
        Log.e("DEVICEID", androidID);

        if (SharedPreferencesManager.getIsHostConfig(context)) {
            getWarehouseDetails();
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
        }

        //TODO comment below
        /*Intent i = new Intent(LoginActivity.this,PalletMovementActivity.class);
        i.putExtra("WorkOrderNumber","bdjhsb");
        i.putExtra("WorkOrderType","L0");
        startActivity(i);*/

        if (SharedPreferencesManager.getIsLoginSaved(context)) {
            binding.chkRemember.setChecked(true);
            binding.edtUserName.setText(SharedPreferencesManager.getSavedUser(context));
            binding.edtPassword.setText(SharedPreferencesManager.getSavedPassword(context));
        } else {
            binding.chkRemember.setChecked(false);
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
        }

        binding.btnLogin.setOnClickListener(view -> {
           /* Intent loginIntent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(loginIntent);*/
            if (SharedPreferencesManager.getIsHostConfig(context)) {

                String user = binding.edtUserName.getText().toString().trim();
                String password = binding.edtPassword.getText().toString().trim();
               /* try {
                    password = PSLEncryption.encrypt(password,PSLEncryption.publicKey);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                }*/
                if (user.equalsIgnoreCase("") || password.equalsIgnoreCase("")) {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.login_data_validation));
                } else if (!IS_WAREHOSE_SELECTED) {
                    AssetUtils.showCommonBottomSheetErrorDialog(context, "Please select a warehouse");
                }
                  else  {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(APIConstants.K_USER, user);
                            jsonObject.put(APIConstants.K_PASSWORD, password);
                            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
                            userLogin(jsonObject, APIConstants.M_USER_LOGIN, "Please wait...\n" + "User login is in progress");

                       /* Intent loginIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(loginIntent);*/
                        } catch (JSONException e) {

                        }
                    }
            } else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
            }

        });

        binding.imgSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent configIntent = new Intent(LoginActivity.this, URLConfigActivity.class);
                startActivity(configIntent);

            }
        });
        binding.btnClear.setOnClickListener(view -> {
            binding.chkRemember.setChecked(false);
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
            SharedPreferencesManager.setIsLoginSaved(context, false);
            SharedPreferencesManager.setSavedUser(context, "");
            SharedPreferencesManager.setSavedPassword(context, "");
            binding.chkRemember.setChecked(false);
        });
        binding.textDeviceId.setText("Device ID: " + SharedPreferencesManager.getDeviceId(context));
        binding.searchableTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharedPreferencesManager.getIsHostConfig(context)) {
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
                    searchableAdapter = new SearchableAdapter(context, warehouseNames);

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
                            SELECTED_ITEM = (String) searchableAdapter.getItem(position);
                            binding.searchableTextView.setText(SELECTED_ITEM);
                            if (SELECTED_ITEM.equalsIgnoreCase(default_source_item) || SELECTED_ITEM.equalsIgnoreCase("")) {
                                SELECTED_ITEM = "";
                                IS_WAREHOSE_SELECTED = false;

                            } else {
                                IS_WAREHOSE_SELECTED = true;
                                warehouseID = warehouseList.get(SELECTED_ITEM);
                            }

                        }
                    });

                }
             else {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
            }

            }
        });
    }


    public void userLogin(final JSONObject loginRequestObject, String METHOD_NAME, String progress_message) {
        showProgress(context, progress_message);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Log.e("URL", SharedPreferencesManager.getHostUrl(context) + METHOD_NAME);
        Log.e("LOGINREQUEST", loginRequestObject.toString());
        AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME).addJSONObjectBody(loginRequestObject)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        hideProgressDialog();
                        parseJson(result, loginRequestObject);
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        Log.e("ERROR", anError.getErrorDetail());
//                        if (BuildConfig.DEBUG) {
//                            // do something for a debug build
//                            try {
//                                parseJson(new JSONObject(AssetUtils.getJsonFromAssets(context,"loginres.json")),new JSONObject(AssetUtils.getJsonFromAssets(context,"loginreq.json")));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }else{
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
    }

    private void parseJson(JSONObject result, JSONObject loginRequestObject) {
        if (result != null) {
            try {
                Log.e("LOGINRESULT", result.toString());
                String status = result.getString(APIConstants.K_STATUS).trim();
                String message = result.getString(APIConstants.K_MESSAGE).trim();

                if (status.equalsIgnoreCase("true")) {
                    SharedPreferencesManager.setSavedUser(context, loginRequestObject.getString(APIConstants.K_USER));
                    SharedPreferencesManager.setSavedPassword(context, loginRequestObject.getString(APIConstants.K_PASSWORD));
                    SharedPreferencesManager.setSavedPassword(context, binding.edtPassword.getText().toString().trim());
                    JSONObject dataObject = null;
                    if (result.has(APIConstants.K_DATA)) {
                        dataObject = result.getJSONObject(APIConstants.K_DATA);
                        if (dataObject != null) {
                            if (dataObject.has(APIConstants.K_CUSTOMER_ID)) {
                                String customerid = dataObject.getString(APIConstants.K_CUSTOMER_ID).trim();
                                SharedPreferencesManager.setCustomerId(context, customerid);
                            }
                            if (dataObject.has(APIConstants.K_TAG_ACCESS_PASSWORD)) {
                                String tagpassword = dataObject.getString(APIConstants.K_TAG_ACCESS_PASSWORD).trim();
                                               /* try {
                                                    tagpassword = PSLEncryption.decrypt(tagpassword,PSLEncryption.publicKey);
                                                } catch (GeneralSecurityException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }*/
                                tagpassword = AssetUtils.numberToHex(tagpassword);
                                tagpassword = AssetUtils.get8DigitAssetSerialNumber(tagpassword);
                                if (tagpassword.length() > 8) {
                                    SharedPreferencesManager.setCurrentAccessPassword(context, tagpassword);
                                }
                            }

                            if (dataObject.has(APIConstants.K_USER_ID)) {
                                String userid = dataObject.getString(APIConstants.K_USER_ID).trim();
                                SharedPreferencesManager.setSavedUserId(context, userid);
                            }

                            if (dataObject.has(APIConstants.K_COMPANY_CODE)) {
                                String companycode = dataObject.getString(APIConstants.K_COMPANY_CODE).trim();
                                companycode = AssetUtils.numberToHex(companycode);
                                Log.e("COMPANYCODE", companycode);
                                SharedPreferencesManager.setCompanyCode(context, companycode);
                            }

                            List<AssetMaster> assetTypeMasterList = new ArrayList<>();
                            List<DashboardModel> dashboardMenuList = new ArrayList<>();

                            if (dataObject.has(APIConstants.K_DASHBOARD_ARRAY)) {
                                JSONArray dashboardMenu = null;
                                dashboardMenu = dataObject.getJSONArray(APIConstants.K_DASHBOARD_ARRAY);
                                if (dashboardMenu.length() > 0) {
                                    boolean syncAvailable = false;
                                    for (int dashboard = 0; dashboard < dashboardMenu.length(); dashboard++) {
                                        DashboardModel dashboardModel = new DashboardModel();
                                        JSONObject dashboardObject = dashboardMenu.getJSONObject(dashboard);
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_ID)) {
                                            String menuid = dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_ID).trim();
                                            menuid = menuid.trim();
                                            dashboardModel.setMenuId(menuid);

                                            Log.e("MENUID", menuid + " ORIGINAL:" + AppConstants.MENU_ID_ASSETSYNC + " ==>" + menuid.equalsIgnoreCase(AppConstants.MENU_ID_ASSETSYNC));
                                            if (menuid.equalsIgnoreCase(AppConstants.MENU_ID_ASSETSYNC)) {
                                                Log.e("MENUIDTRUE", menuid);
                                                syncAvailable = true;
                                            }

                                        }
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_NAME)) {
                                            dashboardModel.setMenuName(dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_NAME).trim());
                                        }
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_ACTIVITY_NAME)) {
                                            dashboardModel.setMenuActivityName(dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_ACTIVITY_NAME).trim());
                                        }
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_SEQUENCE)) {
                                            dashboardModel.setMenuSequence(dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_SEQUENCE).trim());
                                        }
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_ACTIVE)) {
                                            dashboardModel.setIsMenuActive(dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_ACTIVE).trim());
                                        }
                                        if (dashboardObject.has(APIConstants.K_DASHBOARD_MENU_IMAGE)) {
                                            dashboardModel.setMenuimageName(dashboardObject.getString(APIConstants.K_DASHBOARD_MENU_IMAGE).trim());
                                        }
                                        dashboardMenuList.add(dashboardModel);
                                    }

                                    if (!syncAvailable) {
                                        DashboardModel dashboardModelSync = new DashboardModel();
                                        dashboardModelSync.setMenuName(AppConstants.DASHBOARD_MENU_SYNC);
                                        dashboardModelSync.setDrawableImage(R.mipmap.ic_launcher_sync_foreground);
                                        dashboardModelSync.setMenuId(AppConstants.MENU_ID_ASSETSYNC);
                                        dashboardModelSync.setMenuimageName("");
                                        dashboardModelSync.setMenuSequence("5");
                                        dashboardModelSync.setIsMenuActive("true");
                                        dashboardMenuList.add(dashboardModelSync);
                                    }
                                }

                            } else {

                            }

                            if (dataObject.has(APIConstants.K_ASSETTYPE_MASTER)) {
                                JSONArray assettypeMaster = null;
                                assettypeMaster = dataObject.getJSONArray(APIConstants.K_ASSETTYPE_MASTER);

                                if (assettypeMaster.length() > 0) {
                                    for (int assettype = 0; assettype < assettypeMaster.length(); assettype++) {
                                        AssetMaster assetMaster = new AssetMaster();
                                        JSONObject vendorObject = assettypeMaster.getJSONObject(assettype);
                                        if (vendorObject.has(APIConstants.K_ASSET_TYPE_ID)) {
                                            String assettypeid = vendorObject.getString(APIConstants.K_ASSET_TYPE_ID).trim();
                                            assettypeid = AssetUtils.numberToHex(assettypeid);
                                            assettypeid = AssetUtils.get2DigitAssetTypeId(assettypeid);
                                            assetMaster.setAssetTypeId(assettypeid);
                                        }

                                        if (vendorObject.has(APIConstants.K_ASSET_TYPE_NAME)) {
                                            assetMaster.setAssetTypeName(vendorObject.getString(APIConstants.K_ASSET_TYPE_NAME).trim());
                                        }
                                        assetTypeMasterList.add(assetMaster);
                                    }
                                }

                            }

                            if (assetTypeMasterList.size() > 0) {

                                if (dashboardMenuList.size() > 0) {
                                    db.deleteDashboardMenuMaster();
                                    db.storeDashboardMenuMaster(dashboardMenuList);
                                } else {
                                    db.deleteDashboardMenuMaster();
                                    List<DashboardModel> dashboardList = AssetUtils.getDashboardDetails();
                                    db.storeDashboardMenuMaster(dashboardList);
                                }

                                db.deleteAssetTypeMaster();
                                db.storeAssetTypeMaster(assetTypeMasterList);

                                if (binding.chkRemember.isChecked()) {
                                    SharedPreferencesManager.setIsLoginSaved(context, true);
                                } else {
                                    SharedPreferencesManager.setIsLoginSaved(context, false);
                                }
                                SharedPreferencesManager.setWarehouseId(context, warehouseID);
                                Log.e("WID", ""+warehouseID);
                                if (db.getDashboardMenuCount() > 0) {

                                    Intent loginIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    startActivity(loginIntent);
                                } else {
                                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.no_dashboard_menu_active));
                                }

                            } else {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, "Asset Type/Vendor Master is Empty");
                            }
                        }
                    }

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

    public void getWarehouseDetails() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();

        Log.e("URL", SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_WAREHOUSE_MASTER);
        AndroidNetworking.get(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_WAREHOUSE_MASTER)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        hideProgressDialog();
                        if(result!=null){
                            try{
                                if (result.getBoolean(APIConstants.K_STATUS)) {
                                    JSONArray dataArray = result.getJSONArray("data");
                                    if(warehouseList != null){
                                        warehouseList.clear();
                                    }
                                    if(warehouseNames != null){
                                        warehouseNames.clear();
                                    }
                                    if (dataArray.length() > 0) {
                                        for (int i = 0; i < dataArray.length(); i++) {
                                            JSONObject dataObject = dataArray.getJSONObject(i);
                                            String WarehouseName = dataObject.getString(APIConstants.K_WAREHOUSE_NAME);
                                            int WarehouseID = dataObject.getInt(APIConstants.K_WAREHOUSE_ID);

                                            warehouseList.put(WarehouseName, WarehouseID);
                                            warehouseNames.add(WarehouseName);
                                            Log.e("Warehouses", warehouseList.toString());
                                        }
                                    }
                                }
                            }
                            catch (JSONException e){
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
                            }
                        }
                        else {
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
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                        } else {
                            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                        }
                        //}

                    }
                });
    }

    @Override
    protected void onResume() {
        if (SharedPreferencesManager.getIsHostConfig(context)) {
            getWarehouseDetails();
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.url_not_config));
        }
        super.onResume();
    }
}