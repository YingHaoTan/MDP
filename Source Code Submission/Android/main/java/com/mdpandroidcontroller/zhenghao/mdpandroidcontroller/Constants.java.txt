package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

/**
 * Created by Zhenghao on 26/1/18.
 */

public class Constants {

    // Intent extra identifier
    public static final String PAIRED_DEVICES_LIST = "PAIRED_DEVICES_LIST";
    public static final String SELECTED_DEVICE_NAME = "SELECTED_DEVICE_NAME";
    public static final String SELECTED_DEVICE_ADDRESS = "SELECTED_DEVICE_ADDRESS";

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Constants that indicate the current connection state
    public static final int STATE_NO_INIT = -1;  // the service is not initialized
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // For voice control
    public static final String VOICE_COMMAND_FORWARD = "move forward";
    public static final String VOICE_COMMAND_LEFT = "turn left";
    public static final String VOICE_COMMAND_RIGHT = "turn right";
    public static final String VOICE_COMMAND_BACKWARD = "move backward";
    public static final String VOICE_COMMAND_EXPLORE = "explore mode";
    public static final String VOICE_COMMAND_FATEST_PATH = "fastest path mode";
    public static final String VOICE_COMMAND_AUTO_UPDATE = "auto update";
    public static final String VOICE_COMMAND_MANUAL_UPDATE = "manual update";
    public static final String[] VOICE_COMMOND_PHRASES =
            {VOICE_COMMAND_FORWARD, VOICE_COMMAND_LEFT, VOICE_COMMAND_RIGHT, VOICE_COMMAND_BACKWARD,
                    VOICE_COMMAND_EXPLORE, VOICE_COMMAND_FATEST_PATH, VOICE_COMMAND_AUTO_UPDATE, VOICE_COMMAND_MANUAL_UPDATE};

}
