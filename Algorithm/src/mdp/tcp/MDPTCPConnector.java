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
import mdp.controllers.XController;
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
                            /*case ANDROID_START:
                        init_algo();
                        tell_arduino_to_start_working();
                        break;
                             */
                            case ANDROID_INSTRUCTION:
                                AndroidInstruction fromAndroid = new AndroidInstruction(incoming);
                                // set interrupt variable to true, exploration and fastest path stops giving instruction to Arduino,
                                // only bluetooth gives instructions to Arduino
                                for(Consumer<AndroidInstruction> consumer: androidInstructionListeners)
                                	consumer.accept(fromAndroid);
                                break;
                            case ARDUINO_UPDATE:
                                ArduinoUpdate arduinoUpdate = new ArduinoUpdate(incoming);
                                System.out.println("Receiving: " + arduinoUpdate.getId());
                                System.out.println("Expected ID: " + nextExpectedID);
                                if (arduinoUpdate.getId() == nextExpectedID && !getSentStop()) {
                                    yetToReceiveAck = false;
                                    
                                    for(Consumer<ArduinoUpdate> consumer: arduinoUpdateListeners)
                                    	consumer.accept(arduinoUpdate);
                                    
                                    incrementNextExpectedID();
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

                outToServer = new DataOutputStream(clientSocket.getOutputStream());

                
               // ArduinoInstruction ins = new ArduinoInstruction(lastSent, RobotAction.FORWARD, true);
                //outToServer.writeBytes(new String(ins.toBytes()) + "~");
                /*
                List<RobotAction> actions = new ArrayList<>();
                actions.add(RobotAction.START);
                actions.add(RobotAction.FORWARD);
                actions.add(RobotAction.FORWARD);
                
                ArduinoStream strm = new ArduinoStream(0, actions);
                outToServer.writeBytes(new String(strm.toBytes()) + "~");
                
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
                while (true) {
                	int processed = 0;
                	
                	Thread.yield();
                	outgoingSemaphore.acquire();
                	
                	ArduinoMessage arduinoMessage = null;
                    if (currentID == nextExpectedID) {
                    	if((arduinoMessage = outgoingArduinoQueue.poll()) != null) {
	                        // Raspberry Pi need to check byte [0], then sends byte [1] to [3] with ~ and ! to Arduino
                    		arduinoMessage.setID(currentID);
	                        System.out.println("Sending: " + currentID + " " + arduinoMessage.getMessageAction());
	                        
	                        outToServer.writeBytes(new String(arduinoMessage.toBytes()) + "~");
	                        yetToReceiveAck = true;
	                        //timer = System.currentTimeMillis();
	                        
	                        if(arduinoMessage.getMessageAction() == RobotAction.STOP){
	                            setSentStop(true);
	                        } 
	                        else{
	                            setSentStop(false);
	                            incrementID();
	                        }
	                        
	                        processed++;
                    	}
                    }
                    
                    AndroidUpdate update = null;
                    if ((update = outgoingAndroidQueue.poll()) != null) {
                    	outToServer.writeBytes(new String(update.toBytes()) + "~");
                    	
                    	System.out.println("Sending android: " + update.getMessage());
                    	
                    	processed++;
                    }
                    
                    // Release permits if no messages is processed
                    // Acquire additional permits if we process more than 1 message in this loop
                    if(processed == 0)
                    	outgoingSemaphore.release();
                    else if(processed > 1)
                    	outgoingSemaphore.acquire(processed - 1);
            	}
                    
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
            } catch (SocketException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            	Logger.getLogger(MDPTCPSender.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
                try {
                	outToServer.close();
                } catch (IOException ex) {
                    Logger.getLogger(MDPTCPReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        

    }

}
