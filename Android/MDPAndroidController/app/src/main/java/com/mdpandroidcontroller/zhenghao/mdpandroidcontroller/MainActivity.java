package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothService;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DEVICE_SELECT = 2;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;

    private Button btnConnect = null;
    private TextView connectionString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.connectionBtn);
        btnConnect.setOnClickListener(connectButtonListener);

        connectionString = (TextView) findViewById(R.id.connectionStr);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // TODO: check if a bluetooth device is already connected

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    Button.OnClickListener connectButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mBluetoothService == null) {
                Log.w(TAG, "Bluetooth service is not started properly, trying to start it now");
                mBluetoothService = new BluetoothService(getApplicationContext(), mHandler);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
            pairedDevicesList.addAll(pairedDevices);

            Intent intent = new Intent(MainActivity.this, DeviceSelectActivity.class);
            intent.putParcelableArrayListExtra(Constants.PAIRED_DEVICES_LIST, pairedDevicesList);
            startActivityForResult(intent, REQUEST_DEVICE_SELECT);
        }
    };

    Button.OnClickListener disconnectButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO: do disconnect here


            btnConnect.setOnClickListener(connectButtonListener);
            btnConnect.setText(R.string.connectionBtnDefaultText);
            connectionString.setText(R.string.connectionStringDefaultText);
            Log.d(TAG, "disconnectButtonListener::onClick start");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // If the adapter is not enabled, show request for enable bluetooth service
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // else start the bluetooth service for establish connections
        else {
            if (mBluetoothService == null) {
                mBluetoothService = new BluetoothService(getApplicationContext(), mHandler);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Start the bluetooth service after the bluetooth is enabled
        // We need to do this because if the bluetooth is not enabled when app started, user would
        // be promoted to enable it. This is done in a new activity. After the activity returns,
        // onResume() is called and we can start the service.
        if (mBluetoothService == null) {
            mBluetoothService = new BluetoothService(getApplicationContext(), mHandler);
        }
        else if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
            mBluetoothService.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request code " + requestCode + " result code " + resultCode);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: enable BT done");
            Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_LONG).show();
        }
        else if (requestCode == REQUEST_DEVICE_SELECT && resultCode == RESULT_OK) {
            String deviceName = data.getStringExtra(Constants.SELECTED_DEVICE_NAME);
            String deviceAddress = data.getStringExtra(Constants.SELECTED_DEVICE_ADDRESS);
            Log.d(TAG, "onActivityResult: selected device - " + deviceName + " " + deviceAddress);
            updateConnection(deviceName, deviceAddress);
        }
    }

    private void updateConnection(String deviceName, String deviceAddress) {
        Log.d(TAG, "updateConnection start");
        // do connect here

        connectionString.setText("Device connected: " + deviceName + "@" + deviceAddress);
        btnConnect.setText(R.string.connectionBtnDisconnectText);
        btnConnect.setOnClickListener(disconnectButtonListener);
    }

    // TODO: Override handleMessage method
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
