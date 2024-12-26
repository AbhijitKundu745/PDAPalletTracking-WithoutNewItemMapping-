package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.psl.pallettracking.PalletMovementActivity;
import com.psl.pallettracking.R;
import com.psl.pallettracking.bean.WorkOrderListItem;
import com.psl.pallettracking.viewHolder.OrderDetails;

import java.util.List;

public class WorkOrderDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<WorkOrderListItem> orderDetailList;
    private String orderType;
    public WorkOrderDetailsAdapter(Context context, List<WorkOrderListItem> orderDetailList, String orderType) {
        this.context = context;
        this.orderDetailList = orderDetailList;
        this.orderType = orderType;
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView itemSerialNo;
        TextView itemPalletNo;
        TextView itemPickup;
        TextView itemWOType;
        TextView itemStatus;

        ItemViewHolder(View itemView) {
            super(itemView);
            //Initialize item views
            itemSerialNo = itemView.findViewById(R.id.itemSerialNo);
            itemPalletNo = itemView.findViewById(R.id.itemPalletNo);
            itemPickup = itemView.findViewById(R.id.itemPickup);
            itemWOType= itemView.findViewById(R.id.itemWOtype);
            itemStatus = itemView.findViewById(R.id.itemStatus);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_loading_unloading, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        WorkOrderListItem order = orderDetailList.get(position);
        // Bind item data
        int pos= position+1;
        itemViewHolder.itemSerialNo.setText(""+pos);
        itemViewHolder.itemPalletNo.setText(order.getPalletName());
        itemViewHolder.itemPalletNo.setSelected(true);
        switch (order.getWorkorderType()){
            case "":
                break;
            case "U0":
                itemViewHolder.itemPickup.setText(order.getDestinationLocationName());
                itemViewHolder.itemPickup.setSelected(true);
                break;
            case "U1":
                itemViewHolder.itemPickup.setText(order.getDestinationLocationName());
                itemViewHolder.itemPickup.setSelected(true);
                break;
            case "L0":
                itemViewHolder.itemPickup.setText(order.getBinLocationName());
                itemViewHolder.itemPickup.setSelected(true);
                break;
            case "L1":
                itemViewHolder.itemPickup.setText(order.getDestinationLocationName());
                itemViewHolder.itemPickup.setSelected(true);
                break;
            case "I0":
                itemViewHolder.itemPickup.setText(order.getLoadingAreaName());
                itemViewHolder.itemPickup.setSelected(true);
                break;
        }

        itemViewHolder.itemWOType.setText(order.getWorkorderType());
        itemViewHolder.itemWOType.setSelected(true);
        itemViewHolder.itemStatus.setText(order.getListItemStatus());
        itemViewHolder.itemStatus.setSelected(true);
        if ("Completed".equals(order.getListItemStatus())) {
            itemViewHolder.itemStatus.setTextColor(context.getResources().getColor(R.color.green));
        }//changed


        switch (orderType){
            case "U0":
                itemViewHolder.itemSerialNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPalletNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPickup.setVisibility(View.VISIBLE);
                itemViewHolder.itemWOType.setVisibility(View.VISIBLE);
                itemViewHolder.itemStatus.setVisibility(View.VISIBLE);
                break;
            case "U1":
                itemViewHolder.itemSerialNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPalletNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPickup.setVisibility(View.VISIBLE);
                itemViewHolder.itemWOType.setVisibility(View.VISIBLE);
                itemViewHolder.itemStatus.setVisibility(View.VISIBLE);
                break;
            case "L0":
                itemViewHolder.itemSerialNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPalletNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPickup.setVisibility(View.VISIBLE);
                itemViewHolder.itemWOType.setVisibility(View.VISIBLE);
                itemViewHolder.itemStatus.setVisibility(View.VISIBLE);
                break;
            case "L1":
                itemViewHolder.itemSerialNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPalletNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPickup.setVisibility(View.VISIBLE);
                itemViewHolder.itemWOType.setVisibility(View.VISIBLE);
                itemViewHolder.itemStatus.setVisibility(View.VISIBLE);
                break;
            case "I0":
                itemViewHolder.itemSerialNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPalletNo.setVisibility(View.VISIBLE);
                itemViewHolder.itemPickup.setVisibility(View.VISIBLE);
                itemViewHolder.itemWOType.setVisibility(View.VISIBLE);
                itemViewHolder.itemStatus.setVisibility(View.VISIBLE);
                break;
        }
        if(order.getWorkorderType().equalsIgnoreCase("I0")){
            itemViewHolder.itemSerialNo.setTextColor(context.getResources().getColor(R.color.white));
            itemViewHolder.itemPalletNo.setTextColor(context.getResources().getColor(R.color.white));
            itemViewHolder.itemPickup.setTextColor(context.getResources().getColor(R.color.white));
            itemViewHolder.itemWOType.setTextColor(context.getResources().getColor(R.color.white));
            itemViewHolder.itemStatus.setTextColor(context.getResources().getColor(R.color.white));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.orange));
        } else if ((position) % 2 == 0) {
            itemViewHolder.itemSerialNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPalletNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickup.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemWOType.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemStatus.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
        } else {
            itemViewHolder.itemSerialNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPalletNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickup.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemWOType.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemStatus.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lemonyellow));
        }
    }

    public int getItemCount() {
        return orderDetailList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

}
