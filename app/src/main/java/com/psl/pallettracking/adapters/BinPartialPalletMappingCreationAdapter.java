package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psl.pallettracking.BinPartialPalletMappingActivity;
import com.psl.pallettracking.DashboardActivity;
import com.psl.pallettracking.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinPartialPalletMappingCreationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<BinPartialPalletMappingAdapterModel> orderList;
    public BinPartialPalletMappingCreationAdapter(Context context, List<BinPartialPalletMappingAdapterModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    // ViewHolder for items
    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Item views
        TextView textSrNo;
        TextView textWorkOrderNo;
        TextView textWorkorderType;
        Button btnProceed;

        ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize item views
            textSrNo = itemView.findViewById(R.id.textSrNo);
            textWorkOrderNo = itemView.findViewById(R.id.textWorkOrderNo);
            textWorkorderType = itemView.findViewById(R.id.textWorkorderType);
            btnProceed = itemView.findViewById(R.id.btnProceed);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.bin_partial_pallet_mapping_workorder_adapter, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        BinPartialPalletMappingAdapterModel order = orderList.get(position);
        // Bind item data
        int pos  = position+1;
        itemViewHolder.textSrNo.setText(""+pos);
        itemViewHolder.textWorkOrderNo.setText(order.getDRN());
        itemViewHolder.textWorkorderType.setText(order.getWorkOrderStatus());

        String status = order.getWorkOrderStatus();
        if (status.equalsIgnoreCase("UA")) {
            itemViewHolder.itemView.setVisibility(View.VISIBLE);
            itemViewHolder.textWorkorderType.setText("L0");
            itemViewHolder.btnProceed.setBackground(context.getResources().getDrawable(R.drawable.round_button_green));
        }

        if ((position) % 2 == 0) {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
        } else {
            itemViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lemonyellow));
        }
        itemViewHolder.btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context != null && context instanceof BinPartialPalletMappingActivity) {
                    ((BinPartialPalletMappingActivity) context).onBinPartialPalletMappingWorkOrderListItemClicked(order);
                }
            }
        });
    }

    public int getItemCount() {
        return orderList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

}
