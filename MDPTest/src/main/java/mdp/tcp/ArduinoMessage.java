/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

import mdp.models.RobotAction;

/**
 *
 * @author JINGYANG
 */
public abstract class ArduinoMessage extends StatusMessage{
    private byte id;
    public ArduinoMessage(int id, StatusMessageType type){
        super(type);
        this.id = (byte) id;
    }
    
    public ArduinoMessage(StatusMessageType type){
        super(type);
    }
    
    public byte getID(){
        return this.id;
    }
    
    public void setID(int id){
        this.id = (byte)id;
    }
    
    public abstract RobotAction getMessageAction();
}
