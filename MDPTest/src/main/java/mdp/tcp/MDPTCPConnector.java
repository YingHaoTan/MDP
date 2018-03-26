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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import mdp.models.RobotAction;

/**
 *
 * @author JINGYANG
 */
public class MDPTCPConnector {

    private volatile byte nextExpectedID;
    private volatile byte currentID;
    private boolean yetToReceiveAck;
    private Socket clientSocket;
    
    
    // Used for controlling the transmission of stop message
    private boolean resendStop = false;
    private boolean sentStop = false;
    private boolean stopFlag = false;
    
    //SynchronousQueue<ArduinoUpdate> incomingArduinoQueue;
    List<Consumer<ArduinoUpdate>> arduinoUpdateListeners;
    List<Consumer<AndroidInstruction>> androidInstructionListeners;
    Semaphore outgoingSemaphore;
    Queue<ArduinoMessage> outgoingArduinoQueue;
    Queue<AndroidUpdate> outgoingAndroidQueue;

    public MDPTCPConnector(Queue<ArduinoMessage> outgoingArduinoQueue, Queue<AndroidUpdate> outgoingAndroidQueue) throws UnknownHostException, IOException {
        this.clientSocket = new Socket("192.168.6.6", 5000);
        this.arduinoUpdateListeners = new ArrayList<>();
        this.androidInstructionListeners = new ArrayList<>();
        this.outgoingSemaphore = new Semaphore(0);
        this.outgoingArduinoQueue = outgoingArduinoQueue;
        this.outgoingAndroidQueue = outgoingAndroidQueue;
        
        System.out.println("Connection established!");
    }
    
    public List<Consumer<ArduinoUpdate>> getArduinoUpdateListenerList() {
    	return this.arduinoUpdateListeners;
    }
    
    public List<Consumer<AndroidInstruction>> getAndroidInstructionListenerList() {
    	return this.androidInstructionListeners;
    }
    
    public Semaphore getOutgoingSemaphore() {
    	return this.outgoingSemaphore;
    }

    public void startThreads() {
        MDPTCPReceiver receiver = new MDPTCPReceiver();
        MDPTCPSender sender = new MDPTCPSender();
        receiver.start();
        sender.start();
    }

    private synchronized void incrementID() {
        currentID = (byte) ((byte) (currentID + 1) % 10);
    }
    
    private synchronized void incrementNextExpectedID() {
    	nextExpectedID = (byte) ((byte) (nextExpectedID + 1) % 10);
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
    	
        @Override
        public void run() {
            BufferedReader inFromServer = null;
            
            try {
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (true) {
                	Thread.yield();
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
                            case ANDROID_INSTRUCTION:
                                AndroidInstruction fromAndroid = new AndroidInstruction(incoming);
                                // set interrupt variable to true, exploration and fastest path stops giving instruction to Arduino,
                                // only bluetooth gives instructions to Arduino
                                
                                break;
                            case ARDUINO_UPDATE:
                                ArduinoUpdate arduinoUpdate = new ArduinoUpdate(incoming);
                                
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

        @Override
        public void run() {
        	DataOutputStream outToServer = null;
        	
            try {
                /*byte[] test = {0x01,0x01};
                System.out.println(StatusMessage.checkMessageType(test));
                 */
                //long timer = System.currentTimeMillis();

                // Longer timeout, because need to take into account of robot moving. Could implement a simple ACK message from Arduino.
                //long timeout = 2000;

                

                
               // ArduinoInstruction ins = new ArduinoInstruction(lastSent, RobotAction.FORWARD, true);
                //outToServer.writeBytes(new String(ins.toBytes()) + "~");
                /*
                List<RobotAction> actions = new ArrayList<>();
                actions.add(RobotAction.TURN_LEFT);
                actions.add(RobotAction.FORWARD);
                actions.add(RobotAction.FORWARD);
                actions.add(RobotAction.FORWARD);
                actions.add(RobotAction.TURN_RIGHT);
                
                ArduinoStream strm = new ArduinoStream(0, actions);
                outToServer.writeBytes(new String(strm.toBytes()) + "~");
                */
                /*
                while(true) {
                	if(currentID == nextExpectedID) {
                		ArduinoInstruction scan = new ArduinoInstruction(RobotAction.SCAN, false);
                		scan.setID(currentID);
                		
                		outToServer.writeBytes(new String(scan.toBytes()) + "~");
                		incrementID();
                		Thread.sleep(1000);
                	}
                }
                */
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                
                while (true) {
                    System.out.println("=============Controls===========");
                    System.out.println("1) FORWARD");
                    System.out.println("2) TURN_LEFT");
                    System.out.println("3) TURN_RIGHT");
                    System.out.println("4) ABOUT_TURN");
                    System.out.println("5) CAL_SIDE");
                    System.out.println("6) CAL_CORNER");
                    System.out.println("7) CAL_JIEMING");
                    
                    System.out.print("Input: ");
                    //Thread.yield();
                    Scanner keyboard = new Scanner(System.in);
                    int input = keyboard.nextInt();
                    
                    ArduinoInstruction ins = null;
                    
                    switch(input){
                        case 1:
                            ins = new ArduinoInstruction(RobotAction.FORWARD, true);
                            break;
                        case 2:
                            ins = new ArduinoInstruction(RobotAction.TURN_LEFT, true);
                            break;
                        case 3:
                            ins = new ArduinoInstruction(RobotAction.TURN_RIGHT, true);
                            break;
                        case 4:
                            ins = new ArduinoInstruction(RobotAction.ABOUT_TURN, true);
                            break;
                        case 5:
                            ins = new ArduinoInstruction(RobotAction.CAL_SIDE, true);
                            break;
                        case 6:
                            ins = new ArduinoInstruction(RobotAction.CAL_CORNER, true);
                            break;
                        case 7:
                            ins = new ArduinoInstruction(RobotAction.CAL_JIEMING, true);
                            break;
                    }
                    
                    
                    outgoingArduinoQueue.add(ins);
                    
                    ArduinoMessage arduinoMessage = null;
                    if((arduinoMessage = outgoingArduinoQueue.poll()) != null) {
                        // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
                    	arduinoMessage.setID(currentID);
	                System.out.println("Sending: " + currentID + " " + arduinoMessage.getMessageAction());
	                        
	                outToServer.writeBytes(new String(arduinoMessage.toBytes()) + "~");
                    }
                    
            	}
                    
            } catch (SocketException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            }  finally {
                try {
                    outToServer.close();
                } catch (IOException ex) {
                    Logger.getLogger(MDPTCPReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        

    }

}
