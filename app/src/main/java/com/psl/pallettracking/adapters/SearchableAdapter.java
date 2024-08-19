package com.psl.pallettracking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.psl.pallettracking.R;

import java.util.ArrayList;
import java.util.List;

public class SearchableAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private List<String> originalData;
    private List<String> filteredData;

    public SearchableAdapter(Context context, List<String> data) {
        this.context = context;
        this.originalData = data;
        this.filteredData = data;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            if (position % 2 != 0) {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.cyan1));
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.lemonyellow));
            }
        }

        String item = filteredData.get(position);

        // Customize the view as needed
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(item);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterString = charSequence.toString().toLowerCase();

                FilterResults results = new FilterResults();

                final List<String> list = originalData;

                int count = list.size();
                final ArrayList<String> nlist = new ArrayList<>(count);

                for (String item : list) {
                    if (item.toLowerCase().contains(filterString)) {
                        nlist.add(item);
                    }
                }

                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
