package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psl.pallettracking.R;
import com.psl.pallettracking.viewHolder.ItemDetailsList;

import java.util.List;

public class DispatchPalletPickedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<ItemDetailsList> orderList;
    public DispatchPalletPickedAdapter(Context context, List<ItemDetailsList> orderList) {
        this.context = context;
        this.orderList = orderList;
    }
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView textItemDesc;
        TextView textBinName;
        TextView textPickQty;


        ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize item views
            textItemDesc = itemView.findViewById(R.id.textItemDesc);
            textBinName = itemView.findViewById(R.id.textBinName);
            textPickQty = itemView.findViewById(R.id.textPickQty);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.bin_partial_pallet_mapping_workorder_process_adapter, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        ItemDetailsList order = orderList.get(position);
        // Bind item data
        itemViewHolder.textItemDesc.setText(order.getItemDesc());
        itemViewHolder.textItemDesc.setSelected(true);
        itemViewHolder.textBinName.setText(order.getBinName());
        itemViewHolder.textBinName.setSelected(true);
        itemViewHolder.textPickQty.setText(order.getPickedQty());

        if ((position) % 2 == 0) {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
        } else {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lemonyellow));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return 1;
    }
}
