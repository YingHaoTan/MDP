package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Zhenghao on 20/2/18.
 */

public class ControlMessageHandler extends Handler {

    private static final String TAG = "ControlMessageHandler";

    private static ControlMessageHandler mInstance = null;

    private ControlMessageCallBack mParentActivity;

    private ControlMessageHandler() {}

    public static ControlMessageHandler getInstance() {
        if (mInstance == null) {
            mInstance = new ControlMessageHandler();
        }
        return mInstance;
    }

    public ControlMessageHandler withParentActivity (ControlMessageCallBack parentActivity) {
        mParentActivity = parentActivity;
        return this;
    }

    interface ControlMessageCallBack {

        void onMessageDeviceName(Message msg);

        void onMessageRead(Message msg);

        void onMessageWrite(Message msg);

        void onMessageStateChange(Message msg);

        void onMessageToast(Message msg);
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case Constants.MESSAGE_DEVICE_NAME:
                Log.d(TAG, "handleMessage::MESSAGE_DEVICE_NAME");

                mParentActivity.onMessageDeviceName(msg);
                break;
            case Constants.MESSAGE_READ:
                //byte[] readBuf = (byte[]) msg.obj;
                //String readMessage = new String(readBuf, 0, msg.arg1);
                String readMessage = (String) msg.obj;
                Log.d(TAG, "handleMessage::MESSAGE_READ - message:" + readMessage);

                mParentActivity.onMessageRead(msg);
                break;
            case Constants.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                String writeMessage = new String(writeBuf);
                Log.d(TAG, "handleMessage::MESSAGE_WRITE - message:" + writeMessage);

                mParentActivity.onMessageWrite(msg);
                break;
            case Constants.MESSAGE_STATE_CHANGE:
                Log.d(TAG, "handleMessage::MESSAGE_STATE_CHANGE");
                mParentActivity.onMessageStateChange(msg);
                break;
            case Constants.MESSAGE_TOAST:
                mParentActivity.onMessageToast(msg);
                break;
        }
    }
}
