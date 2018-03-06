package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.CellState;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.Direction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

		void onDoRobotPos(int x, int y, Direction d);

		void onDoMapUpdateFull(String mdf1, String mdf2);

		void onDoMapUpdatePartial(int x, int y, CellState cs);
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
	 * Generate message to let robot know its auto update mode
	 *
	 * @return
	 */
	public String commandUpdateAuto(){
		String message = CommConstants.COMMAND_TYPE_UPDATE + CommConstants.UPDATE_AUTO;

		return message;
	}

	/**
	 * Generate message to let robot know its manual update mode
	 *
	 * @return
	 */
	public String commandUpdateManual(){
		String message = CommConstants.COMMAND_TYPE_UPDATE + CommConstants.UPDATE_MANUAL;

		return message;
	}

	/**
	 * Generate message to tell robot to update android controller during manual mode
	 *
	 * @return
	 */
	public String commandUpdateNow(){
		String message = CommConstants.COMMAND_TYPE_UPDATE + CommConstants.UPDATE_MANUAL + CommConstants.MANUAL_UPDATE_NOW;

		return message;
	}

	/**
	 * Generate message to ask robot to forget arena
	 *
	 * @return
	 */
	public String commandReset(){
		String message = CommConstants.MESSAGE_TYPE_COMMAND + CommConstants.COMMAND_TYPE_RESET;

		return message;
	}

	/**
	 * generate string with configuration information to robot
	 *
	 * To be written in the future after collaboration with algo
	 *
	 * @return
	 */
	public String generateConfigString(String config){
		String message = CommConstants.MESSAGE_TYPE_CONFIG + config;

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
					return;
				}else if(message.substring(4,6).equals(CommConstants.ROBOT_TURNING)){
					//do turning actions
					mParentActivity.onDoTurn();
					return;
				}else if(message.substring(4,6).equals(CommConstants.ROBOT_STOPPED)){
					//do stopping actions
					mParentActivity.onDoStop();
					return;
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
						mParentActivity.onDoRobotPos(x,y,d);
						return;
					}catch (NumberFormatException exception){
						//handle error
					}
				}
			}else if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_MAP)) {
				try{
					x = Integer.parseInt(message.substring(4, 6));
					y = Integer.parseInt(message.substring(6, 8));
					//update map
					if(message.substring(8, 10).equals(CommConstants.MAP_TYPE_BLOCK)){
						mParentActivity.onDoMapUpdatePartial(x,y,CellState.OBSTACLE);
					}else{
						mParentActivity.onDoMapUpdatePartial(x,y,CellState.NORMAL);
					}
					return;
				}catch(NumberFormatException exception){
					//handle error
				}
			}else if(message.substring(2, 4).equals(CommConstants.STATUS_TYPE_MDF)){
				String arr[] = message.split(CommConstants.DELIMITER);
				String mdf1 = arr[1];
				String mdf2 = arr[2];
				mParentActivity.onDoMapUpdateFull(mdf1 , mdf2);
			}
		}
		// proceed to read input for amd for now
		readFromAMD(message);

		// print error here if required
	}

	/**
	 * method to pass android checklist
	 *
	 * @param message
	 */
	private void readFromAMD(String message){
		JSONArray array = null;
		String value = null;
		try {

			JSONObject jsonObj = new JSONObject(message);
			JSONArray jsonAry = jsonObj.names();

			if(jsonAry.get(0).toString().equals("robotPosition")){
				value = jsonObj.getString("robotPosition");
				value = value.substring(1,value.length()-1);
				String arr[] = value.split(",");
				int x = Integer.parseInt(arr[0]);
				int y = Integer.parseInt(arr[1]);
				int d = Integer.parseInt(arr[2]);

				Direction dir;

				switch(d){
					case 0:
						dir = Direction.UP;
						break;
					case 90:
						dir = Direction.RIGHT;
						break;
					case 180:
						dir = Direction.DOWN;
						break;
					case 270:
						dir = Direction.LEFT;
						break;
					default:
						dir = Direction.UP;
						break;
				}
				mParentActivity.onDoRobotPos(x,y,dir);

			}else if(jsonAry.get(0).toString().equals("grid")){
				value = jsonObj.getString("grid");
				String mdf1 = "";

				// set mdf1 to "know" all grid in the maze for AMD tool
				while(mdf1.length() < 76){
					mdf1 += "F";
				}
				mParentActivity.onDoMapUpdateFull(mdf1, value);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
