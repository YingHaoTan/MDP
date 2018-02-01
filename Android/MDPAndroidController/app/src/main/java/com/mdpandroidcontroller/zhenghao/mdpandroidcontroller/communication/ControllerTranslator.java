package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;
/**
 * ControllerTranslator translates instructions between robot and controller for the controller
 * 
 * @author ernes
 *
 */
public class ControllerTranslator {
	//variables and constructors TBC
	
	/**
	 * Generate message to command robot to explore the maze
	 * 
	 * @return
	 */
	public String commandExplore() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_EXPLORE;
		return message;
	}
	
	/**
	 * Generate message to command robot to move via the fastest path to the end
	 * 
	 * @return
	 */
	public String commandFastestPath() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_FASTESTPATH;
		return message;
	}

	/**
	 * Generate message to command robot to move forward
	 * 
	 * @return
	 */
	public String commandMoveForward() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_MOVE + CommConstants.COMMAND_MOVE_FORWARD;
		return message;
	}
	
	/**
	 * Generate message to command robot to turn right
	 * 
	 * @return
	 */
	public String commandTurnRight() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_MOVE + CommConstants.COMMAND_MOVE_RIGHT_TURN;
		return message;
	}
	
	/**
	 * Generate message to command robot to turn left
	 * 
	 * @return
	 */
	public String commandTurnLeft() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_MOVE + CommConstants.COMMAND_MOVE_LEFT_TURN;
		return message;
	}
	
	/**
	 * 
	 * Generate message to command robot to move backwards
	 * 
	 * @return
	 */
	public String commandMoveBack() {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_MOVE + CommConstants.COMMAND_MOVE_BACK;
		return message;
	}
	
	/**
	 * Generate message to command robot to set a waypoint at a specified location
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String commandWayPoint(int x, int y) {
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_WAYPOINT;
		
		if(x < 10)
			message += 0;
		message += x;
		if(y < 10)
			message+=0;
		message+= y;
		
		return message;
	}
	
	/**
	 * Decodes the message sent from the robot
	 * 
	 * @param message
	 */
	public void decodeMessage(String message) {
		int x;
		int y;
		boolean isBlocked;
		if(message.substring(0,2).equals(CommConstants.MESSAGE_TYPE_STATUS)) {
			if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_ROBOT)) {
				x = Integer.parseInt(message.substring(4, 6));
				y = Integer.parseInt(message.substring(6, 8));
				//find direction of robot from message.substring(8,10)
				//update update map
				return;
			}else if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_MAP)) {
				x = Integer.parseInt(message.substring(4, 6));
				y = Integer.parseInt(message.substring(6, 8));
				isBlocked = (message.substring(8, 10) == CommConstants.MAP_TYPE_BLOCK);
				//update map
				return;
			}
		}
		// print error if required
	}
}
