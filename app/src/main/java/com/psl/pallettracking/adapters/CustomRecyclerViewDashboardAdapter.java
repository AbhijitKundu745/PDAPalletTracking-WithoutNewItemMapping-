package com.psl.pallettracking.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.psl.pallettracking.DashboardActivity;
import com.psl.pallettracking.R;
import com.psl.pallettracking.helper.AppConstants;

import java.util.List;

public class CustomRecyclerViewDashboardAdapter extends RecyclerView.Adapter {
    List<DashboardModel> dashboardModelList;
    //ArrayList personNames;
    //ArrayList personImages;
    Context context;
    public CustomRecyclerViewDashboardAdapter(Context context,  List<DashboardModel> dashboardModelList) {
        this.context = context;
        //this.personNames = personNames;
        //this.personImages = personImages;
        this.dashboardModelList = dashboardModelList;
        Log.e("SIZZZZ",""+this.dashboardModelList.size());
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_row_layout_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // set the data in items
        MyViewHolder viewHolder= (MyViewHolder)holder;
        ((MyViewHolder) holder).name.setText( dashboardModelList.get(position).getMenuName().toString());

        //((MyViewHolder) holder).image.setImageResource((Integer) dashboardModelList.get(position).getDrawableImage());

        String menu_id = dashboardModelList.get(position).getMenuId();
        String image = dashboardModelList.get(position).getMenuimageName();
        switch (menu_id){
            case AppConstants.MENU_ID_CARTON_PALLET_MAPPING:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.inwardpallet).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_ITEM_PALLET_MAPPING:
                    Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.outwardpallet).override(100,100)).into(((MyViewHolder) holder).image);
                    break;
            case AppConstants.MENU_ID_PALLET_MOVEMENT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.palletmovement).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_CONTAINER_PALLET_MAPPING:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.mapping).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_PARTIAL:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.dispatchpalletcreation).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_ITEM_MOVEMENT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.itemmovement).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_INVENTORY:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.inventory).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_SEARCH:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.search).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_CHECKIN:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.checkin).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_CHECKOUT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.checkout).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_ROOMCHECKOUT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.checkout).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_ASSETSYNC:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.sync).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_TRACKPOINT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.checkout).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_SECURITYOUT:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.checkout).override(100,100)).into(((MyViewHolder) holder).image);
                break;
            case AppConstants.MENU_ID_MAP_PARTIAL_PALLET:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.mapping).override(100,100)).into(((MyViewHolder) holder).image);
                break;
//            case AppConstants.MENU_ID_STOCK_RECORD:
//                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.track).override(100,100)).into(((MyViewHolder) holder).image);
//                break;
            default:
                Glide.with(context).load(image).apply(new RequestOptions().placeholder(R.drawable.inventory).override(100,100)).into(((MyViewHolder) holder).image);
                break;
        }

        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,"Person : "+(position+1),Toast.LENGTH_SHORT).show();
                if (context instanceof DashboardActivity) {
                    ((DashboardActivity) context).gridClicked(position,dashboardModelList.get(position).getMenuName().toString(),dashboardModelList.get(position).getMenuId().toString(),dashboardModelList.get(position).getIsMenuActive().toString());
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return dashboardModelList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView name;
        ImageView image;
        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}