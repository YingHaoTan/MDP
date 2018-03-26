/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mistlab.mdptest;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import mdp.tcp.AndroidUpdate;
import mdp.tcp.ArduinoMessage;
import mdp.tcp.MDPTCPConnector;

/**
 *
 * @author JINGYANG
 */
public class Program {
    
    public static void main(String[] args){
        try {
            Queue<ArduinoMessage> outgoingArduinoQueue = new ConcurrentLinkedQueue<>();
            Queue<AndroidUpdate> outgoingAndroidQueue = new ConcurrentLinkedQueue<>();
            MDPTCPConnector mdpTCPConnector = new MDPTCPConnector(outgoingArduinoQueue, outgoingAndroidQueue);
            mdpTCPConnector.startThreads();
        } catch (IOException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
