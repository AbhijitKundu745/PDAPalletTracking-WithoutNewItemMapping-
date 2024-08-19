package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.psl.pallettracking.AssetInventoryActivity;
import com.psl.pallettracking.R;

import java.util.ArrayList;
import java.util.HashMap;

import static com.psl.pallettracking.helper.AppConstants.ASSET_COUNT;
import static com.psl.pallettracking.helper.AppConstants.ASSET_TYPE_NAME;


public class InventoryAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public ArrayList<HashMap<String, String>> tagList;
    private Context mContext;

    public InventoryAdapter(Context context,ArrayList<HashMap<String, String>> tagList) {
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
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.inventory_adapter_layout, null);
            holder.textType = (TextView) convertView.findViewById(R.id.textType);
            holder.textCount = (TextView) convertView.findViewById(R.id.textCount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textType.setText((String) tagList.get(position).get(ASSET_TYPE_NAME));
        holder.textCount.setText((String) tagList.get(position).get(ASSET_COUNT));

       /* if (position == selectItem) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lighter_grey));
        }
        else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
*/
        if (position%2!=0) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.cyan1));
        }
        else {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.lemonyellow));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AssetInventoryActivity)mContext).onListItemClicked(tagList.get(position).get(ASSET_TYPE_NAME));
            }
        });
        return convertView;
    }
    public  void setSelectItem(int select) {
        if(selectItem==select){
            selectItem=-1;

        }else {
            selectItem = select;

        }

    }
    private int  selectItem=-1;
    public final class ViewHolder {
        public TextView textType;
        public TextView textCount;

    }

}
