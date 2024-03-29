package com.jpmunz.betterreviewed;

import android.content.Context;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

public class RottenTomatoesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> resultList;
    private RottenTomatoesAPI mApi;

    public RottenTomatoesAutoCompleteAdapter(Context context, int textViewResourceId, RottenTomatoesAPI api) {
        super(context, textViewResourceId);
        mApi = api;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    public boolean hasValue(String value) {
        return resultList.contains(value);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Filter.FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    try {
                        resultList = mApi.autoComplete(constraint.toString());
                    } catch (RottenTomatoesAPI.RottenTomatoesAPIException e) {
                        e.printStackTrace();
                    }

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }
}