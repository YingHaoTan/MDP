package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothClass;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothService;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.map.PixelGridView;

import java.util.ArrayList;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_NONE;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DEVICE_SELECT = 2;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private BluetoothClass mBluetoothClass = null;

    private Button btnConnect = null;
    private TextView connectionString = null;
//    private PixelGridView arenaView = null;

    //variables for controller portion
    private ToggleButton explorationButton = null;
    private ToggleButton fastestPathButton = null;
    private ToggleButton manualControlButton = null;

    private TextView updateModeTextView = null;
    private Switch updateModeSwitch = null;
    private Button updateButton = null;

    private TableLayout goTable = null;
    private TableLayout controlTable = null;

    private Button goButton = null;
    private Button upButton = null;
    private Button downButton = null;
    private Button leftButton = null;
    private Button rightButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.connectionBtn);
        btnConnect.setOnClickListener(connectButtonListener);

        connectionString = (TextView) findViewById(R.id.connectionStr);

        controllerInit(); //initialization for components within the controller portion

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }

    }

    private void controllerInit(){
        explorationButton = (ToggleButton) findViewById(R.id.explorationButton);
        explorationButton.setOnClickListener(explorationButtonListener);
        fastestPathButton = (ToggleButton) findViewById(R.id.fastestPathButton);
        fastestPathButton.setOnClickListener(fastestPathButtonListener);
        manualControlButton = (ToggleButton) findViewById(R.id.manualControlButton);
        manualControlButton.setOnClickListener(manualControlButtonListener);

        updateModeTextView = (TextView) findViewById(R.id.updateModeTextView);
        updateModeSwitch = (Switch) findViewById(R.id.updateModeSwitch);
        updateModeSwitch.setOnClickListener(updateModeSwitchListener);
        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(updateButtonListener);

        goTable = (TableLayout) findViewById(R.id.goTable);
        controlTable = (TableLayout)findViewById(R.id.controlTable);

        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(goButtonListener);
        upButton = (Button) findViewById(R.id.upButton);
        upButton.setOnClickListener(upButtonListener);
        downButton = (Button) findViewById(R.id.downButton);
        downButton.setOnClickListener(downButtonListener);
        leftButton = (Button) findViewById(R.id.leftButton);
        leftButton.setOnClickListener(leftButtonListener);
        rightButton = (Button) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(rightButtonListener);
    }

    Button.OnClickListener connectButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mBluetoothService == null) {
                Log.w(TAG, "Bluetooth service is not started properly, trying to start it now");
                mBluetoothService = new BluetoothService(getApplicationContext(), mHandler);
            }
//            if (mBluetoothClass == null) {
//                mBluetoothClass = BluetoothClass.getInstance();
//            }

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
            Log.d(TAG, "disconnectButtonListener::onClick start");
            disconnectDevice();

            btnConnect.setOnClickListener(connectButtonListener);
            btnConnect.setText(R.string.connectionBtnDefaultText);
            connectionString.setText(R.string.connectionStringDefaultText);
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
//            if (mBluetoothClass == null) {
//                mBluetoothClass = BluetoothClass.getInstance();
//                mBluetoothClass.setmHandler(mHandler);
//            }
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
        else if (mBluetoothService.getState() == STATE_NONE) {
            mBluetoothService.start();
        }

//        if (mBluetoothClass == null) {
//            mBluetoothClass = BluetoothClass.getInstance();
//        }
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

            connectDevice(deviceAddress);
        }
    }

    private void connectDevice(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG, "connectDevice: connecting device " + device.getName());
        mBluetoothService.connect(device);
    }

    private void connectDeviceNew(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d(TAG, "connectDevice: connecting device " + device.getName());
        try {
            mBluetoothClass.ConnectToDevice(device);
        } catch (InterruptedException e) {
            Log.d(TAG, "connectDeviceNew: connecting device interrupted ex");
            e.printStackTrace();
        }

    }

    private void updateConnectionUI(String deviceName, String deviceAddress) {
        Log.d(TAG, "updateConnection start");
        connectionString.setText("Device connected: " + deviceName + "@" + deviceAddress);
        btnConnect.setText(R.string.connectionBtnDisconnectText);
        btnConnect.setOnClickListener(disconnectButtonListener);
    }

    private void disconnectDevice() {
        // No need to validate connection state here, it is done in the disconnect() method.
        mBluetoothService.disconnect();
        Log.d(TAG, "disconnectDevice: disconnecting device");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "handleMessage::MESSAGE_DEVICE_NAME");
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + msg.getData().getString(Constants.DEVICE_NAME),
                            Toast.LENGTH_SHORT).show();
                    updateConnectionUI(mBluetoothService.getDevice().getName(),
                            mBluetoothService.getDevice().getAddress());
//                    updateConnectionUI(mBluetoothClass.getMmDevice().getName(),
//                            mBluetoothClass.getMmDevice().getAddress());
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "handleMessage::MESSAGE_READ - message:" + readMessage);
                    // TODO: do something with the received string
                    // How the controller should react to the received string


                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "handleMessage::MESSAGE_WRITE - message:" + writeMessage);
                    // TODO: do something with the written string
                    // The string has already been written to outStream, no need to resend it here.
                    // Instead, peripheral operations on the string can be defined here.
                    // E.g, show the message in dialog box or record the string in datastore, etc



                    break;
                case Constants.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "handleMessage::MESSAGE_STATE_CHANGE");
                    // do nothing
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    ToggleButton.OnClickListener explorationButtonListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            goTable.setVisibility(VISIBLE);
            controlTable.setVisibility(GONE);
            fastestPathButton.setChecked(false);
            manualControlButton.setChecked(false);
        }
    };

    ToggleButton.OnClickListener fastestPathButtonListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            goTable.setVisibility(VISIBLE);
            controlTable.setVisibility(GONE);
            explorationButton.setChecked(false);
            manualControlButton.setChecked(false);
        }
    };

    ToggleButton.OnClickListener manualControlButtonListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            goTable.setVisibility(GONE);
            controlTable.setVisibility(VISIBLE);
            explorationButton.setChecked(false);
            fastestPathButton.setChecked(false);
        }
    };

    Button.OnClickListener updateModeSwitchListener = new Switch.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(updateModeSwitch.isChecked()){
                updateModeTextView.setText(R.string.autoModeText);
                updateButton.setVisibility(GONE);
            }else{
                updateModeTextView.setText(R.string.manualModeText);
                updateButton.setVisibility(VISIBLE);
            }
        }
    };

    Button.OnClickListener updateButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            //send message to robot via bluetooth

        }
    };

    Button.OnClickListener goButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(explorationButton.isChecked()){
                //send message to robot via bluetooth
            }else if(fastestPathButton.isChecked()){
                //send message to robot via bluetooth
            }else{
                //handle error here
            }
        }
    };

    Button.OnClickListener upButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            //send message to robot via bluetooth
//            Log.d(TAG, "up button onClick");
//            mBluetoothService.write("test123".getBytes());
        }
    };

    Button.OnClickListener downButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            //send message to robot via bluetooth

        }
    };

    Button.OnClickListener leftButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            //send message to robot via bluetooth

        }
    };

    Button.OnClickListener rightButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            //send message to robot via bluetooth

        }
    };

}
