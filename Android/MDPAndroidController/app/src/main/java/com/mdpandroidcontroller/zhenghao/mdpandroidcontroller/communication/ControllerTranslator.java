package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.Direction;

/**
 * ControllerTranslator translates instructions between robot and controller for the controller
 * this is a singleton class
 * 
 * @author ernes
 *
 */
public class ControllerTranslator {

	public interface ControllerTranslatorCallBack {
		void onDoMove();

		void onDoTurn();

		void onDoStop();

		void onDoMapUpdateFull();

		void onDoMapUpdatePartial();
	}

	//variables and constructors
	private static final String TAG = "ControllerTranslator";
	private static ControllerTranslator instance = null;
	private ControllerTranslatorCallBack mParentActivity = null;

	private ControllerTranslator(){
		//to prevent instantiation elsewhere. edit accordingly if required.
	}

	public static ControllerTranslator getInstance(){
		if(instance == null) {
			instance = new ControllerTranslator();
		}
		return instance;
	}

	public ControllerTranslator withParentActivity (ControllerTranslatorCallBack parentActivity) {
		mParentActivity = parentActivity;
		return this;
	}

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
	 * Generate message to let robot know its starting position
	 *
	 * @param x
	 * @param y
	 * @param d
	 * @return
	 */
	public String commandRobotStartPos(int x, int y, Direction d){
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_ROBOT_POS;

		if(x < 10)
			message += 0;
		message += x;
		if(y < 10)
			message+=0;
		message+= y;

		switch(d){
			case UP:
				message += CommConstants.ROBOT_POS_UP;
				break;
			case DOWN:
				message += CommConstants.ROBOT_POS_DOWN;
				break;
			case LEFT:
				message += CommConstants.ROBOT_POS_LEFT;
				break;
			case RIGHT:
				message += CommConstants.ROBOT_POS_RIGHT;
				break;
		}

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
	 * generate string with configuration information to robot
	 *
	 * To be written in the future after collaboration with algo
	 *
	 * @return
	 */
	public String generateConfiString(){
		String message = CommConstants.MESSAGE_TYPE_CONFIG;

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
		Direction d;
		boolean isBlocked;
		if(message.substring(0,2).equals(CommConstants.MESSAGE_TYPE_STATUS)) {
			if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_ROBOT)) {
				if(message.substring(4,6).equals(CommConstants.ROBOT_MOVING)){
					//do moving actions
					mParentActivity.onDoMove();
				}else if(message.substring(4,6).equals(CommConstants.ROBOT_TURNING)){
					//do turning actions
					mParentActivity.onDoTurn();
				}else if(message.substring(4,6).equals(CommConstants.ROBOT_STOPPED)){
					//do stopping actions
					mParentActivity.onDoStop();
				}else{
					try{
						x = Integer.parseInt(message.substring(4, 6));
						y = Integer.parseInt(message.substring(6, 8));
						//find direction of robot from message.substring(8,10)
						String temp = message.substring(8, 10);
						if(temp.equals(CommConstants.ROBOT_DIRECTION_UP)){
							d = Direction.UP;
						}else if (temp.equals(CommConstants.ROBOT_DIRECTION_DOWN)){
							d = Direction.DOWN;
						}else if(temp.equals(CommConstants.ROBOT_DIRECTION_LEFT)){
							d = Direction.LEFT;
						}else if(temp.equals(CommConstants.ROBOT_DIRECTION_RIGHT)){
							d = Direction.RIGHT;
						}else{
							//error
							return;
						}
						//update robot position on map
						return;
					}catch (NumberFormatException exception){
						//handle error
					}
				}
			}else if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_MAP)) {
				try{
					x = Integer.parseInt(message.substring(4, 6));
					y = Integer.parseInt(message.substring(6, 8));
					isBlocked = (message.substring(8, 10) == CommConstants.MAP_TYPE_BLOCK);
					//update map
					mParentActivity.onDoMapUpdatePartial();
					return;
				}catch(NumberFormatException exception){
					//handle error
				}
			}
		}
		// print error here if required
	}
}
