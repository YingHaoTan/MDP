package mdp.controllers;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import mdp.controllers.explorer.ExplorationBase;
import mdp.controllers.fp.FastestPathBase;
import mdp.graphics.ExecutionMode;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;
import mdp.robots.PhysicalRobot;
import mdp.robots.RobotBase;
import mdp.robots.SimulatorRobot;
import mdp.tcp.AndroidInstruction;
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
	private Queue<StatusMessage> outgoingAndroidQueue;
	private Semaphore outgoingSemaphore;

	public XController(MapState mstate, List<Consumer<AndroidInstruction>> androidInstructionListenerList, Queue<StatusMessage> outgoingAndroidQueue, Semaphore outgoingSemaphore) {
		this.mstate = mstate;
		this.outgoingAndroidQueue = outgoingAndroidQueue;
		this.outgoingSemaphore = outgoingSemaphore;
		
		androidInstructionListenerList.add(this::handleAndroidInstruction);
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
                mstate.setRobotPoint(coordinate);
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
	}
	
	@SuppressWarnings("unused")
	private void sendAndroidStatusMessage(StatusMessage message) {
		outgoingAndroidQueue.offer(message);
		outgoingSemaphore.release();
	}

}
