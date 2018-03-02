package mdp.models;

/**
 * RobotAction contains the enumeration of all possible mutually exclusive actions
 * 
 * @author Ying Hao
 */


public enum RobotAction {
        // byte values are for communication
        START((byte)(0x01)),  SCAN((byte)(0x02)), TURN_LEFT((byte)0x03), TURN_RIGHT((byte)0x04), FORWARD((byte)0x05), 
        REVERSE((byte)0x06), STOP((byte)0x07), CAL_CORNER((byte)0x08), CAL_SIDE((byte)0x09);
        
        private byte value;
        
        RobotAction(byte value){
            this.value = value;
        }
        
        public byte getByte(){
            return this.value;
        }
}
