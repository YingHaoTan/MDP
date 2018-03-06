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
public class ArduinoUpdate extends StatusMessage{
    
    private byte id;
    private byte front1;
    private byte front2;
    private byte front3;
    private byte right1;
    private byte right2;
    private byte left1;
    private byte reached;

    public ArduinoUpdate(byte[] received) {
        super(StatusMessageType.ARDUINO_UPDATE);
        // received[0] contains the message type, in this case ARDUINO_UPDATE
        id = (byte) Character.getNumericValue(received[1]);
        front1 = (byte) Character.getNumericValue(received[2]);
        front2 = (byte) Character.getNumericValue(received[3]);
        front3 = (byte) Character.getNumericValue(received[4]);
        right1 = (byte) Character.getNumericValue(received[5]);
        right2 = (byte) Character.getNumericValue(received[6]);
        left1 = (byte) Character.getNumericValue(received[7]);
        reached = (byte) Character.getNumericValue(received[8]);
    }

    @Override
    public byte[] toBytes() {
        return null;
    }

    public byte getId() {
        return id;
    }

    public byte getFront1() {
        return front1;
    }

    public byte getFront2() {
        return front2;
    }

    public byte getFront3() {
        return front3;
    }

    public byte getRight1() {
        return right1;
    }

    public byte getRight2() {
        return right2;
    }

    public byte getLeft1() {
        return left1;
    }

    public byte getReached() {
        return reached;
    }

    
    
}
