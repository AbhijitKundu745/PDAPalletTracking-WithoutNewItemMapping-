package com.psl.pallettracking.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.psl.pallettracking.AssetPalletMappingActivity;
import com.psl.pallettracking.ItemMovementActivity;
import com.psl.pallettracking.R;
import com.psl.pallettracking.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class AssetPalletMapAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<HashMap<String, String>> tagList;
    private Context mContext;
    DatabaseHandler db;

    public AssetPalletMapAdapter(Context context, ArrayList<HashMap<String, String>> tagList) {
        this.mInflater = LayoutInflater.from(context);
        this.tagList = tagList;
        this.mContext = context;
        db = new DatabaseHandler(mContext);
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return tagList.size();
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return tagList.get(arg0);
    }

    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AssetPalletMapAdapter.ViewHolder holder = null;
        if (convertView == null) {
            holder = new AssetPalletMapAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.asset_pallet_map_adapter, null);
            holder.textAssetName = (TextView) convertView.findViewById(R.id.textAssetName);

            convertView.setTag(holder);
        } else {
            holder = (AssetPalletMapAdapter.ViewHolder) convertView.getTag();
        }
        String tag = tagList.get(position).get("ASSETNAME").toString();
        String[] parts;
        String SerialNo = "";
        String skuCode = "";
        String ItemName = "";
        if (tag.contains(",")) {
            parts = tag.split("[,]+");
            skuCode = parts[1].trim().replaceAll("^0*", "");
            SerialNo = parts[0].trim().replaceAll("^0*", "");
            ItemName = db.getItemNameByItemCode(skuCode);
            Log.e("Ite", ItemName);
            Log.e("SKU", skuCode);
        } else if (tag.contains(" ")) {
            parts = tag.split("\\s+");
            skuCode = parts[2].trim();
            SerialNo = parts[1].trim();
            ItemName = db.getItemNameByItemCode(skuCode);
            Log.e("Ite", ItemName);
            Log.e("SKU", skuCode);
        }


        holder.textAssetName.setText(SerialNo+","+ItemName+"("+skuCode+")");


        if (tagList.get(position).get("STATUS").equalsIgnoreCase("Damage")) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.red6));
        } else{
            if (position % 2 != 0) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.cyan1));
            } else {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lemonyellow));
            }
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((AssetPalletMappingActivity) mContext).onListItemClicked(tagList.get(position));

            }
        });
        return convertView;
    }

    public void setSelectItem(int select) {
        if (selectItem == select) {
            selectItem = -1;

        } else {
            selectItem = select;

        }

    }

    private int selectItem = -1;

    public final class ViewHolder {
        public TextView textAssetName;

    }

}