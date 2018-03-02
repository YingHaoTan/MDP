/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JINGYANG
 */
public class MDPTCPConnector {

    private byte lastSent;
    private boolean yetToReceiveAck;
    private Socket clientSocket;
    SynchronousQueue<ArduinoUpdate> incomingArduinoQueue;
    Queue<ArduinoInstruction> outgoingArduinoQueue;
    Queue<StatusMessage> outgoingAndroidQueue;

    public MDPTCPConnector(SynchronousQueue incomingArduinoQueue, Queue outgoingArduinoQueue, Queue outgoingAndroidQueue) {
        try {
            this.clientSocket = new Socket("localhost", 5000);
            this.incomingArduinoQueue = incomingArduinoQueue;
            this.outgoingArduinoQueue = outgoingArduinoQueue;
            this.outgoingAndroidQueue = outgoingAndroidQueue;
        } catch (IOException ex) {
            Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void startThreads() {
        MDPTCPReceiver receiver = new MDPTCPReceiver(clientSocket, incomingArduinoQueue);
        MDPTCPSender sender = new MDPTCPSender(clientSocket, outgoingArduinoQueue, outgoingAndroidQueue);
        receiver.start();
        sender.start();
    }

    public synchronized void incrementID(byte lastSent) {
        this.lastSent = (byte) ((byte) (lastSent + 1) % 126);
    }

    public class MDPTCPReceiver extends Thread {

        Socket connectedSocket;

        SynchronousQueue<ArduinoUpdate> incomingArduinoQueue;

        public MDPTCPReceiver(Socket connectedSocket, SynchronousQueue incomingArduinoQueue) {
            this.incomingArduinoQueue = incomingArduinoQueue;
            this.connectedSocket = connectedSocket;

        }

        @Override
        public void run() {
            BufferedReader inFromServer = null;
            try {
                inFromServer = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
                while (true) {
                    // If I receive something from Raspberry Pi
                    if (inFromServer.ready()) {
                        // Need to read until '~'
                        String incomingStr = "";
                        while (inFromServer.ready()) {
                            int read = inFromServer.read();
                            if (read != 126) {
                                incomingStr += (char) read;
                            } else {
                                break;
                            }
                        }
                        byte[] incoming = incomingStr.getBytes();
                        StatusMessage.StatusMessageType messageType = StatusMessage.checkMessageType(incoming);

                        switch (messageType) {
                            /*case ANDROID_START:
                        init_algo();
                        tell_arduino_to_start_working();
                        break;
                             */
                            case ANDROID_INSTRUCTION:
                                // set interrupt variable to true, exploration and fastest path stops giving instruction to Arduino,
                                // only bluetooth gives instructions to Arduino
                                break;
                            case ARDUINO_UPDATE:
                                ArduinoUpdate arduinoUpdate = new ArduinoUpdate(incoming);
                                System.out.println("Receiving: " + arduinoUpdate.getId());
                                if (arduinoUpdate.getId() == lastSent) {
                                    yetToReceiveAck = false;
                                    incrementID(lastSent);
                                    try {
                                        incomingArduinoQueue.put(arduinoUpdate);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    // Sends Android map updates, maybe put this in PhysicalRobot.move()
                                    // outgoingAndroidQueue.add();
                                    // Sends Android map updates, maybe put this in PhysicalRobot.move()
                                    // outgoingAndroidQueue.add();
                                }
                                break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MDPTCPReceiver.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    inFromServer.close();
                } catch (IOException ex) {
                    Logger.getLogger(MDPTCPReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public class MDPTCPSender extends Thread {

        Socket connectedSocket;
        Queue<ArduinoInstruction> outgoingArduinoQueue;
        Queue<StatusMessage> outgoingAndroidQueue;

        public MDPTCPSender(Socket connectedSocket, Queue outgoingArduinoQueue, Queue outgoingAndroidQueue) {
            this.connectedSocket = connectedSocket;
            this.outgoingArduinoQueue = outgoingArduinoQueue;
            this.outgoingAndroidQueue = outgoingAndroidQueue;

        }

        @Override
        public void run() {
            try {

                /*byte[] test = {0x01,0x01};
                System.out.println(StatusMessage.checkMessageType(test));
                 */
                ArduinoInstruction lastSentArduinoMessage = null;
                long timer = System.currentTimeMillis();

                // Longer timeout, because need to take into account of robot moving. Could implement a simple ACK message from Arduino.
                long timeout = 5000;

                DataOutputStream outToServer = new DataOutputStream(connectedSocket.getOutputStream());

                while (true) {
                    Thread.sleep((long) 0.1);
                    if (!outgoingArduinoQueue.isEmpty()) {
                        // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
                        lastSentArduinoMessage = outgoingArduinoQueue.remove();
                        lastSentArduinoMessage.setID(lastSent);
                        System.out.println("Sending: " + lastSent);
                        outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                        yetToReceiveAck = true;
                        timer = System.currentTimeMillis();
                    }
                    if (!outgoingAndroidQueue.isEmpty()) {
                        // sends whatever format u like
                    }
                    if (yetToReceiveAck && System.currentTimeMillis() > timer + timeout) {
                        if (lastSentArduinoMessage != null) {
                            System.out.println("Resending:" + lastSentArduinoMessage.getID());
                            outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                            timer = System.currentTimeMillis();
                        }
                    }
                }
            } catch (SocketException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
