/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.tcp;

import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.CommConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * RobotTranslator translates messages between the robot and controller for the robot itself
 * put in algo
 * 
 * @author ernest
 *
 */
public class AndroidCommandsTranslator {
	//variables and constructors TBC

	/**
	 * Generate message to update controller that robot is moving
	 *
	 * @return
	 */
	public String robotMoving(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_STATUS);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.ROBOT_MOVING);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to update controller that robot is turning
	 *
	 * @return
	 */
	public String robotTurning(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_STATUS);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.ROBOT_TURNING);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to update controller that robot has stopped
	 *
	 * @return
	 */
	public String robotStopped(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_STATUS);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.ROBOT_STOPPED);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to update controller about the location and orientation of the robot
	 * 
	 * @param x
	 * @param y
	 * @param d
	 * @return
	 */
	public String robotPosition(int x, int y, Direction d) {
		String sx = "";
		String sy = "";
		String sd = "";

		if(x < 10)
			sx = "0";
		sx += x;

		if(y < 10)
			sy="0";
		sy+= y;

		switch(d){
			case UP:
				sd = CommConstants.COMMON_UP;
				break;
			case DOWN:
				sd = CommConstants.COMMON_DOWN;
				break;
			case LEFT:
				sd = CommConstants.COMMON_LEFT;
				break;
			case RIGHT:
				sd = CommConstants.COMMON_RIGHT;
				break;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_POSITION);
			jsonObject.put(CommConstants.JSONNAME_COORDINATE , (sx+sy));
			jsonObject.put(CommConstants.JSONNAME_ORIENTATION , sd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate Message to update controller about a certain block
	 * 
	 * @param x
	 * @param y
	 * @param isBlocked
	 * @return
	 */
	public String gridStatus(int x , int y, boolean isBlocked) {
		String sx = "";
		String sy = "";
		String option = "";

		if(x < 10)
			sx = "0";
		sx += x;

		if(y < 10)
			sy="0";
		sy+= y;
		
		if(isBlocked) {
			option = CommConstants.GRID_OBSTACLE;
		}else {
			option = CommConstants.GRID_CLEAR;
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_GRID);
			jsonObject.put(CommConstants.JSONNAME_COORDINATE , (sx+sy));
			jsonObject.put(CommConstants.JSONNAME_OPTION , option);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message for sending the MDF file to the android controller
	 *
	 * @param mdf1
	 * @param mdf2
	 * @return
	 */
	public String sendArena(String mdf1 , String mdf2){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.ROBOT_MESSAGE_MAP);
			jsonObject.put(CommConstants.JSONNAME_MDF1 , mdf1);
			jsonObject.put(CommConstants.JSONNAME_MDF2 , mdf2);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * 
	 * Decodes a message that was sent from the controller
	 * to decode message at the algo side when received from android
	 * @param message
	 */
	public void decodeMessage(String message) {
		try{
			JSONObject jsonObj = new JSONObject(message);

			String messageType = jsonObj.getString(CommConstants.JSONNAME_TYPE);
			if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_EXPLORE)){
				// do explore
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_FASTESTPATH)){
				//do fastest path
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_STARTPOSITION)){
				String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
				String orientation = jsonObj.getString(CommConstants.JSONNAME_ORIENTATION);
				int x = getXFromString(coordinate);
				int y = getYFromString(coordinate);
				Direction d = getDirFromString(orientation);
				// do update robot location with x,y,d
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_WAYPOINT)){
				String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
				int x = getXFromString(coordinate);
				int y = getYFromString(coordinate);
				//do update waypoint with x,y
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_MOVE)){
				String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
				if(option.equals(CommConstants.MOVE_FORWARD)){
					// do move forward
				}else if(option.equals(CommConstants.MOVE_RIGHTTURN)){
					// do right turn
				}else if(option.equals(CommConstants.MOVE_LEFTTURN)){
					// do left turn
				}else if(option.equals(CommConstants.MOVE_BACKWARD)){
					// do move backward
				}
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_RESET)){
				// do reset map
			}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_UPDATE)){
				String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
				if(option.equals(CommConstants.UPDATE_AUTO)){
					// do set to auto update mode
				}else if(option.equals(CommConstants.UPDATE_MANUAL)){
					// do set to  manual update mode
				}else if(option.equals(CommConstants.UPDATE_NOW)){
					// do update android arena
				}
			}
		}catch (JSONException e){
			e.printStackTrace();
		}
	}

	private int getXFromString(String coordinate){
		return Integer.parseInt(coordinate.substring(0,2));
	}

	private int getYFromString(String coordinate){
		return Integer.parseInt(coordinate.substring(2,4));
	}

	private Direction getDirFromString(String orientation){
		Direction dir = Direction.UP;

		if (orientation.equals(CommConstants.COMMON_UP)){
			dir = Direction.DOWN;
		}else if(orientation.equals(CommConstants.COMMON_UP)){
			dir = Direction.LEFT;
		}else if(orientation.equals(CommConstants.COMMON_UP)){
			dir = Direction.RIGHT;
		}

		return dir;
	}

	private CellState getCSFromString(String cell){
		CellState cs = CellState.UNEXPLORED;
		if(cell.equals(CommConstants.GRID_CLEAR)){
			cs = CellState.NORMAL;
		}else if(cell.equals(CommConstants.GRID_OBSTACLE)){
			cs = CellState.OBSTACLE;
		}
		return cs;
	}


}
