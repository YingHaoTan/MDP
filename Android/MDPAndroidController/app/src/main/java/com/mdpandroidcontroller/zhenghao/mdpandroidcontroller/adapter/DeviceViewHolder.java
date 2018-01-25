package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.R;

/**
 * Created by Zhenghao on 25/1/18.
 */

public class DeviceViewHolder extends RecyclerView.ViewHolder {
    TextView deviceName;
    public DeviceViewHolder(View itemView) {
        super(itemView);
        this.deviceName = (TextView) itemView.findViewById(R.id.deviceName);
    }
}
