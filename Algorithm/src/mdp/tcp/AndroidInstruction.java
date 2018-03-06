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
public class AndroidInstruction extends StatusMessage{

    private String message = "";
    public AndroidInstruction(byte[] received) {
        
        super(StatusMessage.StatusMessageType.ANDROID_INSTRUCTION);
        // received[0] contains the message type, in this case ANDROID_INSTRUCTION
        for(int i = 1; i<received.length; i++){
            this.message += (char)received[i];
        }
        
    }

    @Override
    public byte[] toBytes() {
        return null;
    }
    
    public String getMessage(){
        return this.message;
    }
    
}
