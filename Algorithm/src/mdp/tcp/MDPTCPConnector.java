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
            Socket clientSocket = new Socket(ipAddr, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            while (true) {
                // If I receive something from Raspberry Pi
                if(inFromServer.ready()){
                    System.out.println(inFromServer.readLine());
                    System.out.println("Sending Raspberry Pi stuffs");
                    outToServer.writeBytes("YO WHATUP FROM ALGORITHM CLIENT\n");
                    /*
                    switch(Header){
                    case ANDROID_START:
                        init_algo();
                        tell_arduino_to_start_working();
                        break;
                    
                    case ARDUINO_STATUS:
                        read_status_updates();
                        send_updates_to_android();
                        break;
                    
                    } 
                    */
                }                                
            }
        } catch (SocketException ex) {
            Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MDPTCPConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
