/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

import java.util.List;
import mdp.models.RobotAction;

/**
 *
 * @author JINGYANG
 */
public class ArduinoStream extends ArduinoMessage{

    private List<RobotAction> actions;
    
    public ArduinoStream(int id, List<RobotAction> actions){
        super(id, StatusMessageType.ARDUINO_STREAM);
        this.actions = actions;
    }
    
    public ArduinoStream(List<RobotAction> actions){
        super(StatusMessageType.ARDUINO_STREAM);
        this.actions = actions;
    }
    
    @Override
    public byte[] toBytes() {
        
        /*  1               for MessageType
            1               for id
            1               for number of actions
            lengthOfActions actions
        */
        int length = 3 + actions.size();
        
        byte[] toSend = new byte[length];
        // Raspberry Pi need to check byte [0], then sends byte [1] to [2] to Arduino
        toSend[0] = StatusMessageType.ARDUINO_STREAM.getByte();
        toSend[1] = this.getID();
        toSend[2] = (byte)actions.size();
        for(int i = 0; i < actions.size(); i++){
            toSend[3+i] = actions.get(i).getByte();
        }
        System.out.println("Length of stream:" + actions.size());
        return toSend;
    }

    @Override
    public RobotAction getMessageAction() {
        return null;
    }
    
}
