package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.KnownDevicesAdapter;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.NearbyDevicesAdapter;

import java.util.ArrayList;

public class DeviceSelectActivity extends AppCompatActivity {
    private static final String TAG = "DeviceSelectActivity";

    private ArrayList<String> knownDeviceNameList = null;
    private ArrayList<String> nearbyDeviceNameList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        knownDeviceNameList = getKnownDeviceNameList();
        nearbyDeviceNameList = getNearbyDeviceNameList();

        RecyclerView knownDevicesRecyclerView = (RecyclerView) findViewById(R.id.knownDevicesRecyclerView);
        RecyclerView nearbyDevicesRecyclerView = (RecyclerView) findViewById(R.id.nearbyDevicesRecyclerView);

        knownDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        knownDevicesRecyclerView.setAdapter(new KnownDevicesAdapter(knownDeviceNameList));
        nearbyDevicesRecyclerView.setAdapter(new NearbyDevicesAdapter(nearbyDeviceNameList));
    }

    private ArrayList<String> getKnownDeviceNameList() {
        return new ArrayList<>();
    }

    private ArrayList<String> getNearbyDeviceNameList() {
        return new ArrayList<>();
    }

}
