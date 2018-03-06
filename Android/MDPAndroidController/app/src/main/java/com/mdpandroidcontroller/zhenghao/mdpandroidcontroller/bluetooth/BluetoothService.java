package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_CONNECTED;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_CONNECTING;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_LISTEN;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_NONE;
import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_NO_INIT;

/**
 * Implement Handler() interface and pass to BluetoothService upon creation.
 *
 * Created by Zhenghao on 28/1/18.
 */

public class BluetoothService {

    private static final String TAG = "BluetoothService";

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothService";

    private static BluetoothService mInstance = null;

    // The connected bluetooth device
    private BluetoothDevice device = null;

    // Unique UUID for this application
    // If new UUID is needed, obtain one from online UUID generation tool
    //private static final UUID MY_UUID = UUID.fromString("124b4e1a-9b5f-4191-a129-90e7947acacd");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // Member fields
    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    /**
     * Get the only instance of this object
     * @return the only instance
     */
    public static BluetoothService getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothService();
        }
        return mInstance;
    }

    /**
     * Private constructor for singleton. Prepares a new Bluetooth service.
     */
    private BluetoothService() {
        mState = STATE_NO_INIT;
    }

    /**
     * Initiate message handler if it is not initiated.
     * @param handler
     */
    public void initHandler(Handler handler) {
        // Check if is already initiated
        if (mState == STATE_NO_INIT) {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            mState = STATE_NONE;
            mHandler = handler;
        }
    }

    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * Force this service to use a new message handler. Use this method if the service is going to
     * be used for another propose. E.g.: use for chat activity.
     * @param handler
     */
    public synchronized void setmHandler (Handler handler) {
        this.mHandler = handler;
    }

    /**
     * For testing
     * @return
     */
    public BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    public void setmAdapter(BluetoothAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        // Reset connected device
        if (this.device != null) {
            this.device = null;
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        this.device = device;

        // Send the name of the connected device back to the parent Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    public synchronized void disconnect() {
        if (mState != STATE_CONNECTED || mConnectedThread == null){
            Log.w(TAG, "disconnect: the device is not connected or has already been disconnected");
            BluetoothService.this.start();
        }
        else if (this.device == null) {
            Log.w(TAG, "disconnect: no connected device found");
            BluetoothService.this.start();
        }
        else {
            mConnectedThread.cancel();
            mState = STATE_LISTEN;
            this.device = null;
            BluetoothService.this.start();
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        this.device = null;
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);
        this.device = null;
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        this.device = null;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    public class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                //tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
//                try {
//                    tmp = (BluetoothServerSocket) mAdapter.getClass().getMethod("listenUsingRfcommOn", new Class[] {int.class}).invoke(mAdapter, 1);
//                }
//                catch (Exception e) {
//                    throw new IOException(e);
//                }
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread::listenUsingRfcommWithServiceRecord failed", e);
            }
            if (tmp == null) {
                Log.e(TAG, "AcceptThread::NPE!!");
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread::run() " + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
//                    Method m = mAdapter.getClass().getMethod("listenUsingRfcommOn", new Class[] { int.class });
//                    int port = mmServerSocket.;
//                    tmp = (BluetoothServerSocket) m.invoke(mAdapter, port);
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    try {
                        Log.e(TAG, "accept() failed", e);
                        mmServerSocket.close();
                        break;
                    } catch (IOException e2) {
                        Log.e(TAG, "accept() failed: failed to close socket", e2);
                        break;
                    }

                } catch (Exception e2) {
                    Log.e(TAG, "accept() failed: other exception", e2);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                // First discard server socket as we only expect one connection at a time
                                try {
                                    mmServerSocket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close server socket", e);
                                }
                                // Second start connected thread
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    mmServerSocket.close();
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.d(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel() " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    public class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                //tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectTread::create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                //mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,4);
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    Log.e(TAG, "ConnectThread::run() - IOException then trying to connect to device");
                    e.printStackTrace();
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close socket", e2);
                    e2.printStackTrace();
                }
                connectionFailed();
                return;
            } catch (Exception e3) {
                Log.e(TAG, "other exception", e3);
                e3.printStackTrace();
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Failed to get input and output stream from socket", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    Log.d(TAG, "ConnectedThread: read string - " + new String(buffer));
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
