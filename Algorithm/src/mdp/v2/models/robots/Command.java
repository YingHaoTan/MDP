package mdp.v2.models.robots;

/**
 * Command is an enumeration that contains all possible commands for a robot
 * 
 * @author Ying Hao
 * @since 22 Feb 2018
 * @version 2.0
 */
public enum Command {
	/**
	 * START Command
	 */
	START((byte)(0x01)),
	/**
	 * SCAN Command
	 */
	SCAN((byte)(0x02)), 
	/**
	 * TURN LEFT Command
	 */
	TURN_LEFT((byte)0x03), 
	/**
	 * TURN RIGHT Command
	 */
	TURN_RIGHT((byte)0x04),
	/**
	 * FORWARD Command
	 */
	FORWARD((byte)0x05);
	
	public byte bytevalue;
	
	/**
	 * Creates a new instance of Command
	 * @param code
	 */
	private Command(byte bytevalue) {
		this.bytevalue = bytevalue;
	}

	/**
	 * Gets the byte value of the command
	 * @return
	 */
	public byte getByteValue() {
		return bytevalue;
	}

}
