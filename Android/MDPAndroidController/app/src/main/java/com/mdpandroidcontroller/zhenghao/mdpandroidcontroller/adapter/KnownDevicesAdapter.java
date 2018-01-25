package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Zhenghao on 25/1/18.
 */

public class KnownDevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private static final String TAG = "KnownDevicesAdapter";

    private List<String> deviceNameList;

    public KnownDevicesAdapter(List<String> deviceNameList) {
        this.deviceNameList = deviceNameList;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
