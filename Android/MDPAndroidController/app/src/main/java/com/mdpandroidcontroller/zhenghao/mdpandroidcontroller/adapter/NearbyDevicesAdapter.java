package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Zhenghao on 26/1/18.
 */

public class NearbyDevicesAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private static final String TAG = "NearbyDevicesAdapter";

    private List<BluetoothDevice> deviceNameList;

    public NearbyDevicesAdapter(List<BluetoothDevice> deviceNameList) {
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
