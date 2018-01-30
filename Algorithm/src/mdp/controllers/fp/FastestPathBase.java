package mdp.controllers.fp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.MapState;
import mdp.robots.RobotBase;

/**
 * FastestPathBase is an abstract base class for all fastest path classes
 *
 * @author Ying Hao
 */
public abstract class FastestPathBase {
	
	private MapState mstate;
	private List<FastestPathCompletedListener> listeners;
	private RobotBase robot;
	private Point destination;
	
	public FastestPathBase() {
		this.listeners = new ArrayList<>();
	}
	
	/**
	 * Add FastestPathCompletedListener
	 * @param listener
	 */
	public void addFastestPathCompletedListener(FastestPathCompletedListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Remove FastestPathCompletedListener
	 * @param listener
	 */
	public void removeFastestPathCompletedListener(FastestPathCompletedListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * Moves to the specified destination in robot coordinate using fastest path
	 * @param mstate
	 * @param robot
	 * @param destination
	 */
	public void move(MapState mstate, RobotBase robot, Point destination) {
		this.mstate = mstate;
		this.robot = robot;
		this.destination = destination;                
	}
	
	/**
	 * Notifies that the movement to the specified destination is completed
	 */
	protected void notifyMovementComplete() {
		for(FastestPathCompletedListener listener: listeners)
			listener.onFastestPathCompleted();
	}
	
	 /**
     * Gets the map state
     * @return
     */
    protected MapState getMapState() {
        return mstate;
    }

    /**
     * Gets the robot
     * @return
     */
    protected RobotBase getRobot() {
        return robot;
    }
    
    /**
     * Gets the destination
     * @return
     */
    protected Point getDestination() {
    	return destination;
    }

}
