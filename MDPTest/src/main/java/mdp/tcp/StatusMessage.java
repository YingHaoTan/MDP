/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

/**
 *
 * @author JINGYANG
 */
public abstract class StatusMessage {
    enum StatusMessageType{
        ARDUINO_UPDATE((byte)(0x01)),  ARDUINO_INSTRUCTION((byte)(0x02)), ARDUINO_STREAM((byte)(0x03)), ANDROID_START((byte)0x04), ANDROID_INSTRUCTION((byte)0x05), ANDROID_UPDATE((byte)0x06);
        private byte value;
        
        StatusMessageType(byte value){
            this.value = value;
        }
        
        public byte getByte(){
            return this.value;
        }
    }; 
    
    
    private StatusMessageType messageType;

    protected StatusMessage(StatusMessageType type){
        this.messageType = type;
    }
    
    public abstract byte[] toBytes();
    
    protected StatusMessageType getMessageType(){
        return this.messageType;
    }
    
    public static StatusMessageType checkMessageType(byte[] message){
        byte typeInMessage = message[0];
        for(StatusMessageType type : StatusMessageType.values()){
            if(type.getByte() == typeInMessage){
                return type;
            }
        }
        return null;
    }
    
}
