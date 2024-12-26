package com.psl.pallettracking;

import static com.psl.pallettracking.helper.AssetUtils.hideProgressDialog;
import static com.psl.pallettracking.helper.AssetUtils.showProgress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pallettracking.adapters.DashboardModel;
import com.psl.pallettracking.database.AssetMaster;
import com.psl.pallettracking.helper.APIConstants;
import com.psl.pallettracking.helper.AssetUtils;
import com.psl.pallettracking.helper.ConnectionDetector;
import com.psl.pallettracking.helper.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class URLConfigActivity extends AppCompatActivity {

    private Context context = this;
    private String HOST_URL;
    private boolean host_config = false;

    private EditText edtUrl;
    private Button btnConfig,btnClear2;
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urlconfig);
        setTitle("URL Configuration");
        getSupportActionBar().hide();

        findViews();
        cd = new ConnectionDetector(context);

        HOST_URL = SharedPreferencesManager.getHostUrl(context);
        host_config = SharedPreferencesManager.getIsHostConfig(context);

        if(host_config){
            edtUrl.setText(HOST_URL);
        }else{
            edtUrl.setText(SharedPreferencesManager.getDefaultHostUrl(context));
        }
        btnClear2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtUrl.setText(HOST_URL);
                finish();
            }
        });

        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtUrl.getText().toString().length()<8){
                    AssetUtils.showCommonBottomSheetErrorDialog(context,getResources().getString(R.string.enter_config_url));
                }else{
                    if(cd.isConnectingToInternet()) {
                        String url = edtUrl.getText().toString().trim();
                        try{
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(APIConstants.K_USER,"");
                            jsonObject.put(APIConstants.K_PASSWORD,"");
                            jsonObject.put(APIConstants.K_DEVICE_ID,"");
                            GetAccessToken(context, jsonObject,APIConstants.M_USER_LOGIN,"Please wait...\n" + "URL Validation is in progress",url);

                        }catch (JSONException e){

                        }
                    }else{
                        AssetUtils.showCommonBottomSheetErrorDialog(context,getResources().getString(R.string.internet_error));
                    }
                }
            }
        });

    }

    private void findViews() {
        edtUrl = (EditText)findViewById( R.id.edtUrl );
        btnClear2 = (Button)findViewById( R.id.btnClear2);
        btnConfig = (Button)findViewById( R.id.btnConfig);

    }

    Dialog dialog,dialog2;
    public void showCustomErrorDialog(Context context, String msg) {
        if(dialog!=null){
            dialog.dismiss();
        }
        if(dialog2!=null){
            dialog2.dismiss();
        }
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_alert_dialog_layout);
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * method to GET ACCESS TOKEN
     * */
    public  void GetAccessToken(final Context context,JSONObject jsonObject, String METHOD_NAME, String progress_message,final String url) {
        showProgress(context,progress_message);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(APIConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Log.e("URL",url + METHOD_NAME);
        AndroidNetworking.post(url+ METHOD_NAME).addJSONObjectBody(jsonObject)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        hideProgressDialog();
                        parseJson(result,null,url);
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        Log.e("ERROR",anError.getErrorDetail());
//                        if (BuildConfig.DEBUG) {
//                            // do something for a debug build
//                            try {
//                                parseJson(new JSONObject(AssetUtils.getJsonFromAssets(context,"loginres.json")),new JSONObject(AssetUtils.getJsonFromAssets(context,"loginreq.json")),HOST_URL);
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

    public  void showCustomSuccessDialog(Context context, String msg){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_alert_success_dialog_layout);
        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    Dialog dialogsuccess;
    public  void showCustomSuccessConfirmationDialog(final Context context, String msg,final String url){
        if(dialogsuccess!=null){
            dialogsuccess.dismiss();
        }
        dialogsuccess = new Dialog(context);
        dialogsuccess.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogsuccess.setCancelable(false);
        dialogsuccess.setContentView(R.layout.custom_alert_success_confirmation_dialog_layout);
        TextView text = (TextView) dialogsuccess.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) dialogsuccess.findViewById(R.id.btn_dialog);
        Button dialogCancel = (Button) dialogsuccess.findViewById(R.id.btnCancel);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsuccess.dismiss();
                SharedPreferencesManager.setIsHostConfig(context,true);
                SharedPreferencesManager.setHostUrl(context,url);
                HOST_URL = url;
                Log.e("URL",url);
                Log.e("SHAREDURL",SharedPreferencesManager.getHostUrl(context));
                showCustomSuccessDialog(context,getResources().getString(R.string.url_config_success));
            }
        });
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsuccess.dismiss();

            }
        });
        dialogsuccess.show();
    }
    private void parseJson(JSONObject result,JSONObject loginRequestObject,String url){
        if (result != null) {
            Log.e("RES",result.toString());
            showCustomSuccessConfirmationDialog(context,getResources().getString(R.string.url_validation),url);
        } else {
            AssetUtils.showCommonBottomSheetErrorDialog(context,getResources().getString(R.string.communication_error));
        }
    }
}