/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.models;

/**
 * CommConstants stores constants for messages between devices
 *
 * @author ernest
 *
 */
public class CommConstants {

	/*
	public static final String MESSAGE_TYPE_COMMAND	= "CM";
	
		public static final String COMMAND_TYPE_EXPLORE		= "EX";
		public static final String COMMAND_TYPE_FASTESTPATH = "FP";
		public static final String COMMAND_TYPE_ROBOT_POS	= "RP";

			public static final String ROBOT_POS_UP		= "UP";
			public static final String ROBOT_POS_DOWN	= "DN";
			public static final String ROBOT_POS_LEFT	= "LE";
			public static final String ROBOT_POS_RIGHT	= "RI";

		public static final String COMMAND_TYPE_WAYPOINT	= "WP";
		public static final String COMMAND_TYPE_MOVE		= "MV";
		
			public static final String COMMAND_MOVE_FORWARD		= "FW";
			public static final String COMMAND_MOVE_BACK		= "BK";
			public static final String COMMAND_MOVE_LEFT_TURN	= "LT";
			public static final String COMMAND_MOVE_RIGHT_TURN	= "RT";

		public static final String COMMAND_TYPE_UPDATE	= "UD";

			public static final String UPDATE_AUTO 			= "AU";
			public static final String UPDATE_MANUAL 		= "MA";

				public static final String MANUAL_UPDATE_NOW	= "UN";

		public static final String COMMAND_TYPE_RESET	= "RS";
			
	public static final String MESSAGE_TYPE_STATUS	= "ST";
	
		public static final String STATUS_TYPE_ROBOT	= "RO";

			public static final String ROBOT_MOVING				= "MO";
			public static final String ROBOT_TURNING			= "TU";
			public static final String ROBOT_STOPPED			= "SP";
			public static final String ROBOT_DIRECTION_UP		= "UP";
			public static final String ROBOT_DIRECTION_DOWN		= "DN";
			public static final String ROBOT_DIRECTION_LEFT		= "LE";
			public static final String ROBOT_DIRECTION_RIGHT	= "RI";
		
		public static final String STATUS_TYPE_MAP		= "MP";
			
			public static final String MAP_TYPE_CLEAR	= "CL";
			public static final String MAP_TYPE_BLOCK	= "BL";

		public static final String STATUS_TYPE_MDF		= "MD";

	public static final String MESSAGE_TYPE_CONFIG	= "CF";
	//to add more configuration patterns in the future
	*/

	public static final String DELIMITER = "~";

	//constant to change message format to json

	//json names
	public static final String JSONNAME_TYPE		= "messagetype";
	public static final String JSONNAME_COORDINATE	= "coordinate";
	public static final String JSONNAME_ORIENTATION	= "orientation";
	//public static final String JSONNAME_ACTION	= "action";
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

	public static final String ROBOT_MOVING = "moving";
	public static final String ROBOT_TURNING = "turning";
	public static final String ROBOT_STOPPED = "stopped";

	public static final String GRID_OBSTACLE 	= "obs";
	public static final String GRID_CLEAR 		= "clear";

	public static final String CONFIG_STRING = "config";

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
