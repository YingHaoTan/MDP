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
import java.util.logging.Level;
import java.util.logging.Logger;
import mdp.models.RobotAction;
import mdp.tcp.StatusMessage.StatusMessageType;

/**
 *
 * @author JINGYANG
 */
public class MDPTCPConnector extends Thread {

    String ipAddr;
    int port;
    
    

    public MDPTCPConnector(String ipAddr, int port) {
        this.ipAddr = ipAddr;
        this.port = port;
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
            
            
            ArduinoInstruction instruction = new ArduinoInstruction(lastSent, RobotAction.START, false);
            lastSentArduinoMessage = instruction;
            // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
            byte[] test = instruction.toBytes();
            outToServer.writeBytes(new String(instruction.toBytes()) + "~");
            yetToReceiveAck = true;
            
            
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
                    //byte[] incoming = inFromServer.readLine().getBytes();
                    StatusMessageType messageType = StatusMessage.checkMessageType(incoming);
                    //System.out.println("Sending Raspberry Pi stuffs");
                    //outToServer.writeBytes("YO WHATUP FROM ALGORITHM CLIENT\n");
                    
                    switch(messageType){
                    /*case ANDROID_START:
                        init_algo();
                        tell_arduino_to_start_working();
                        break;
                    */
                    case ARDUINO_UPDATE:
                        ArduinoUpdate arduinoUpdate = new ArduinoUpdate(incoming);
                        System.out.println("Receiving:" + arduinoUpdate.getId());
                        if(arduinoUpdate.getId() == lastSent){
                            System.out.println(arduinoUpdate.getFront1());                          
                            yetToReceiveAck = false;
                            lastSent = incrementID(lastSent);
                            instruction = new ArduinoInstruction(lastSent, RobotAction.SCAN, true);
                            outToServer.writeBytes(new String(instruction.toBytes()) + "\n");
                            yetToReceiveAck = true;
                            timer = System.currentTimeMillis();
                            //send_updates_to_android();
                        }
                        break;
                    }   
                }       
                if(yetToReceiveAck && System.currentTimeMillis() > timer + timeout){
                    System.out.println("Resending:" + instruction.getID());
                    
                    outToServer.writeBytes(new String(instruction.toBytes()) + "~");
                    timer = System.currentTimeMillis();
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
