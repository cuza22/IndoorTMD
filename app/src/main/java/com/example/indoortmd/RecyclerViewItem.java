package com.example.indoortmd;

public class RecyclerViewItem {
    private String id;
    private String mac;
    private int rssi;

    public void setId(String id) {
        this.id = id;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public void setRssi(int rssi) { this.rssi = rssi; }

    public String getId() {
        return id;
    }
    public String getMac() {
        return mac;
    }
    public int getRssi() { return rssi; }
}
