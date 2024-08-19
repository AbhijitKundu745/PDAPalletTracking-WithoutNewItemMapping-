package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.psl.pallettracking.AssetInventoryActivity;
import com.psl.pallettracking.R;
import com.psl.pallettracking.database.DatabaseHandler;
import com.psl.pallettracking.helper.AssetUtils;

import java.util.List;

public class InventoryDetailsAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    public List<String> tagList;
    private Context mContext;
    private String name;
    private DatabaseHandler db;

    public InventoryDetailsAdapter(Context context, List<String> tagList,String name) {
        this.mInflater = LayoutInflater.from(context);
        this.tagList = tagList;
        this.name = name;
        this.mContext = context;
        db = new DatabaseHandler(context);
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
            convertView = mInflater.inflate(R.layout.inventory_details_adapter_layout, null);
            holder.textTagId = (TextView) convertView.findViewById(R.id.textTagId);
            holder.textName = (TextView) convertView.findViewById(R.id.textName);
            holder.btnRemove = (Button) convertView.findViewById(R.id.btnRemove);

            holder.textName.setSelected(true);
            holder.textTagId.setSelected(true);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

       // holder.textTagId.setVisibility(View.GONE);
        //holder.textTagId.setText(tagList.get(position));
        String serialnumber = tagList.get(position).substring(4,12);
        String assetname = db.getAssetNameByAssetSerialNumber(serialnumber);
        holder.textName.setText(assetname);
        holder.textTagId.setText(AssetUtils.hexToNumber(serialnumber));

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

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AssetInventoryActivity)mContext).onItemRemovedClicked(name,tagList.get(position));
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
        public TextView textTagId;
        public TextView textName;
        public Button btnRemove;

    }

}
