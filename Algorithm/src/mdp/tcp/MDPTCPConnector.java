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
import mdp.tcp.StatusMessage.StatusMessageType;

/**
 *
 * @author JINGYANG
 */
public class MDPTCPConnector extends Thread {

    String ipAddr;
    int port;
    SynchronousQueue<ArduinoUpdate> incomingArduinoQueue;
    Queue<ArduinoInstruction> outgoingArduinoQueue;
    Queue<StatusMessage> outgoingAndroidQueue;
    

    public MDPTCPConnector(String ipAddr, int port, SynchronousQueue incomingArduinoQueue, Queue outgoingArduinoQueue, Queue outgoingAndroidQueue) {
        this.ipAddr = ipAddr;
        this.port = port;
        this.incomingArduinoQueue = incomingArduinoQueue;
        this.outgoingArduinoQueue = outgoingArduinoQueue;
        this.outgoingAndroidQueue = outgoingAndroidQueue;
        
    }

    @Override
    public void run() {
        try {
 
            /*byte[] test = {0x01,0x01};
            System.out.println(StatusMessage.checkMessageType(test));
            */
            byte lastSent = 0;
            boolean yetToReceiveAck = false;
            ArduinoInstruction lastSentArduinoMessage = null;
            long timer = System.currentTimeMillis();
            
            // Longer timeout, because need to take into account of robot moving. Could implement a simple ACK message from Arduino.
            long timeout = 500;
            
            Socket clientSocket = new Socket(ipAddr, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                      
            
            while (true) {
                // If I receive something from Raspberry Pi
                if(inFromServer.ready()){
                    // Need to read until '~'
                    String incomingStr = "";
                    while(inFromServer.ready()){
                        int read = inFromServer.read();
                        if(read != 126){
                            incomingStr += (char)read;
                        }
                        else{
                            break;
                        }
                    }
                    byte[] incoming = incomingStr.getBytes();
                    StatusMessageType messageType = StatusMessage.checkMessageType(incoming);
                    
                    switch(messageType){
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
                            if(arduinoUpdate.getId() == lastSent){
                                yetToReceiveAck = false;
                                lastSent = incrementID(lastSent);
                                try {
                                    incomingArduinoQueue.put(arduinoUpdate);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                // Sends Android map updates, maybe put this in PhysicalRobot.move()
                                // outgoingAndroidQueue.add();
                            }
                            break;
                    }   
                }       
                if(!outgoingArduinoQueue.isEmpty()){
                    // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
                    lastSentArduinoMessage = outgoingArduinoQueue.remove();
                    lastSentArduinoMessage.setID(lastSent);
                    System.out.println("Sending: " + lastSent);
                    outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                    yetToReceiveAck = true;
                    timer = System.currentTimeMillis();
                }
                if(!outgoingAndroidQueue.isEmpty()){
                    // sends whatever format u like
                }
                if(yetToReceiveAck && System.currentTimeMillis() > timer + timeout){
                    if(lastSentArduinoMessage != null){
                        System.out.println("Resending:" + lastSentArduinoMessage.getID());
                        outToServer.writeBytes(new String(lastSentArduinoMessage.toBytes()) + "~");
                        timer = System.currentTimeMillis();
                    }
                }                
            }
        } catch (SocketException ex) {
            Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private byte incrementID(byte lastSent){
        byte value =  (byte)((byte)(lastSent+1)%126);
        return value;
    }
}
