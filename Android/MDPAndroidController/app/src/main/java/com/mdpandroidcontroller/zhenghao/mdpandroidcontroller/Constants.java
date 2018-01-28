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

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

}
