package com.psl.pallettracking.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psl.pallettracking.R;
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class skuitemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
   private List<ItemDetailsList> itemsList, filteredList;
    private static OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    public skuitemListAdapter(Context context, List<ItemDetailsList> items) {
        this.context = context;
        this.itemsList = items;
        this.filteredList = new ArrayList<>(items);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView itemSKU;
        TextView itemPickedQty;
        TextView itemScannedQty;
        TextView itemRemainingQty;

        ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize item views
            itemSKU = itemView.findViewById(R.id.itemSKU);
            itemPickedQty = itemView.findViewById(R.id.itemPickedQty);
            itemScannedQty = itemView.findViewById(R.id.itemScannedQty);
            itemRemainingQty = itemView.findViewById(R.id.itemRemainingQty);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
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
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemRemainingQty.setTextColor(context.getResources().getColor(R.color.wine));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red3));
        } else {
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemPickedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemRemainingQty.setTextColor(context.getResources().getColor(R.color.wine));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.green1));
        }
    }
    public void filter(String searchText) {
        filteredList.clear();
        if (searchText.isEmpty()) {
            filteredList.addAll(itemsList);
        } else {
            for (ItemDetailsList item : itemsList) {
                if (item.getItemDesc().toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void filterList(List<ItemDetailsList> filteredList) {
        this.itemsList = new ArrayList<>(filteredList);
        this.filteredList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
    }
    public int getItemCount() {
        return itemsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

}