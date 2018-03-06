package mdp.controllers;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import org.json.JSONObject;

import mdp.controllers.explorer.ExplorationBase;
import mdp.controllers.fp.FastestPathBase;
import mdp.graphics.ExecutionMode;
import mdp.models.CellState;
import mdp.models.CommConstants;
import mdp.models.Direction;
import mdp.models.MapDescriptorFormat;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.robots.PhysicalRobot;
import mdp.robots.RobotBase;
import mdp.robots.SimulatorRobot;
import mdp.tcp.AndroidCommandsTranslator;
import mdp.tcp.AndroidInstruction;
import mdp.tcp.AndroidUpdate;
import mdp.tcp.StatusMessage;

/**
 * XController is the main controller responsible for routing controls between different 
 * controllers
 * 
 * @author Ying Hao
 */
public class XController {
	private MapState mstate;
	private SimulatorRobot srobot;
	private PhysicalRobot probot;
	private ExplorationBase explorer;
	private FastestPathBase planner;
	private MdpWindowController wcontroller;
	private Queue<AndroidUpdate> outgoingAndroidQueue;
	private Semaphore outgoingSemaphore;
	private AndroidCommandsTranslator androidTranslator;

	public XController(MapState mstate, List<Consumer<AndroidInstruction>> androidInstructionListenerList, Queue<AndroidUpdate> outgoingAndroidQueue, Semaphore outgoingSemaphore) {
		this.mstate = mstate;
		this.outgoingAndroidQueue = outgoingAndroidQueue;
		this.outgoingSemaphore = outgoingSemaphore;
		this.androidTranslator = new AndroidCommandsTranslator();
		
		androidInstructionListenerList.add(this::handleAndroidInstruction);
	}
	
	/**
	 * Gets the current map state
	 * @return
	 */
	public MapState getMapState() {
		return mstate;
	}
	
	/**
	 * Gets the simulator robot
	 * @return
	 */
	public SimulatorRobot getSimulatorRobot() {
		return srobot;
	}

	/**
	 * Sets the simulator robot
	 * @param srobot
	 */
	public void setSimulatorRobot(SimulatorRobot srobot) {
		this.srobot = srobot;
	}

	/**
	 * Gets the physical robot
	 * @return
	 */
	public RobotBase getPhysicalRobot() {
		return probot;
	}

	/**
	 * Sets the physical robot
	 * @param probot
	 */
	public void setPhysicalRobot(PhysicalRobot probot) {
		this.probot = probot;
	}

	/**
	 * Sets the explorer
	 * @return
	 */
	public ExplorationBase getExplorer() {
		return explorer;
	}

	/**
	 * Gets the explorer
	 * @param explorer
	 */
	public void setExplorer(ExplorationBase explorer) {
		this.explorer = explorer;
	}

	/**
	 * Gets the fastest path planner
	 * @return
	 */
	public FastestPathBase getFastestPathPlanner() {
		return planner;
	}

	/**
	 * Sets the fastest path planner
	 * @param planner
	 */
	public void setFastestPathPlanner(FastestPathBase planner) {
		this.planner = planner;
	}
	
	/**
	 * Gets the window controller
	 * @return
	 */
	public MdpWindowController getWindowController() {
		return wcontroller;
	}

	/**
	 * Sets the window controller
	 * @param wcontroller
	 */
	public void setWindowController(MdpWindowController wcontroller) {
		this.wcontroller = wcontroller;
	}

	/**
	 * Directs control to exploration controller
	 * @param mode Execution mode
	 * @param mstate Map state, ignored when execution mode is PHYSICAL
	 * @param coverage Percentage coverage
	 * @param timelimit Time limit
	 */
	public void explore(ExecutionMode mode, int coverage, double timelimit) {
		RobotBase robot = getActiveRobot(mode);
		
		if(robot != null) {
			MapState simulationState = mstate.clone();
	                
			mstate.setMapCellState(CellState.UNEXPLORED);
	
			Set<Point> exploredpoints = new HashSet<>(mstate.convertRobotPointToMapPoints(mstate.getRobotPoint()));
			exploredpoints.addAll(mstate.convertRobotPointToMapPoints(mstate.getEndPoint()));
	
			for (Point p : exploredpoints)
				mstate.setMapCellState(p, CellState.NORMAL);
	
			if(mstate.getWayPoint() != null)
				mstate.setMapCellState(mstate.getWayPoint(), CellState.WAYPOINT);
	                
	        robot.init(mstate.clone());
	        if(mode == ExecutionMode.SIMULATION)
	        	srobot.setSimulationMapState(simulationState);
	
			if (explorer != null)
				explorer.explore(robot, coverage, timelimit);
		}
	}

