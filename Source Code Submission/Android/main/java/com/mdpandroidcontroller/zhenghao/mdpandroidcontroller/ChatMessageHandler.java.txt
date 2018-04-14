package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Zhenghao on 20/2/18.
 */

public class ChatMessageHandler extends Handler {
    private static final String TAG = "ChatMessageHandler";

    // Call back interface must be implemented in the activity that uses this handler
    interface ChatMessageReceiveCallBack {
        void onMessageReceived(String msg);
    }

    private ChatMessageReceiveCallBack mParentActivity;

    public ChatMessageHandler(ChatMessageReceiveCallBack parentActivity) {
        this.mParentActivity = parentActivity;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case Constants.MESSAGE_DEVICE_NAME:
                break;
            case Constants.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "handleMessage::MESSAGE_READ - message:" + readMessage);

                // on msg received, invoke call back
                mParentActivity.onMessageReceived(readMessage);
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
                break;
        }
    }
}
