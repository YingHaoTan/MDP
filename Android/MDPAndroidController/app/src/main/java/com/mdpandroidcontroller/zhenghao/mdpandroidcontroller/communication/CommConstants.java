package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.communication;

/**
 * CommConstants stores constants for messages between devices
 *
 * @author ernest
 *
 */
public class CommConstants {

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
			
	public static final String MESSAGE_TYPE_STATUS	= "ST";
	
		public static final String STATUS_TYPE_ROBOT	= "RO";

			public static final String ROBOT_MOVING				= "MO";
			public static final String ROBOT_TURNING			= "TU";
			public static final String ROBOT_STOPPED			= "ST";
			public static final String ROBOT_DIRECTION_UP		= "UP";
			public static final String ROBOT_DIRECTION_DOWN		= "DN";
			public static final String ROBOT_DIRECTION_LEFT		= "LE";
			public static final String ROBOT_DIRECTION_RIGHT	= "RI";
		
		public static final String STATUS_TYPE_MAP		= "MP";
			
			public static final String MAP_TYPE_CLEAR	= "CL";
			public static final String MAP_TYPE_BLOCK	= "BL";

	public static final String MESSAGE_TYPE_CONFIG	= "CF";
	//to add more configuration patterns in the future

	
	
	
	
}
