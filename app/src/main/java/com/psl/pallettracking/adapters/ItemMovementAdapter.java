package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.psl.pallettracking.AssetPalletMappingActivity;
import com.psl.pallettracking.ItemMovementActivity;
import com.psl.pallettracking.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemMovementAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<HashMap<String, String>> tagList;
    private Context mContext;

    public ItemMovementAdapter(Context context, ArrayList<HashMap<String, String>> tagList) {
        this.mInflater = LayoutInflater.from(context);
        this.tagList = tagList;
        this.mContext = context;
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
        ItemMovementAdapter.ViewHolder holder = null;
        if (convertView == null) {
            holder = new ItemMovementAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.asset_pallet_map_adapter, null);
            holder.textAssetName = (TextView) convertView.findViewById(R.id.textAssetName);

            convertView.setTag(holder);
        } else {
            holder = (ItemMovementAdapter.ViewHolder) convertView.getTag();
        }

        holder.textAssetName.setText((String) tagList.get(position).get("ASSETNAME"));
        if (position % 2 != 0) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.cyan1));
        } else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lemonyellow));
        }
        if (tagList.get(position).get("STATUS").equalsIgnoreCase("false")) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ItemMovementActivity) mContext).onListItemClicked(tagList.get(position));

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
