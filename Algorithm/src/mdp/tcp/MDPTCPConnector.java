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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mdp.controllers.XController;
import mdp.models.RobotAction;

/**
 *
 * @author JINGYANG
 */
public class MDPTCPConnector {

    private byte lastSent;
    private boolean yetToReceiveAck;
    private Socket clientSocket;
    
    
    // Used for controlling the transmission of stop message
    private boolean resendStop = false;
    private boolean sentStop = false;
    private boolean stopFlag = false;
    
    //SynchronousQueue<ArduinoUpdate> incomingArduinoQueue;
    Queue<ArduinoUpdate> incomingArduinoQueue;
    Queue<ArduinoMessage> outgoingArduinoQueue;
    Queue<StatusMessage> outgoingAndroidQueue;

    public MDPTCPConnector(Queue<ArduinoUpdate> incomingArduinoQueue, Queue<ArduinoMessage> outgoingArduinoQueue, Queue<StatusMessage> outgoingAndroidQueue) {
        try {
            this.clientSocket = new Socket("", 5000);
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

    private synchronized void incrementID(byte lastSent) {
        this.lastSent = (byte) ((byte) (lastSent + 1) % 126);
    }
    
    private void setResendStop(boolean value){
        this.resendStop = value;
    }
        
    private void setSentStop(boolean value){
        this.sentStop = value;
    }
    
    private synchronized boolean getResendStop(){
        return this.resendStop;
    }
    
    private synchronized boolean getSentStop(){
        return this.sentStop;
    }
    
    private synchronized boolean getStopFlag(){
        return this.stopFlag;
    }
    
    private synchronized void setStopFlag(boolean value){
        this.stopFlag = value;
    }

    public class MDPTCPReceiver extends Thread {

        Socket connectedSocket;
        Queue<ArduinoUpdate> incomingArduinoQueue;
        XController xcon;

        public MDPTCPReceiver(Socket connectedSocket, Queue<ArduinoUpdate> incomingArduinoQueue) {
            this.incomingArduinoQueue = incomingArduinoQueue;
            this.connectedSocket = connectedSocket;
            //this.xcon = xcon
        }

        @Override
        public void run() {
            BufferedReader inFromServer = null;
            AndroidCommandsTranslator androidTranslator = new AndroidCommandsTranslator(xcon);
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
                                AndroidInstruction fromAndroid = new AndroidInstruction(incoming);
                                androidTranslator.decodeMessage(fromAndroid.getMessage());
                                // set interrupt variable to true, exploration and fastest path stops giving instruction to Arduino,
                                // only bluetooth gives instructions to Arduino
                                break;
                            case ARDUINO_UPDATE:
                                ArduinoUpdate arduinoUpdate = new ArduinoUpdate(incoming);
                                System.out.println("Receiving: " + arduinoUpdate.getId());
                                System.out.println("Last sent is" + lastSent);
                                if (arduinoUpdate.getId() == lastSent && !getSentStop()) {
                                    
                                    System.out.println("front1:" + arduinoUpdate.getFront1());
                                    System.out.println("front2:" + arduinoUpdate.getFront2());
                                    System.out.println("front3:" + arduinoUpdate.getFront3());
                                    System.out.println("right1:" + arduinoUpdate.getRight1());
                                    System.out.println("right2:" + arduinoUpdate.getRight2());
                                    System.out.println("left1:" + arduinoUpdate.getLeft1());
                                    
                                    
                                    yetToReceiveAck = false;
                                    incrementID(lastSent);
                                    //incomingArduinoQueue.add(arduinoUpdate);
                                    //System.out.println("size from mdp receiver=" + incomingArduinoQueue.size());
                                    // Sends Android map updates, maybe put this in PhysicalRobot.move()
                                    // outgoingAndroidQueue.add();
                                    // Sends Android map updates, maybe put this in PhysicalRobot.move()
                                    // outgoingAndroidQueue.add();
                                }
                                else if(getSentStop()){
                                    setResendStop(true);
                                }
                                break;
							default:
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
        Queue<ArduinoMessage> outgoingArduinoQueue;
        Queue<StatusMessage> outgoingAndroidQueue;

        public MDPTCPSender(Socket connectedSocket, Queue<ArduinoMessage> outgoingArduinoQueue, Queue<StatusMessage> outgoingAndroidQueue) {
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
                ArduinoMessage lastSentArduinoMessage = null;
                long timer = System.currentTimeMillis();

                // Longer timeout, because need to take into account of robot moving. Could implement a simple ACK message from Arduino.
                long timeout = 2000;

                DataOutputStream outToServer = new DataOutputStream(connectedSocket.getOutputStream());

                ArduinoInstruction ins;
                while(true){
                    ins = new ArduinoInstruction(lastSent, RobotAction.SCAN, true);
                    outToServer.writeBytes(new String(ins.toBytes()) + "~");
                    Thread.sleep(1000);
                    
                }
                
               /* ArduinoInstruction ins = new ArduinoInstruction(lastSent, RobotAction.FORWARD, true);
                outToServer.writeBytes(new String(ins.toBytes()) + "~");*/
                /*List<RobotAction> actions = new ArrayList<>();
                actions.add(RobotAction.START);
                actions.add(RobotAction.FORWARD);
                actions.add(RobotAction.FORWARD);
                */
                
                /*ArduinoStream strm = new ArduinoStream(0, actions);
                outToServer.writeBytes(new String(strm.toBytes()) + "~");
                */
                /*
                while (true) {
                    Thread.sleep((long) 0.1);
                    if (!outgoingArduinoQueue.isEmpty()) {
                        // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
                        lastSentArduinoMessage = outgoingArduinoQueue.remove();
                        lastSentArduinoMessage.setID(lastSent);
                        System.out.println("Sending: " + lastSent + " " + lastSentArduinoMessage.getMessageAction());
                        
                        outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                        yetToReceiveAck = true;
                        timer = System.currentTimeMillis();
                        
                        if(lastSentArduinoMessage.getMessageAction() == RobotAction.STOP){
                            setSentStop(true);

                        } 
                        else{
                            setSentStop(false);
                        }
                    }
                    if (!outgoingAndroidQueue.isEmpty()) {
                        // sends whatever format u like
                    }*/
                    /*
                    if (yetToReceiveAck && System.currentTimeMillis() > timer + timeout) {
                        if ((lastSentArduinoMessage != null) && (lastSentArduinoMessage.getMessageAction() != RobotAction.STOP)) {
                            System.out.println("Resending:" + lastSentArduinoMessage.getID());
                            outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                            timer = System.currentTimeMillis();
                        }
                    }*/
                    /*
                    if(getResendStop() && System.currentTimeMillis() > timer + timeout){
                        System.out.println("Resending STOP :" + lastSent);
                        ArduinoMessage stopMessage = new ArduinoInstruction(lastSent, RobotAction.STOP, false);
                        outToServer.writeBytes(new String(stopMessage.toBytes()) + "~");
                        timer = System.currentTimeMillis();
                        setResendStop(false);
                    }*/
                //}
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
