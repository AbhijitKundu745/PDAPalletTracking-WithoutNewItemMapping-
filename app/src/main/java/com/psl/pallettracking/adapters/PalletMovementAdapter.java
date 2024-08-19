package com.psl.pallettracking.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psl.pallettracking.R;
import com.psl.pallettracking.viewHolder.OrderDetails;

import java.util.List;

public class PalletMovementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<OrderDetails> orderDetailList;
    private String orderType;
    public PalletMovementAdapter(Context context, List<OrderDetails> orderDetailList,String orderType) {
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
            // Initialize item views
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
        View itemView = inflater.inflate(R.layout.item_pallet_info, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        PalletMovementAdapter.ItemViewHolder itemViewHolder = (PalletMovementAdapter.ItemViewHolder) viewHolder;
        OrderDetails order = orderDetailList.get(position);
        // Bind item data
        int pos= position+1;
        itemViewHolder.itemSerialNo.setText(""+pos);
        itemViewHolder.itemPalletNo.setText(order.getPalletNumber());
        itemViewHolder.itemPalletNo.setSelected(true);
        itemViewHolder.itemPickup.setText(order.getPickupLocation());
        itemViewHolder.itemPickup.setSelected(true);
        Log.e("Pickup Location",order.getPickupLocation());
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
        }

        if ((position) % 2 == 0) {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
        } else {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red5));
        }
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return 1;
    }
}
