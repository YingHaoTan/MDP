/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

import mdp.models.RobotAction;
import mdp.tcp.ArduinoMessage;

/**
 *
 * @author JINGYANG
 */
public class ArduinoInstruction extends ArduinoMessage{
    
    
    private RobotAction actionToTake;
    private RobotAction calibration;
    
    public ArduinoInstruction(int id, RobotAction actionToTake, RobotAction calibration){
        super(id, StatusMessageType.ANDROID_INSTRUCTION);
        this.calibration = calibration;
        this.actionToTake = actionToTake;
    }
    
    public ArduinoInstruction(RobotAction actionToTake, RobotAction calibration){
        super(StatusMessageType.ANDROID_INSTRUCTION);
        this.calibration = calibration;
        this.actionToTake = actionToTake;
    }

    @Override
    public byte[] toBytes() {
        byte[] toSend = new byte[4];
        // Raspberry Pi need to check byte [0], then sends byte [1] to [2] to Arduino
        toSend[0] = StatusMessageType.ARDUINO_INSTRUCTION.getByte();
        toSend[1] = this.getID();
        toSend[2] = actionToTake.getByte();
        if(calibration == null){
            toSend[3] = 0x00;
        }
        else{
            toSend[3] = calibration.getByte();
        }
        return toSend;
    }
    
    @Override
    public RobotAction getMessageAction(){
        return this.actionToTake;
    }
}
