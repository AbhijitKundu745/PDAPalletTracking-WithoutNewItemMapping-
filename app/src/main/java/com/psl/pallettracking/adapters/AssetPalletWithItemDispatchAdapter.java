package com.psl.pallettracking.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psl.pallettracking.AssetPalletMappingWithItemForDispatchActivity;
import com.psl.pallettracking.AsssetPalletMappingWithItemForDispatchV1Activity;
import com.psl.pallettracking.R;
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import java.util.List;

public class AssetPalletWithItemDispatchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ItemDetailsList> itemsList;

    public AssetPalletWithItemDispatchAdapter (Context context, List<ItemDetailsList> itemsList){
        this.context = context;
        this.itemsList = itemsList;
    }
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView itemSKU;
        TextView itemScannedQty;
        ImageView imgView;

        ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize item views
            itemSKU = itemView.findViewById(R.id.textAssetName);
            itemScannedQty= itemView.findViewById(R.id.textQty);
            imgView = itemView.findViewById(R.id.imgView);
            imgView.setVisibility(View.INVISIBLE);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.asset_pallet_map_without_qr_adapter, parent, false);
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
        itemViewHolder.itemScannedQty.setText(order.getScannedQty());
        itemViewHolder.itemScannedQty.setSelected(true);

        if ((position) % 2 == 0) {
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red3));
        } else {
            itemViewHolder.itemSKU.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemScannedQty.setTextColor(context.getResources().getColor(R.color.black));
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.green1));
        }
//        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ((AsssetPalletMappingWithItemForDispatchV1Activity) context).onListItemClicked(itemsList.get(position));
//            }
//        });

    }
//    public void updateDataset(List<ItemDetailsList> newList) {
//        itemsList.clear();
//        itemsList.addAll(newList);
//        notifyDataSetChanged();
//    }
    @Override
    public int getItemCount() {
        return itemsList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return 1;
    }
}
