package com.example.chinnu.iss;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Chinnu on 12/27/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>  {

     ArrayList<DataModel> list;
    Context context;

    public RecyclerAdapter(ArrayList<DataModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
holder.tvstamp.setText(list.get(position).getRisestamp());
holder.tvduration.setText(list.get(position).getDuration());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvduration,  tvstamp;
        public ViewHolder(View itemView) {
            super(itemView);

            tvduration = (TextView) itemView.findViewById(R.id.duration);
            tvstamp = (TextView) itemView.findViewById(R.id.time);

        }
    }
}
