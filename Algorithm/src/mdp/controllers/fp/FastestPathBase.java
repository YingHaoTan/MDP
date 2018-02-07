package mdp.controllers.fp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 * FastestPathBase is an abstract base class for all fastest path classes
 *
 * @author Ying Hao
 */
public abstract class FastestPathBase implements RobotActionListener {

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
	 * @param faststream - False indicates that the FastestPathBase should wait for robot acknowledgement before issuing another command
	 */
	public void move(MapState mstate, RobotBase robot, Point destination, boolean faststream) {
		this.mstate = mstate.clone();
		this.robot = robot;
		this.destination = destination;

		robot.addRobotActionListener(this);
		if(faststream) {
			Direction direction;
			while((direction = next()) != null)
				robot.move(direction);
		}
		else {
			robot.move(next());
		}
	}

	@Override
	public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
		Direction mdirection = next();
		if(mdirection != null)
			robot.move(mdirection);
		else {
			robot.removeRobotActionListener(this);
			for(FastestPathCompletedListener listener: listeners)
				listener.onFastestPathCompleted();
		}
	}

	/**
	 * Gets the next map direction to move into
	 * @return
	 */
	protected abstract Direction next();

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