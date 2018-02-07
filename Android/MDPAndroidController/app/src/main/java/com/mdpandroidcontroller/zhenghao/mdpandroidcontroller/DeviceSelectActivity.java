package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.ClickListenerInterface;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.bluetoothDevicesAdapter;

import java.util.ArrayList;

public class DeviceSelectActivity extends AppCompatActivity implements ClickListenerInterface {

    private static final String TAG = "DeviceSelectActivity";

    private ArrayList<BluetoothDevice> knownDeviceNameList = null;
    private ArrayList<BluetoothDevice> nearbyDeviceNameList = null;
    private ArrayList<BluetoothDevice> tempDeviceList = null;

    private bluetoothDevicesAdapter knownDevicesAdapter = null;
    private bluetoothDevicesAdapter nearbyDevicesAdapter = null;

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
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(scanButtonOnClickListener);
        scanButton.setText(R.string.scan_devices);

        knownDeviceNameList = getKnownDeviceNameList();
        nearbyDeviceNameList = getNearbyDeviceNameList();
        tempDeviceList = new ArrayList<>();

        RecyclerView knownDevicesRecyclerView = (RecyclerView) findViewById(R.id.knownDevicesRecyclerView);
        RecyclerView nearbyDevicesRecyclerView = (RecyclerView) findViewById(R.id.nearbyDevicesRecyclerView);

        knownDevicesAdapter = new bluetoothDevicesAdapter(getApplicationContext(), this, knownDeviceNameList);
        nearbyDevicesAdapter = new bluetoothDevicesAdapter(getApplicationContext(), this, nearbyDeviceNameList);

        knownDevicesRecyclerView.setAdapter(knownDevicesAdapter);
        nearbyDevicesRecyclerView.setAdapter(nearbyDevicesAdapter);

        knownDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
            Log.d(TAG, "BroadcastReceiver::onReceive: start");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "BroadcastReceiver::onReceive: action found");
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "BroadcastReceiver::onReceive: name - " + device.getName());
                Log.d(TAG, "BroadcastReceiver::onReceive: address - " + device.getAddress());
                Log.d(TAG, "BroadcastReceiver::onReceive: isPaired - " + device.getBondState());
                Log.d(TAG, "BroadcastReceiver::onReceive: type - " + device.getType());

                // if the device is not already paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver::onReceive: new device found");
//                    tempDeviceList.add(device);
//                    nearbyDeviceNameList.clear();
//                    nearbyDeviceNameList.addAll(tempDeviceList);
                    nearbyDevicesAdapter.addDevice(device);

                    if (nearbyDevicesAdapter == null) {
                        throw new NullPointerException("nearbyDevicesAdapter is null!");
                    }
                    else {
                        Log.d(TAG, "BroadcastReceiver::onReceive: notifyDataSetChanged");
                        Log.d(TAG, "BroadcastReceiver::onReceive: device list");
                        for (int i = 0; i < nearbyDeviceNameList.size(); i++) {
                            Log.d(TAG, nearbyDeviceNameList.get(i).getName());
                        }
                        nearbyDevicesAdapter.notifyDataSetChanged();
                    }
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "BroadcastReceiver::onReceive: discovery finish");
                scanButton.setText(R.string.scan_devices);
            }
        }
    };

    Button.OnClickListener scanButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            scanButton.setText(R.string.scanning_devices);
            doDiscovery();
        }
    };

    @Override
    public void onRecyclerViewListClicked(View v, int position) {
        Log.d(TAG, "onRecyclerViewListClicked: start");

        mBluetoothAdapter.cancelDiscovery();

        TextView deviceNameTextView = (TextView) v.findViewById(R.id.deviceName);
        TextView deviceAddressTextView = (TextView) v.findViewById(R.id.deviceAddress);

        String deviceName = deviceNameTextView.getText().toString();
        String deviceAddress = deviceAddressTextView.getText().toString();

        Log.d(TAG, "onRecyclerViewListClicked: deviceName - " + deviceName + ", deviceAddress - " + deviceAddress);

        // Return device info as results to parent activity
        Intent intent = new Intent();
        intent.putExtra(Constants.SELECTED_DEVICE_NAME, deviceName);
        intent.putExtra(Constants.SELECTED_DEVICE_ADDRESS, deviceAddress);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery: start");

        // If previous discovery is still in progress, cancel current discovery
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }
}
