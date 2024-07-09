package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.CustomRecyclerViewDashboardAdapter;
import com.psl.pallettracking.database.AssetMaster;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.database.ProductMaster;
import com.psl.pallettracking.databinding.ActivityDashboardBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AppConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.BatteryStatusHelper;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.NetworkUtils;
import com.psl.pallettracking.helper.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DashboardActivity extends AppCompatActivity {

    ActivityDashboardBinding binding;
    private Context context = this;
    private DatabaseHandler db;
    private ConnectionDetector cd;
    String NetworkStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard);
        setTitle("ASSET MANAGEMENT");
        getSupportActionBar().hide();

        db = new DatabaseHandler(context);
        cd = new ConnectionDetector(context);
        BatteryStatusHelper batteryStatusHelper = getBatteryStatus();
        Log.e("BatteryStat","Battery Level: " + batteryStatusHelper.level + "%, Charging: " + batteryStatusHelper.charging);

        NetworkUtils.checkPingStrength("192.168.1.7", new NetworkUtils.PingStrengthCallback() {
            @Override
            public void onPingStrengthResult(String result) {
                Log.d("PingStrength", result);
                // Update your UI here
            }
        });
        // set a GridLayoutManager with default vertical orientation and 2 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2); // you can change grid columns to 3 or more
        binding.recycleview.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        //  call the constructor of CustomAdapter to send the reference and data to Adapter
        CustomRecyclerViewDashboardAdapter customAdapter = new CustomRecyclerViewDashboardAdapter(DashboardActivity.this, db.getDashboardMenuList());
        binding.recycleview.setAdapter(customAdapter); // set the Adapter to RecyclerView

    }

    public void gridClicked(int position, String name,String menu_id,String isMenuActive) {
        Log.e("ISACTIVE",isMenuActive);
        if(isMenuActive.equalsIgnoreCase("False")){
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.inactive_dashboard_menu));
        }else{
            switch (menu_id) {
                case AppConstants.MENU_ID_CARTON_PALLET_MAPPING:
                    assetpalletMappingInClicked();

                    break;
                case AppConstants.MENU_ID_ITEM_PALLET_MAPPING:
                    assetpalletMappingOutClicked();

                    break;
                case AppConstants.MENU_ID_CONTAINER_PALLET_MAPPING:
                    PalletContainerMappingClicked();

                    break;
                case AppConstants.MENU_ID_PALLET_MOVEMENT:
                    PalletMovementClicked();
                    break;
                case AppConstants.MENU_ID_PARTIAL:
                    DispatchPalletCreationClicked();
                    break;
                case AppConstants.MENU_ID_ITEM_MOVEMENT:
                    ItemMovementClicked();
                    break;
                case AppConstants.MENU_ID_INVENTORY:
                    if (db.getAssetMasterCount() > 0) {
                        inventoryClicked();
                    }else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
                    }

                    break;
                case AppConstants.MENU_ID_SEARCH:

                    if (db.getAssetMasterCount() > 0) {
                        searchClicked();
                    }else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
                    }
                    break;
                case AppConstants.MENU_ID_ASSETSYNC:
                    syncClicked();
                    break;

                case AppConstants.MENU_ID_MAP_PARTIAL_PALLET:
                    mapPartialPalletCLicked();
                    break;
                default:
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.unIntegrated_dashboard_menu));
                    break;
            }
        }
    }

    private void mapPartialPalletCLicked() {

    }

    public void assetpalletMappingInClicked() {
        String type = "AssetPalletMapIn";
        showProgress(context,"Processing");
        if (db.getProductMasterCount() > 0) {
            Intent inventoryIntent = new Intent(DashboardActivity.this, TruckMappingActivity.class);
            startActivity(inventoryIntent);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync ?", type);

            // AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }
    public void assetpalletMappingOutClicked() {
        String type = "AssetPalletMapOut";
        showProgress(context,"Processing");
        if (db.getProductMasterCount() > 0) {
            Intent inventoryIntent = new Intent(DashboardActivity.this, LoadingItemMappingActivity.class);
            startActivity(inventoryIntent);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync ?", type);

            // AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }
    public void PalletMovementClicked() {
        String type = "PalletMovement";
        showProgress(context,"Processing");
        if (db.getProductMasterCount() > 0) {
            Intent inventoryIntent = new Intent(DashboardActivity.this, PalletMovementActivity.class);
            startActivity(inventoryIntent);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync ?", type);

            // AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }
    public void DispatchPalletCreationClicked() {
        String type = "DispatchPalletCreation";
        showProgress(context,"Processing");
        if (db.getProductMasterCount() > 0) {
            Intent i = new Intent(DashboardActivity.this,BinPartialPalletMappingActivity.class);
        startActivity(i);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync ?", type);

            // AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }
    public void ItemMovementClicked() {
        String type = "ItemMovement";
        showProgress(context,"Processing");
        if (db.getProductMasterCount() > 0) {
            Intent i = new Intent(DashboardActivity.this,ItemMovementActivity.class);
            startActivity(i);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync ?", type);

            // AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }
    public void PalletContainerMappingClicked() {
        String type = "PalletContainerMap";
        if (db.getProductMasterCount() > 0) {
            Intent inventoryIntent = new Intent(DashboardActivity.this, PalletContainerMappingActivity.class);
            startActivity(inventoryIntent);
        } else {
            showCustomConfirmationDialog("No Asset Master Sync, Are you sure you want to proceed without Asset Master Sync?", type);
            //AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }

    }

    public void inventoryClicked() {
        if (db.getAssetTypeMasterCount() > 0) {
            showProgress(context, getResources().getString(R.string.uhf_initialization));
            Intent inventoryIntent = new Intent(DashboardActivity.this, AssetInventoryActivity.class);
            inventoryIntent.putExtra("type", 0);
            //0 - inventory
            //1 - check in
            //2 - check out
            startActivity(inventoryIntent);
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }
    }

    public void searchClicked() {
        if (db.getAssetTypeMasterCount() > 0) {
            showProgress(context, getResources().getString(R.string.uhf_initialization));
            Intent searchIntent = new Intent(DashboardActivity.this, AssetSearchActivity.class);
            startActivity(searchIntent);
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.asset_type_master_sync_error));
        }
    }

    public void syncClicked() {
        if (cd.isConnectingToInternet()) {
            //JSONObject jsonObject = new JSONObject();
//                jsonObject.put(APIConstants.K_USER_ID, SharedPreferencesManager.getSavedUserId(context));
//                jsonObject.put(APIConstants.K_CUSTOMER_ID, SharedPreferencesManager.getCustomerId(context));
//                jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));//changed
            String apiUrl= APIConstants.M_GET_PRODUCT_MASTER +"{" +SharedPreferencesManager.getCustomerId(context)+ "}";
            fetchAssetMaster( apiUrl, "Please wait...\n" + "Getting Master",APIConstants.K_ACTION_SYNC);

                       /* Intent loginIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(loginIntent);*/
            //fetchAssetMaster(APIConstants.M_GET_PRODUCT_MASTER, "Please wait...\n" + "Getting Master", APIConstants.K_ACTION_SYNC);
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }

    public void fetchAssetMaster(String METHOD_NAME, String progress_message, String action) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        showProgress(context, progress_message);
        Log.e("ASSETMASTERURL", SharedPreferencesManager.getHostUrl(context) + METHOD_NAME );
        //Log.e("ASSETMASTERREQ", request.toString() );
        //AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME + SharedPreferencesManager.getCustomerId(context)).addJSONObjectBody(request)
        AndroidNetworking.get(SharedPreferencesManager.getHostUrl(context) + METHOD_NAME)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {

                        if (result != null) {
                            try {
                                hideProgressDialog();
                                Log.e("ASSETMASTERRESULT", result.toString());
                                if (result.has(APIConstants.K_STATUS)) {
                                    if (result.getString(APIConstants.K_STATUS).equalsIgnoreCase("true")) {
                                        JSONArray dataArray;
                                        if (result.has(APIConstants.K_DATA)) {
                                            dataArray = result.getJSONArray(APIConstants.K_DATA);
                                            if (dataArray != null) {
                                                if (dataArray.length() > 0) {
                                                    parseMasterFetchAndDoAction(dataArray, action);
                                                } else {
                                                    AssetUtils.showCommonBottomSheetErrorDialog(context, "No Asset Master Found");
                                                }
                                            } else {
                                                AssetUtils.showCommonBottomSheetErrorDialog(context, "No Asset Master Found");
                                            }
                                        }

                                    } else {
                                        String message = result.getString(APIConstants.K_MESSAGE);
                                        AssetUtils.showCommonBottomSheetErrorDialog(context, message);
                                    }
                                }
                            } catch (JSONException e) {
                                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
                            }
                        } else {
                            hideProgressDialog();
                            // Toast.makeText(context,"Communication Error",Toast.LENGTH_SHORT).show();
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

                    }
                });
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
                if (action.equals("PalletContainerMap")) {
                        Intent inventoryIntent = new Intent(DashboardActivity.this, PalletContainerMappingActivity.class);
                        startActivity(inventoryIntent);
                } else if (action.equals("AssetPalletMap")) {
                    Intent inventoryIntent = new Intent(DashboardActivity.this, AssetPalletMappingActivity.class);
                    startActivity(inventoryIntent);
                }

            }
        });
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfirmationDialog.dismiss();
                hideProgressDialog();
            }
        });
        // customConfirmationDialog.getWindow().getAttributes().windowAnimations = R.style.SlideBottomUpAnimation;
        customConfirmationDialog.show();
    }
    private void parseMasterFetchAndDoAction(JSONArray result, String action) {

        List<ProductMaster> list = new ArrayList<>();
        if (result.length() > 0) {
            try {
                for (int i = 0; i < result.length(); i++) {
                    ProductMaster assetMaster = new ProductMaster();
                    JSONObject jsonObject = result.getJSONObject(i);

                    if (jsonObject.has(APIConstants.K_PRODUCT_ID)) {
                        String assetid = jsonObject.getString(APIConstants.K_PRODUCT_ID).trim();
                        assetMaster.setProductTagId(assetid);
                    }

                    if (jsonObject.has(APIConstants.K_PRODUCT_NAME)) {
                        String assetname = jsonObject.getString(APIConstants.K_PRODUCT_NAME).trim();
                        assetMaster.setProductName(assetname);
                    }

                    if (jsonObject.has(APIConstants.K_PRODUCT_TYPE)) {
                        String isregistered = jsonObject.getString(APIConstants.K_PRODUCT_TYPE).trim();
                        assetMaster.setProductType(isregistered);
                    }
                    list.add(assetMaster);
                }
                if(list.size()>0){
                    db.deleteProductmaster();
                    db.storeProductMaster(list);
                }

                if (action.equalsIgnoreCase(APIConstants.K_ACTION_SYNC)) {
                    AssetUtils.showCommonBottomSheetSuccessDialog(context, "Asset Sync Done Successfully");
                }

            } catch (JSONException e) {
                AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.something_went_wrong_error));
            }
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context, "No Asset Master Found");
        }
    }
    //19020019140C002418800310
    private BatteryStatusHelper getBatteryStatus() {
        BatteryStatusHelper batteryStatusHelper = new BatteryStatusHelper();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, filter);
        batteryStatusHelper.level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        batteryStatusHelper.scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryStatusHelper.charging = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
        batteryStatusHelper.usbCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) == BatteryManager.BATTERY_PLUGGED_USB;
        batteryStatusHelper.acCharging = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) == BatteryManager.BATTERY_PLUGGED_AC;
        batteryStatusHelper.voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        batteryStatusHelper.temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        batteryStatusHelper.technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        return batteryStatusHelper;
    }
}