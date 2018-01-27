package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.KnownDevicesAdapter;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.NearbyDevicesAdapter;

import java.util.ArrayList;
import java.util.Set;

public class DeviceSelectActivity extends AppCompatActivity {
    private static final String TAG = "DeviceSelectActivity";

    private ArrayList<BluetoothDevice> knownDeviceNameList = null;
    private ArrayList<BluetoothDevice> nearbyDeviceNameList = null;

    private BluetoothAdapter mBluetoothAdapter = null;

    Button scanButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setResult(Activity.RESULT_CANCELED);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(scanButtonOnClickListener);
        scanButton.setText(R.string.scan_devices);

        knownDeviceNameList = getKnownDeviceNameList();
        nearbyDeviceNameList = getNearbyDeviceNameList();

        RecyclerView knownDevicesRecyclerView = (RecyclerView) findViewById(R.id.knownDevicesRecyclerView);
        RecyclerView nearbyDevicesRecyclerView = (RecyclerView) findViewById(R.id.nearbyDevicesRecyclerView);

        knownDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        knownDevicesRecyclerView.setAdapter(new KnownDevicesAdapter(knownDeviceNameList));
        nearbyDevicesRecyclerView.setAdapter(new NearbyDevicesAdapter(nearbyDeviceNameList));
    }

    private ArrayList<BluetoothDevice> getKnownDeviceNameList() {
        return getIntent().getParcelableArrayListExtra(Constants.PAIRED_DEVICES_LIST);
    }

    private ArrayList<BluetoothDevice> getNearbyDeviceNameList() {
        return new ArrayList<>();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // if the device is not already paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    nearbyDeviceNameList.add(device);
                }
            }
        }
    };

    Button.OnClickListener scanButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            doDiscovery();
        }
    };

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery: start");

        // If previous discovery is still in progress, cancel current discovery
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        scanButton.setText(R.string.scanning_devices);
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

}
