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
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_EXPLORE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * Generate message to command robot to move via the fastest path to the end
	 * 
	 * @return
	 */
	public String commandFastestPath() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_FASTESTPATH);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to command robot to move forward
	 * 
	 * @return
	 */
	public String commandMoveForward() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_MOVE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.MOVE_FORWARD);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * Generate message to command robot to turn right
	 * 
	 * @return
	 */
	public String commandTurnRight() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_MOVE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.MOVE_RIGHTTURN);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * Generate message to command robot to turn left
	 * 
	 * @return
	 */
	public String commandTurnLeft() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_MOVE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.MOVE_LEFTTURN);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * 
	 * Generate message to command robot to move backwards
	 * 
	 * @return
	 */
	public String commandMoveBack() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_MOVE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.MOVE_BACKWARD);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
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
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_STARTPOSITION);
			jsonObject.put(CommConstants.JSONNAME_COORDINATE , (sx+sy));
			jsonObject.put(CommConstants.JSONNAME_ORIENTATION , sd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to command robot to set a waypoint at a specified location
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public String commandWayPoint(int x, int y) {
		String sx = "";
		String sy = "";
		String sd = "";

		if(x < 10)
			sx = "0";
		sx += x;

		if(y < 10)
			sy="0";
		sy+= y;

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_WAYPOINT);
			jsonObject.put(CommConstants.JSONNAME_COORDINATE , (sx+sy));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to let robot know its auto update mode
	 *
	 * @return
	 */
	public String commandUpdateAuto(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_UPDATE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.UPDATE_AUTO);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to let robot know its manual update mode
	 *
	 * @return
	 */
	public String commandUpdateManual(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_UPDATE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.UPDATE_MANUAL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to tell robot to update android controller during manual mode
	 *
	 * @return
	 */
	public String commandUpdateNow(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_UPDATE);
			jsonObject.put(CommConstants.JSONNAME_OPTION , CommConstants.UPDATE_NOW);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * Generate message to ask robot to forget arena
	 *
	 * @return
	 */
	public String commandReset(){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONTROLLER_MESSAGE_RESET);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	/**
	 * generate string with configuration information to robot
	 *
	 * To be written in the future after collaboration with algo
	 *
	 * @return
	 */
	public String generateConfigString(String config){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(CommConstants.JSONNAME_TYPE , CommConstants.CONFIG_STRING);
			jsonObject.put(CommConstants.JSONNAME_OPTION , config);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * Decodes the message sent from the robot
	 * 
	 * @param message
	 */
	public void decodeMessage(String message) {

		try{
			JSONObject jsonObj = new JSONObject(message);

			String messageType = jsonObj.getString(CommConstants.JSONNAME_TYPE);
			if(messageType.equals(CommConstants.ROBOT_MESSAGE_STATUS)){
				String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
				if(option.equals(CommConstants.ROBOT_MOVING)){
					mParentActivity.onDoMove();
				}else if(option.equals(CommConstants.ROBOT_TURNING)){
					mParentActivity.onDoTurn();
				}else if(option.equals(CommConstants.ROBOT_STOPPED)){
					mParentActivity.onDoStop();
				}
			}else if(messageType.equals(CommConstants.ROBOT_MESSAGE_POSITION)){
				String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
				String orientation = jsonObj.getString(CommConstants.JSONNAME_ORIENTATION);
				int x = getXFromString(coordinate);
				int y = getYFromString(coordinate);
				Direction d = getDirFromString(orientation);
				mParentActivity.onDoRobotPos(x,y,d);
			}else if(messageType.equals(CommConstants.ROBOT_MESSAGE_GRID)){
				String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
				String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
				int x = getXFromString(coordinate);
				int y = getYFromString(coordinate);
				CellState cs = getCSFromString(option);
				mParentActivity.onDoMapUpdatePartial(x,y,cs);
			}else if(messageType.equals(CommConstants.ROBOT_MESSAGE_MAP)){
				String mdf1 = jsonObj.getString(CommConstants.JSONNAME_MDF1);
				String mdf2 = jsonObj.getString(CommConstants.JSONNAME_MDF2);
				mParentActivity.onDoMapUpdateFull(mdf1,mdf2);
			}

		}catch (JSONException e) {
			e.printStackTrace();
		}
		// message error if manage to reach here

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
