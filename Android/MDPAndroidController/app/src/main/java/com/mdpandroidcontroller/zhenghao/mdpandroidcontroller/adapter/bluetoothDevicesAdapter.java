package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.R;

import java.util.List;

/**
 * Created by Zhenghao on 25/1/18.
 */

public class bluetoothDevicesAdapter extends RecyclerView.Adapter<bluetoothDevicesAdapter.DeviceViewHolder> {

    private static final String TAG = "bluetoothDevicesAdapter";

    private Context mContext;
    private static ClickListenerInterface clickListener;
    private List<BluetoothDevice> deviceList;

    public bluetoothDevicesAdapter(Context context, ClickListenerInterface clickListener, List<BluetoothDevice> deviceNameList) {
        this.mContext = context;
        this.clickListener = clickListener;
        this.deviceList = deviceNameList;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_device_name, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        if (deviceList.size() == 0) {
            holder.deviceName.setText(R.string.no_device);
            holder.deviceName.setTextColor(Color.parseColor("#c6c6c6"));

            holder.deviceAddress.setText(R.string.no_device_address);
            holder.deviceAddress.setTextColor(Color.parseColor("#c6c6c6"));
        }
        else {
            holder.deviceName.setText(deviceList.get(position).getName());
            holder.deviceName.setTextColor(Color.parseColor("#FF000000"));

            holder.deviceAddress.setText(deviceList.get(position).getAddress());
            holder.deviceAddress.setTextColor(Color.parseColor("#FF000000"));
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView deviceName;
        TextView deviceAddress;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            this.deviceName = (TextView) itemView.findViewById(R.id.deviceName);
            this.deviceAddress = (TextView) itemView.findViewById(R.id.deviceAddress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onRecyclerViewListClicked(v, this.getLayoutPosition());
        }
    }
}
