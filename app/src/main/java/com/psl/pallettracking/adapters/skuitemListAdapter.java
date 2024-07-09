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
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import java.util.List;

public class skuitemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
   private List<ItemDetailsList> itemsList;
    public skuitemListAdapter(Context context, List<ItemDetailsList> items) {
        this.context = context;
        this.itemsList = items;
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView itemSerialNo;
        TextView itemSKU;
        TextView itemPickedQty;
        TextView itemScannedQty;
        TextView itemRemainingQty;

        ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize item views
            itemSerialNo = itemView.findViewById(R.id.itemSerialNo);
            itemSKU = itemView.findViewById(R.id.itemSKU);
            itemPickedQty = itemView.findViewById(R.id.itemPickedQty);
            itemScannedQty= itemView.findViewById(R.id.itemScannedQty);
            itemRemainingQty= itemView.findViewById(R.id.itemRemainingQty);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_list_view, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        ItemDetailsList order = itemsList.get(position);
        // Bind item data
        int pos= position+1;
        itemViewHolder.itemSerialNo.setText(""+pos);
        itemViewHolder.itemSKU.setText(order.getItemDesc());
        itemViewHolder.itemSKU.setSelected(true);
        itemViewHolder.itemPickedQty.setText(order.getPickedQty());
        itemViewHolder.itemPickedQty.setSelected(true);
        itemViewHolder.itemScannedQty.setText(order.getScannedQty());
        itemViewHolder.itemScannedQty.setSelected(true);
        itemViewHolder.itemRemainingQty.setText(order.getRemainingQty());
        itemViewHolder.itemRemainingQty.setSelected(true);
        Log.e("itemSKU", order.getSkuCode());
        Log.e("itemPickd", order.getPickedQty());

        if ((position) % 2 == 0) {
            itemViewHolder.itemSerialNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemRemainingQty.setTextColor(context.getResources().getColor(R.color.wine));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red3));
        } else {
            itemViewHolder.itemSerialNo.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemRemainingQty.setTextColor(context.getResources().getColor(R.color.wine));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.green1));
        }
    }

    public int getItemCount() {
        return itemsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

}