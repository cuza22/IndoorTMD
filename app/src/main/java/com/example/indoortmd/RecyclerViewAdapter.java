package com.example.indoortmd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<RecyclerViewItem> mData;
    private Context context;

    public RecyclerViewAdapter(ArrayList<RecyclerViewItem> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: onCreateViewHolder
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recycler_item, parent, false);
        ViewHolder vh = new ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecyclerViewItem item = mData.get(position);

        holder.ip.setText(item.getId());
        holder.mac.setText(item.getMac());
        holder.rssi.setText(item.getRssi());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ip;
        public TextView mac;
        public TextView rssi;

        public ViewHolder(View itemView) {
            super(itemView);

            ip = itemView.findViewById(R.id.ip);
            mac = itemView.findViewById(R.id.mac);
            rssi = itemView.findViewById(R.id.rssi);
        }
    }
}
