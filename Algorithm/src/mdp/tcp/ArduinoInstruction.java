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
public class ArduinoInstruction extends StatusMessage{
    
    private RobotAction actionToTake;
    private byte id;
    
    public ArduinoInstruction(int id, RobotAction actionToTake){
        super(StatusMessageType.ARDUINO_INSTRUCTION);
        this.id = (byte)id;
        this.actionToTake = actionToTake;
    }

    @Override
    public byte[] toBytes() {
        byte[] toSend = new byte[3];
        // Raspberry Pi need to check byte [0], then sends byte [1] to [2] to Arduino
        toSend[0] = StatusMessageType.ARDUINO_INSTRUCTION.getByte();
        toSend[1] = id;
        toSend[2] = actionToTake.getByte();
        return toSend;
    }
    
    public int getID(){
        return this.id;
    }
}
