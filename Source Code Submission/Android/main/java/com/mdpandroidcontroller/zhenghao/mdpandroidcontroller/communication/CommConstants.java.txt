package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;

/**
 * CommConstants stores constants for messages between devices
 *
 * @author ernest
 *
 */
public class CommConstants {

	//json names
	public static final String JSONNAME_TYPE		= "messagetype";
	public static final String JSONNAME_COORDINATE	= "coordinate";
	public static final String JSONNAME_ORIENTATION	= "orientation";
	public static final String JSONNAME_OPTION		= "option";
	public static final String JSONNAME_MDF1		= "mdf1";
	public static final String JSONNAME_MDF2		= "mdf2";

	//common
	public static final String COMMON_UP 	= "up";
	public static final String COMMON_RIGHT	= "right";
	public static final String COMMON_DOWN	= "down";
	public static final String COMMON_LEFT	= "left";

	public static final String MOVE_FORWARD 	= "forward";
	public static final String MOVE_RIGHTTURN 	= "rightturn";
	public static final String MOVE_LEFTTURN 	= "leftturn";
	public static final String MOVE_BACKWARD 	= "backward";

	public static final String UPDATE_AUTO		= "auto";
	public static final String UPDATE_MANUAL	= "manual";
	public static final String UPDATE_NOW		= "now";

	public static final String ROBOT_MOVING 	= "moving";
	public static final String ROBOT_TURNING 	= "turning";
	public static final String ROBOT_STOPPED 	= "stopped";

	public static final String GRID_OBSTACLE 	= "obs";
	public static final String GRID_CLEAR 		= "clear";

	public static final String CONFIG_STRING 	= "config";

	// message type from controller
	public static final String CONTROLLER_MESSAGE_EXPLORE		= "explore";
	public static final String CONTROLLER_MESSAGE_FASTESTPATH	= "fastestpath";
	public static final String CONTROLLER_MESSAGE_STARTPOSITION	= "startpos";
	public static final String CONTROLLER_MESSAGE_WAYPOINT		= "waypoint";
	public static final String CONTROLLER_MESSAGE_MOVE			= "move";
	public static final String CONTROLLER_MESSAGE_RESET			= "reset";
	public static final String CONTROLLER_MESSAGE_UPDATE		= "update";

	// message type from robot
	public static final String ROBOT_MESSAGE_STATUS 	= "status";
	public static final String ROBOT_MESSAGE_POSITION 	= "position";
	public static final String ROBOT_MESSAGE_GRID 		= "grid";
	public static final String ROBOT_MESSAGE_MAP 		= "map";


	
}
