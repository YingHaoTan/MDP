package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter.MazeGridAdapter;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothClass;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothService;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication.ControllerTranslator;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication.MDPPersistentManager;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.map.Maze;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.CellState;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_CONNECTED;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_NONE;


public class MainActivity extends AppCompatActivity implements ControlMessageHandler.ControlMessageCallBack,
        ControllerTranslator.ControllerTranslatorCallBack{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DEVICE_SELECT = 2;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private Context context = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBluetoothService = null;
    private BluetoothClass mBluetoothClass = null;
    private ControlMessageHandler mHandler =
            ControlMessageHandler.getInstance().withParentActivity(this);

    private Button btnConnect = null;
    private Button btnChat = null;
    private TextView connectionString = null;
//    private PixelGridView arenaView = null;

    //variables for Arena Portion
    private Maze maze;
    private Button settingsButton = null;
    private GridView mazeGridView = null;
    private MazeGridAdapter mazeGridAdapter = null;
    private TextView robotStatusTextView = null;

    //variables for settings popup
    private Dialog settingsDialog = null;
    private TextView closeText = null;
    private EditText robotxSelection, robotySelection, waypointxSelection, waypointySelection = null;
    private Spinner robotdSpinner = null;
    private Button sendRobotButton = null;
    private Button sendWaypointButton = null;

    private ToggleButton persist1Button, persist2Button = null;
    private EditText persistText = null;
    private Button persistSaveButton, persistSendButton = null;

    private Button resetButton = null;


    //variables for controller portion
    private ToggleButton explorationButton = null;
    private ToggleButton fastestPathButton = null;
    private ToggleButton manualControlButton = null;

    private TextView updateModeTextView = null;
    private TextView voiceControlResult = null;
    private Switch updateModeSwitch = null;
    private Button updateButton = null;
    private Button voiceControlButton = null;

    private TableLayout goTable = null;
    private TableLayout controlTable = null;

    private Button goButton = null;
    private Button upButton = null;
    private Button downButton = null;
    private Button leftButton = null;
    private Button rightButton = null;

    private ControllerTranslator translator = null;

    // hacky variable to pass checklist
    //boolean mapChecker = false;
    //boolean robotChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.connectionBtn);
        btnConnect.setOnClickListener(connectButtonListener);

        btnChat = (Button) findViewById(R.id.chatBtn);
        btnChat.setOnClickListener(chatButtonListener);

        connectionString = (TextView) findViewById(R.id.connectionStr);

        context = this.getBaseContext();
        translator = ControllerTranslator.getInstance().withParentActivity(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }

        arenaInit(); //initialization for components within the arena portion
        controllerInit(); //initialization for components within the controller portion
        settingsPopupInit(); //initialization for the popup
    }

    private void arenaInit(){
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(settingsButtonListener);

        mazeGridView = (GridView) findViewById(R.id.mazeGridView);
        maze = new Maze();
        mazeGridAdapter = new MazeGridAdapter(this, maze);
        mazeGridView.setAdapter(mazeGridAdapter);

        robotStatusTextView = (TextView) findViewById(R.id.robotStatusTextView);
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

        voiceControlButton = (Button) findViewById(R.id.voiceControlButton);
        voiceControlButton.setOnClickListener(voiceControlButtonListener);
        voiceControlResult = (TextView) findViewById(R.id.voiceControlResult);
    }

    private void settingsPopupInit(){
        settingsDialog = new Dialog(this);
        settingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // remove header on dialog
        settingsDialog.setContentView(R.layout.settings_popout);


        closeText = (TextView) settingsDialog.findViewById(R.id.settingsClose);
        closeText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                settingsDialog.dismiss();
            }
        });

        robotxSelection = (EditText) settingsDialog.findViewById(R.id.robotRowSelection);
        robotySelection = (EditText) settingsDialog.findViewById(R.id.robotColSelection);
        robotdSpinner = (Spinner) settingsDialog.findViewById(R.id.robotDirSpinner);
        waypointxSelection = (EditText) settingsDialog.findViewById(R.id.waypointRowSelection);
        waypointySelection = (EditText) settingsDialog.findViewById(R.id.waypointColSelection);

        List<String> list = new ArrayList<String>();
        list.add("Up");
        list.add("Down");
        list.add("Left");
        list.add("Right");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        robotdSpinner.setAdapter(dataAdapter);
        robotdSpinner.setSelection(0); // set default value to up

        sendRobotButton = (Button) settingsDialog.findViewById(R.id.robotButton);
        sendRobotButton.setOnClickListener(sendRobotButtonListener);
        sendWaypointButton = (Button) settingsDialog.findViewById(R.id.waypointButton);
        sendWaypointButton.setOnClickListener(sendWaypointButtonListener);

        persist1Button = (ToggleButton) settingsDialog.findViewById(R.id.persist1);
        persist1Button.setOnClickListener(persist1ButtonListener);
        persist2Button = (ToggleButton) settingsDialog.findViewById(R.id.persist2);
        persist2Button.setOnClickListener(persist2ButtonListener);
        persistSaveButton = (Button) settingsDialog.findViewById(R.id.persistSaveButton);
        persistSaveButton.setOnClickListener(persistSaveButtonListener);
        persistSendButton = (Button) settingsDialog.findViewById(R.id.persistSendButton);
        persistSendButton.setOnClickListener(persistSendButtonListener);
        persistText = (EditText) settingsDialog.findViewById(R.id.persistText);
        MDPPersistentManager pm = MDPPersistentManager.getInstance(context);
        persistText.setText(pm.getPersistString(MDPPersistentManager.PERSIST_1));

        resetButton = (Button) settingsDialog.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(resetButtonListener);
    }

    Button.OnClickListener connectButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mBluetoothService == null) {
                Log.w(TAG, "Bluetooth service is not started properly");
                return;
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
            pairedDevicesList.addAll(pairedDevices);

            Intent intent = new Intent(MainActivity.this, DeviceSelectActivity.class);
            intent.putParcelableArrayListExtra(Constants.PAIRED_DEVICES_LIST, pairedDevicesList);
            startActivityForResult(intent, REQUEST_DEVICE_SELECT);
        }
    };

    Button.OnClickListener disconnectButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "disconnectButtonListener::onClick start");
            disconnectDevice();

            btnConnect.setOnClickListener(connectButtonListener);
            btnConnect.setText(R.string.connectionBtnDefaultText);
            connectionString.setText(R.string.connectionStringDefaultText);
        }
    };

    Button.OnClickListener chatButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mBluetoothService == null) {
                Log.e(TAG, "Bluetooth service is not started properly");
                return;
            }

            if (mBluetoothService.getState() != STATE_CONNECTED) {
                Log.d(TAG, "No device connected");
                Toast.makeText(getApplicationContext(), "No device connected", Toast.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
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
                mBluetoothService = BluetoothService.getInstance();
                mBluetoothService.initHandler(mHandler);
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
            mBluetoothService = BluetoothService.getInstance();
            mBluetoothService.initHandler(mHandler);
        }
        else if (mBluetoothService.getState() == STATE_NONE) {
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

            connectDevice(deviceAddress);
        }
        else if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                // An array of potential results
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d(TAG, "voice control onActivityResult - data: " + result.toString());

                onVoiceControlInputReceived(result);
            }
            else {
                Log.e(TAG, "voice control onActivityResult - error!");
            }
        }
    }

    private void onVoiceControlInputReceived(ArrayList<String> result) {
        // Check if the result arraylist contains any command phrase
        List<String> commandPhrasesList = Arrays.asList(Constants.VOICE_COMMOND_PHRASES);
        String inputPhrase;

        for (int i = 0; i < result.size(); i++) {
            if (commandPhrasesList.contains(result.get(i))) {
                voiceControlResult.setText(result.get(i));
                inputPhrase = convertVoicePhraseToCommand(result.get(i));
                if (inputPhrase != null) {
                    Log.d(TAG, "voice control command to send: " + inputPhrase);
                    mBluetoothService.write(inputPhrase.getBytes());
                }
                return;
            }
        }
        voiceControlResult.setText(R.string.voiceInputTextFailed);
    }

    private String convertVoicePhraseToCommand(String phrase) {
        // Use switch for now, ideally should use enum
        switch (phrase) {
            case Constants.VOICE_COMMAND_FORWARD:
                return translator.commandMoveForward();
            case Constants.VOICE_COMMAND_LEFT:
                return translator.commandTurnLeft();
            case Constants.VOICE_COMMAND_RIGHT:
                return translator.commandTurnRight();
            case Constants.VOICE_COMMAND_BACKWARD:
                return translator.commandMoveBack();
            default:
                Log.e(TAG, "Convert voice to phrase error, non existing phrase!");
                return null;
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

    ToggleButton.OnClickListener persist1ButtonListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            persist2Button.setChecked(false);
            //display text
            MDPPersistentManager pm = MDPPersistentManager.getInstance(context);
            persistText.setText(pm.getPersistString(MDPPersistentManager.PERSIST_1));
        }
    };

    ToggleButton.OnClickListener persist2ButtonListener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            persist1Button.setChecked(false);
            //display text
            MDPPersistentManager pm = MDPPersistentManager.getInstance(context);
            persistText.setText(pm.getPersistString(MDPPersistentManager.PERSIST_2));
        }
    };

    Button.OnClickListener persistSaveButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            MDPPersistentManager pm = MDPPersistentManager.getInstance(context);
            if(persist1Button.isChecked()){
                //save here
                pm.savePersistString(MDPPersistentManager.PERSIST_1, persistText.getText().toString());
            }else{
                //save here
                pm.savePersistString(MDPPersistentManager.PERSIST_2, persistText.getText().toString());
            }
        }
    };

    Button.OnClickListener persistSendButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            String message;
            MDPPersistentManager pm = MDPPersistentManager.getInstance(context);
            if(persist1Button.isChecked()){
                message = persistText.getText().toString();
                pm.savePersistString(MDPPersistentManager.PERSIST_1, message);
                mBluetoothService.write(message.getBytes());
            }else{
                message = persistText.getText().toString();
                pm.savePersistString(MDPPersistentManager.PERSIST_2, message);
                mBluetoothService.write(message.getBytes());
            }
        }
    };



    Button.OnClickListener sendRobotButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //check that values are legal
            int robotx = Integer.parseInt(robotxSelection.getText().toString());
            int roboty = Integer.parseInt(robotySelection.getText().toString());

            int robotd = robotdSpinner.getSelectedItemPosition();

            if(robotx < 0 || robotx > Maze.MAZE_COLS - 3 || roboty < 0 || roboty > Maze.MAZE_ROWS -3){
                Toast.makeText(getApplicationContext(),"invalid coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            Direction dir = null;
            switch (robotd){
                case 0:
                    dir = Direction.UP;
                    break;
                case 1:
                    dir = Direction.DOWN;
                    break;
                case 2:
                    dir = Direction.LEFT;
                    break;
                case 3:
                    dir = Direction.RIGHT;
                    break;
                default:
                    Toast.makeText(getApplicationContext(),"invalid direction", Toast.LENGTH_SHORT).show();
                    break;
            }
            //send message to robot
            mBluetoothService.write(translator.commandRobotStartPos(robotx , roboty, dir).getBytes());

            //this para is for testing purposes
            maze.updateRobot(robotx,roboty, dir);
            mazeGridAdapter.updateMaze(maze);
            mazeGridAdapter.notifyDataSetChanged();

        }
    };

    Button.OnClickListener sendWaypointButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //check that values are legal
            int waypointx = Integer.parseInt(waypointxSelection.getText().toString());
            int waypointy = Integer.parseInt(waypointySelection.getText().toString());

            if(waypointx < 0 || waypointx > Maze.MAZE_COLS - 1 || waypointy < 0 || waypointy > Maze.MAZE_ROWS -1){
                Toast.makeText(getApplicationContext(),"invalid coordinates", Toast.LENGTH_SHORT).show();
                return;
            }
            //send message to robot
            mBluetoothService.write(translator.commandWayPoint(waypointx , waypointy).getBytes());

            // for testing now
            maze.updateWaypoint(waypointx,waypointy);
            mazeGridAdapter.updateMaze(maze);
            mazeGridAdapter.notifyDataSetChanged();
        }
    };

    Button.OnClickListener settingsButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            settingsDialog.show();
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
                mBluetoothService.write(translator.commandUpdateAuto().getBytes());
            }else{
                updateModeTextView.setText(R.string.manualModeText);
                updateButton.setVisibility(VISIBLE);
                mBluetoothService.write(translator.commandUpdateManual().getBytes());
            }
        }
    };

    Button.OnClickListener updateButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //send message to robot via bluetooth

            //commented as we are using auto only now
            //mapChecker = true;
            //robotChecker = true;
            mBluetoothService.write(translator.commandUpdateNow().getBytes());

        }
    };

    Button.OnClickListener goButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(explorationButton.isChecked()){
                //send message to robot via bluetooth
                mBluetoothService.write(translator.commandExplore().getBytes());
            }else if(fastestPathButton.isChecked()){
                mBluetoothService.write(translator.commandFastestPath().getBytes());
            }else{
                //handle error here
            }
        }
    };

    Button.OnClickListener upButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            mBluetoothService.write(translator.commandMoveForward().getBytes());
        }
    };

    Button.OnClickListener downButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            mBluetoothService.write(translator.commandMoveBack().getBytes());

        }
    };

    Button.OnClickListener leftButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            mBluetoothService.write(translator.commandTurnLeft().getBytes());
        }
    };

    Button.OnClickListener rightButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            mBluetoothService.write(translator.commandTurnRight().getBytes());

        }
    };

    Button.OnClickListener voiceControlButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "voiceControlButton onClick");
            promptSpeechInput();
        }
    };

    Button.OnClickListener resetButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            mBluetoothService.write(translator.commandReset().getBytes());
            maze = new Maze();
            mazeGridAdapter.updateMaze(maze);
            mazeGridAdapter.notifyDataSetChanged();
        }
    };

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMessageDeviceName(Message msg) {
        Log.d(TAG, "handleMessage::MESSAGE_DEVICE_NAME");
        Toast.makeText(getApplicationContext(),
                "Connected to " + msg.getData().getString(Constants.DEVICE_NAME),
                Toast.LENGTH_SHORT).show();

        updateConnectionUI(mBluetoothService.getDevice().getName(),
                mBluetoothService.getDevice().getAddress());
    }

    @Override
    public void onMessageRead(Message msg) {
        byte[] readBuf = (byte[]) msg.obj;
        String readMessage = new String(readBuf, 0, msg.arg1);
        Log.d(TAG, "handleMessage::MESSAGE_READ - message:" + readMessage);

        translator.decodeMessage(readMessage);
    }

    @Override
    public void onMessageWrite(Message msg) {
        byte[] writeBuf = (byte[]) msg.obj;
        String writeMessage = new String(writeBuf);
        Log.d(TAG, "handleMessage::MESSAGE_WRITE - message:" + writeMessage);

        // TODO: do something with the written string
        // The string has already been written to outStream, no need to resend it here.
        // Instead, peripheral operations on the string can be defined here.
        // E.g, show the message in dialog box or record the string in datastore, etc.


    }

    @Override
    public void onMessageStateChange(Message msg) {
        Log.d(TAG, "handleMessage::MESSAGE_STATE_CHANGE");
        // do nothing
    }

    @Override
    public void onMessageToast(Message msg) {
        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoMove() {
        robotStatusTextView.setText(R.string.statusMoving);
    }

    @Override
    public void onDoTurn() {
        robotStatusTextView.setText(R.string.statusTurn);
    }

    @Override
    public void onDoStop() {
        robotStatusTextView.setText(R.string.statusStop);
    }

    @Override
    public void onDoRobotPos(int x, int y, Direction d){

        maze.updateRobot(x,y,d);
        mazeGridAdapter.updateMaze(maze);
        mazeGridAdapter.notifyDataSetChanged();

        /* Originally used to manage auto vs manual
        if (updateModeSwitch.isChecked()) {
            // Do auto update
            maze.updateRobot(x,y,d);
            mazeGridAdapter.updateMaze(maze);
            mazeGridAdapter.notifyDataSetChanged();
        }
        else {
            // Manual update
            // modify to rely on the update button later on
            if(robotChecker){
                robotChecker = false;
                maze.updateRobot(x,y,d);
                mazeGridAdapter.updateMaze(maze);
                mazeGridAdapter.notifyDataSetChanged();
            }
        }
        */
    }

    @Override
    public void onDoMapUpdateFull(String mdf1, String mdf2) {

        maze.updateMaze(mdf1,mdf2);
        mazeGridAdapter.updateMaze(maze);
        mazeGridAdapter.notifyDataSetChanged();

        /* Originally used to manage auto vs manual
        if (updateModeSwitch.isChecked()) {
            // Do auto update
            maze.updateMaze(mdf1,mdf2);
            mazeGridAdapter.updateMaze(maze);
            mazeGridAdapter.notifyDataSetChanged();
        }
        else {
            // Manual update
            // modify to rely on the update button later on
            if(mapChecker){
                mapChecker = false;
                maze.updateMaze(mdf1,mdf2);
                mazeGridAdapter.updateMaze(maze);
                mazeGridAdapter.notifyDataSetChanged();
            }
        }
        */

    }

    @Override
    public void onDoMapUpdatePartial(int x, int y, CellState cs) {
        maze.updateGrid(x,y,cs);
        mazeGridAdapter.updateMaze(maze);
        mazeGridAdapter.notifyDataSetChanged();
    }
}
