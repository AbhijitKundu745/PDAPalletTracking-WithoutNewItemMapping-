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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.BinPartialPalletMappingAdapterModel;
import com.psl.pallettracking.adapters.BinPartialPalletMappingCreationAdapter;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.databinding.ActivityBinPartialPalletMappingBinding;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class BinPartialPalletMappingActivity extends AppCompatActivity {
    private BinPartialPalletMappingCreationAdapter adapter;
    private List<BinPartialPalletMappingAdapterModel> orderList;
    ActivityBinPartialPalletMappingBinding binding;
    Context context= this;
    private DatabaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bin_partial_pallet_mapping);
        setTitle("USER LOGIN");
        getSupportActionBar().hide();
        db = new DatabaseHandler(context);
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomConfirmationDialog("Are you sure you want to go back","BACK");
            }
        });
       // setContentView(R.layout.activity_bin_partial_pallet_mapping);
    }


    @Override
    protected void onResume() {
        super.onResume();
        binding.rvPallet.setLayoutManager(new GridLayoutManager(context, 1));
        if (orderList != null) {
            orderList.clear();
        }
        orderList = new ArrayList<>();
        adapter = new BinPartialPalletMappingCreationAdapter(context, orderList);
        binding.rvPallet.setAdapter(adapter);
        startWorkOrderPollingApiHandler();
    }

    @Override
    protected void onDestroy() {

        stopWorkOrderPollingApiHandler();
        super.onDestroy();
    }

    private Handler workOrderPollingApiHandler = new Handler();
    private Runnable workOrderPollingApiRunnable;

    private void startWorkOrderPollingApiHandler() {
        try {
            workOrderPollingApiRunnable = new Runnable() {
                @Override
                public void run() {
                    getPartialPalletWorkOrderList();
                    workOrderPollingApiHandler.postDelayed(this, 10000);
                }
            };
        } catch (Exception ex) {
            Log.e("HANDEXC", ex.getMessage());
        }
        // Post the initial Runnable with a delay of 2 seconds first time start handler after 1 seconds
        workOrderPollingApiHandler.postDelayed(workOrderPollingApiRunnable, 1000);
    }

    private void stopWorkOrderPollingApiHandler() {
        // Remove any pending callbacks and messages
        workOrderPollingApiHandler.removeCallbacks(workOrderPollingApiRunnable);
    }


    private void getPartialPalletWorkOrderList() {

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(APIConstants.K_DEVICE_ID, SharedPreferencesManager.getDeviceId(context));
            jsonObject.put(APIConstants.K_WAREHOUSE_ID, SharedPreferencesManager.getWarehouseId(context));
            Log.e("JSONReq", SharedPreferencesManager.getHostUrl(context)+ APIConstants.M_GET_PARTIAL_WORK_ORDERS);
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(SharedPreferencesManager.getHostUrl(context) + APIConstants.M_GET_PARTIAL_WORK_ORDERS).addJSONObjectBody(jsonObject)
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
                                    if(response.getBoolean("status")){
                                        JSONArray dataArray = response.getJSONArray("data");
                                        parseWorkDetailsObjectAndDoAction(dataArray);
                                    }else{
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

    private void parseWorkDetailsObjectAndDoAction(JSONArray dataArray) {
        if(orderList!=null){
            orderList.clear();

        }
        if(dataArray.length()>0){
            for(int i=0;i<dataArray.length();i++){
                try {
                    BinPartialPalletMappingAdapterModel binPartialPalletMappingAdapterModel = new BinPartialPalletMappingAdapterModel();
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    String workOrderNumber = dataObject.getString("WorkorderNumber");
                    String workOrderType = dataObject.getString("WorkorderType");
                    String DRN = dataObject.getString("DRN");
                    binPartialPalletMappingAdapterModel.setWorkOrdernumber(workOrderNumber);
                    binPartialPalletMappingAdapterModel.setDRN(DRN);
                    binPartialPalletMappingAdapterModel.setWorkOrderStatus(workOrderType);
                    orderList.add(binPartialPalletMappingAdapterModel);

                } catch (JSONException e) {
                    adapter.notifyDataSetChanged();
                    AssetUtils.showCommonBottomSheetErrorDialog(context, getResources().getString(R.string.internet_error));
                }
            }
        }
        Collections.sort(orderList, new Comparator<BinPartialPalletMappingAdapterModel>() {
            @Override
            public int compare(BinPartialPalletMappingAdapterModel o1, BinPartialPalletMappingAdapterModel o2) {
                return o1.getDRN().compareTo(o2.getDRN());
            }
        });

        adapter.notifyDataSetChanged();
    }


    public void onBinPartialPalletMappingWorkOrderListItemClicked(BinPartialPalletMappingAdapterModel order) {
        stopWorkOrderPollingApiHandler();
        Intent i = new Intent(BinPartialPalletMappingActivity.this, DispatchPalletCreationActivity.class);
        i.putExtra("WorkOrderNumber",order.getWorkOrdernumber());
        i.putExtra("WorkOrderType",order.getWorkOrderStatus());
        i.putExtra("DRN", order.getDRN());
        startActivity(i);
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
                if (action.equals("BACK")) {
                    hideProgressDialog();
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
    @Override
    public void onBackPressed() {
        showCustomConfirmationDialog("Are you sure you want to go back","BACK");
        //super.onBackPressed();
    }
}