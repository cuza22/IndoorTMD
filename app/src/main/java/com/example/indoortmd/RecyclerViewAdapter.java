package com.example.indoortmd;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<ScanResult> mData;
    private Context context;

    public RecyclerViewAdapter(List<ScanResult> mData, Context context) {
        this.mData = mData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        RecyclerViewItem item = mData.get(position);

        holder.setItem(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView ip;
        public TextView mac;
        public TextView rssi;
        public TextView capabilities;

        public ViewHolder(View itemView) {
            super(itemView);

            ip = itemView.findViewById(R.id.ip);
            mac = itemView.findViewById(R.id.mac);
            rssi = itemView.findViewById(R.id.rssi);
            capabilities = itemView.findViewById(R.id.capabilities);
        }

        public void setItem(ScanResult item) {
            ip.setText("ID: " + String.valueOf(item.SSID));
            mac.setText("MAC: " + String.valueOf(item.BSSID));
            rssi.setText("RSSI: " + String.valueOf(item.level));
            capabilities.setText(String.valueOf(item.capabilities));
        }
    }

}