	/**
	 * Directs control to fastest path controller
	 * @param mode Execution mode
	 * @param mstate Map state
	 */
	public void fastestpath(ExecutionMode mode) {
		RobotBase robot = getActiveRobot(mode);
		if(robot != null && planner != null)
			planner.move(mstate, robot, mstate.getEndPoint(), true);
	}
	
	/**
	 * Initializes the map state with the given starting coordinate and waypoint
	 * @param waypoint
	 * @param coordinate
	 */
	public void initialize(Point coordinate, Point waypoint, Direction orientation) {
		mstate.setStartPoint(coordinate);
		if(waypoint != null)
			mstate.setMapCellState(waypoint, CellState.WAYPOINT);
		mstate.reset();
                
        if(srobot != null)
            srobot.setCurrentOrientation(orientation);
        if(probot != null)
            probot.setCurrentOrientation(orientation);
		
		if(wcontroller != null)
			wcontroller.requestSynchronization();
	}
	
	/**
	 * Gets the active robot for the specified execution mode
	 * @param mode
	 * @return
	 */
	public RobotBase getActiveRobot(ExecutionMode mode) {
		return mode == ExecutionMode.PHYSICAL ? probot : srobot;
	}
	
	/**
	 * Performs a reset on the entire game
	 * @param mstate
	 */
	public void reset(MapState mstate) {
		mstate.reset();
		if(srobot != null)
			srobot.reset();
		if(probot != null)
			probot.reset();
		
		if(mstate.getWayPoint() != null)
			mstate.setMapCellState(mstate.getWayPoint(), CellState.WAYPOINT);
	}
	
	private void handleAndroidInstruction(AndroidInstruction instruction) {
		// Handle android instruction
		System.out.println("Android instruction received: " + instruction.getMessage());
		//androidTranslator.sendArena(mstate.toString(MapDescriptorFormat.MDF1), mstate.toString(MapDescriptorFormat.MDF2));
	
		JSONObject jsonObj = new JSONObject(instruction.getMessage());

		String messageType = jsonObj.getString(CommConstants.JSONNAME_TYPE);
		if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_EXPLORE)){
			explore(ExecutionMode.PHYSICAL, 100, -1);
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_FASTESTPATH)){
			fastestpath(ExecutionMode.PHYSICAL);
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_STARTPOSITION)){
			String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
			String orientation = jsonObj.getString(CommConstants.JSONNAME_ORIENTATION);
			int x = getXFromString(coordinate);
			int y = getYFromString(coordinate);
			Direction d = getDirFromString(orientation);
			
			initialize(new Point(x, y), getMapState().getWayPoint(), d);
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_WAYPOINT)){
			String coordinate = jsonObj.getString(CommConstants.JSONNAME_COORDINATE);
			int x = getXFromString(coordinate);
			int y = getYFromString(coordinate);
			
			initialize(getMapState().getStartPoint(), new Point(x, y), getActiveRobot(ExecutionMode.PHYSICAL).getCurrentOrientation());
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_MOVE)){
			String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
			if(option.equals(CommConstants.MOVE_FORWARD)){
				getActiveRobot(ExecutionMode.PHYSICAL).move(RobotAction.FORWARD);
			}else if(option.equals(CommConstants.MOVE_RIGHTTURN)){
				getActiveRobot(ExecutionMode.PHYSICAL).move(RobotAction.TURN_RIGHT);
			}else if(option.equals(CommConstants.MOVE_LEFTTURN)){
				getActiveRobot(ExecutionMode.PHYSICAL).move(RobotAction.TURN_LEFT);
			}else if(option.equals(CommConstants.MOVE_BACKWARD)){
				// do move backward
			}
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_RESET)){
			getMapState().reset();
		}else if(messageType.equals(CommConstants.CONTROLLER_MESSAGE_UPDATE)){
			String option = jsonObj.getString(CommConstants.JSONNAME_OPTION);
			if(option.equals(CommConstants.UPDATE_AUTO)){
				System.out.println("UPDATE AUTO CALLED");
			}else if(option.equals(CommConstants.UPDATE_MANUAL)){
				
			}else if(option.equals(CommConstants.UPDATE_NOW)){
				Point rpoint = mstate.getRobotPoint();
				RobotBase robot = getActiveRobot(ExecutionMode.PHYSICAL);
				
				sendAndroidUpdate(new AndroidUpdate(androidTranslator.sendArena(mstate.toString(MapDescriptorFormat.MDF1), mstate.toString(MapDescriptorFormat.MDF2))));
				sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotPosition(rpoint.x, rpoint.y, robot.getCurrentOrientation())));
			}
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
	
	private void sendAndroidUpdate(AndroidUpdate message) {
		outgoingAndroidQueue.offer(message);
		outgoingSemaphore.release();
	}

}
