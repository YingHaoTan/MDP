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

    public static final String MESSAGE_TYPE_COMMAND = "CM";

    public static final String COMMAND_TYPE_EXPLORE = "EX";
    public static final String COMMAND_TYPE_FASTESTPATH = "FP";
    public static final String COMMAND_TYPE_ROBOT_POS = "RP";

    public static final String ROBOT_POS_UP = "UP";
    public static final String ROBOT_POS_DOWN = "DN";
    public static final String ROBOT_POS_LEFT = "LE";
    public static final String ROBOT_POS_RIGHT = "RI";

    public static final String COMMAND_TYPE_WAYPOINT = "WP";
    public static final String COMMAND_TYPE_MOVE = "MV";

    public static final String COMMAND_MOVE_FORWARD = "FW";
    public static final String COMMAND_MOVE_BACK = "BK";
    public static final String COMMAND_MOVE_LEFT_TURN = "LT";
    public static final String COMMAND_MOVE_RIGHT_TURN = "RT";

    public static final String COMMAND_TYPE_UPDATE = "UD";

    public static final String UPDATE_AUTO = "AU";
    public static final String UPDATE_MANUAL = "MA";

    public static final String MANUAL_UPDATE_NOW = "UN";

    public static final String COMMAND_TYPE_RESET = "RS";

    public static final String MESSAGE_TYPE_STATUS = "ST";

    public static final String STATUS_TYPE_ROBOT = "RO";

    public static final String ROBOT_MOVING = "MO";
    public static final String ROBOT_TURNING = "TU";
    public static final String ROBOT_STOPPED = "SP";
    public static final String ROBOT_DIRECTION_UP = "UP";
    public static final String ROBOT_DIRECTION_DOWN = "DN";
    public static final String ROBOT_DIRECTION_LEFT = "LE";
    public static final String ROBOT_DIRECTION_RIGHT = "RI";

    public static final String STATUS_TYPE_MAP = "MP";

    public static final String MAP_TYPE_CLEAR = "CL";
    public static final String MAP_TYPE_BLOCK = "BL";

    public static final String STATUS_TYPE_MDF = "MD";

    public static final String MESSAGE_TYPE_CONFIG = "CF";
    //to add more configuration patterns in the future

    public static final String DELIMITER = "~";

}
