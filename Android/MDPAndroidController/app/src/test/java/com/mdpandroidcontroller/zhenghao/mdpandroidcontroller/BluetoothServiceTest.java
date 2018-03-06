package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.bluetooth.BluetoothService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.Constants.STATE_CONNECTED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by Zhenghao on 5/3/18.
 */

@RunWith(MockitoJUnitRunner.class)
public class BluetoothServiceTest {

    private static final String NAME = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Class under test
    private BluetoothService bluetoothService = BluetoothService.getInstance();

    // Sample return
    private int mockInStreamReadReturn = 4;
    private String mockDeviceName = "deviceName";

    @Mock
    private BluetoothAdapter mockAdapter;

    @Mock
    private Handler mockHandler;

    @Test
    public void testBluetoothServiceStart() throws Exception {

        BluetoothService spiedClass = spy(bluetoothService);

        BluetoothServerSocket mockServerSocket = mock(BluetoothServerSocket.class);
        when(mockAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID))
                .thenReturn(mockServerSocket);

        BluetoothSocket mockSocket = mock(BluetoothSocket.class);
        BluetoothDevice mockDevice = mock(BluetoothDevice.class);
        when(mockServerSocket.accept()).thenReturn(mockSocket);
        when(mockSocket.getRemoteDevice()).thenReturn(mockDevice);
        when(mockDevice.getName()).thenReturn(mockDeviceName);
        doNothing().when(mockServerSocket).close();

        InputStream mockInputStream = mock(InputStream.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        when(mockInputStream.read(any(byte[].class))).thenReturn(mockInStreamReadReturn);

        Message mockMessage = mock(Message.class);
        when(mockHandler.obtainMessage(anyInt(), anyInt(), anyInt(), any(byte[].class)))
                .thenReturn(mockMessage);
        when(mockHandler.obtainMessage(anyInt(), anyInt(), anyInt())).thenReturn(mockMessage);
        when(mockHandler.obtainMessage(anyInt())).thenReturn(mockMessage);
        when(mockHandler.sendMessage(mockMessage)).thenReturn(true);
        doNothing().when(mockMessage).setData(any(Bundle.class));
        doNothing().when(mockMessage).sendToTarget();
        doNothing().when(mockInputStream).close();
        doNothing().when(mockOutputStream).close();
        doNothing().when(mockSocket).close();

        bluetoothService.setmAdapter(mockAdapter);
        bluetoothService.setmHandler(mockHandler);
        bluetoothService.start();

        System.out.println("actual state: " + bluetoothService.getState());
        System.out.println("expected state: " + STATE_CONNECTED);
        assertTrue(bluetoothService.getState() == STATE_CONNECTED);
    }
}
