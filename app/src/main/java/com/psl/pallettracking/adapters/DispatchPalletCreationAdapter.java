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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DispatchPalletCreationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private static List<ItemDetailsList> orderList;
    private List<ItemDetailsList> filteredList;
    private static OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    public DispatchPalletCreationAdapter(Context context, List<ItemDetailsList> orderList) {
        this.context = context;
        this.orderList = orderList;
        this.filteredList = new ArrayList<>(orderList);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
//                            BinPartialPalletMappingCreationProcessModel order = orderList.get(position);
//                            if (order.getClickedEnable()) {
//                                listener.onItemClick(itemView, position);
//                            }
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
        View itemView = inflater.inflate(R.layout.bin_partial_pallet_mapping_workorder_process_adapter, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Log.d("Adapter", "Binding item at position: " + position);
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        ItemDetailsList order = orderList.get(position);
        // Bind item data
        int pos  = position+1;
        itemViewHolder.textItemDesc.setText(order.getItemDesc());
        itemViewHolder.textItemDesc.setSelected(true);
        itemViewHolder.textBinName.setText(order.getBinName());
        itemViewHolder.textBinName.setSelected(true);
        itemViewHolder.textPickQty.setText(order.getPickedQty());
        itemViewHolder.textPickQty.setSelected(true);
//if(order.getClickedEnable()){
        if ((position) % 2 == 0) {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
        } else {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lemonyellow));
        }
//} else{
//    itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.text_gray));
//}
    }
    public void filter(String searchText) {
        filteredList.clear();
        if (searchText.isEmpty()) {
            filteredList.addAll(orderList);
        } else {
            for (ItemDetailsList item : orderList) {
                if (item.getItemDesc().toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void filterList(List<ItemDetailsList> filteredList) {
        this.orderList = new ArrayList<>(filteredList);
        this.filteredList = new ArrayList<>(filteredList);
        notifyDataSetChanged();
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
