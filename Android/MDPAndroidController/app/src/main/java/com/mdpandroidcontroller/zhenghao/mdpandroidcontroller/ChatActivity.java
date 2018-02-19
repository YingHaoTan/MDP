package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothService;

public class ChatActivity extends AppCompatActivity implements ChatMessageHandler.ChatMessageReceiveCallBack {

    private static final String TAG = "ChatActivity";

    private BluetoothService mBluetoothService = null;
    private Handler mHandler = new ChatMessageHandler(this);

    private TextView receivedTextView = null;
    private EditText sendTextView = null;
    private Button sendBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sendTextView = (EditText) findViewById(R.id.sendTextView);
        sendBtn = (Button) findViewById(R.id.chatSendBtn);

        sendBtn.setOnClickListener(sendBtnListener);
    }

    Button.OnClickListener sendBtnListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg = sendTextView.getText().toString();
            Log.d(TAG, "send button onClick with message: " + msg);
            mBluetoothService.write(msg.getBytes());
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mBluetoothService = BluetoothService.getInstance();

        if (mBluetoothService.getState() == Constants.STATE_NO_INIT) {
            Log.e(TAG, "bluetooth service is not started properly.");
        }
        // Set the message handler to chat message handler.
        mBluetoothService.setmHandler(mHandler);
    }

    @Override
    public void onMessageReceived(String msg) {
        Log.d(TAG, "onMessageReceived call back fired with message: " + msg);
        receivedTextView.setText(msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Reset the handler to control message handler.
        mBluetoothService.setmHandler(ControlMessageHandler.getInstance());
    }
}
