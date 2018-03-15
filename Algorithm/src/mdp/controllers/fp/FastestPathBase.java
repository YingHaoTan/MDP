package mdp.controllers.fp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.robots.CalibrationSpecification;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 * FastestPathBase is an abstract base class for all fastest path classes
 *
 * @author Ying Hao
 */
public abstract class FastestPathBase implements RobotActionListener {

    private boolean faststream;
    private MapState mstate;
    private List<FastestPathCompletedListener> listeners;
    private RobotBase robot;
    private Point destination;

    public FastestPathBase() {
        this.listeners = new ArrayList<>();
    }

    /**
     * Add FastestPathCompletedListener
     *
     * @param listener
     */
    public void addFastestPathCompletedListener(FastestPathCompletedListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove FastestPathCompletedListener
     *
     * @param listener
     */
    public void removeFastestPathCompletedListener(FastestPathCompletedListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Moves to the specified destination in robot coordinate using fastest path
     *
     * @param mstate
     * @param robot
     * @param destination
     * @param faststream - False indicates that the FastestPathBase should wait
     * for robot acknowledgement before issuing another command
     */
    public boolean move(MapState mstate, RobotBase robot, Point destination, boolean faststream) {
        this.mstate = mstate.clone();
        this.robot = robot;
        this.destination = destination;
        this.faststream = faststream;

        boolean success = preprocess();

        if (success) {
            robot.addRobotActionListener(this);
            if (faststream) {
                Direction direction;
                ArrayList<Direction> streamDirections = new ArrayList<>();
                while ((direction = next()) != null) {
                    streamDirections.add(direction);
                }
                
                if(streamDirections.size() > 0) {
                	reset();
                    robot.moveStream(streamDirections);
                }
                else {
                    complete();
                }
            } else {
                Direction direction = next();
                if (direction != null) {
                    robot.move(direction);
                } else {
                    complete();
                }
            }
        }

        return success;
    }

    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
    	if(mapdirection != null) {
	        Direction mdirection = next();
	
	        if (mdirection != null) {
	        	if(!faststream)
	            	robot.move(mdirection);
	        } else {
	            complete();
	        }
    	}
    }

    /**
     * Completes the fastest path by notifying listeners and removing robot
     * action listener
     */
    private void complete() {

        robot.removeRobotActionListener(this);
        
        if (faststream) {
            System.out.println("Fastest path stream completed. Will calibrate now..");
            RobotBase robot = getRobot();
            CalibrationSpecification spec = robot.getCalibrationSpecifications().get(0);
            if (spec.isInPosition(getRobot(), RobotAction.ABOUT_TURN)) {
                robot.move(RobotAction.ABOUT_TURN);
            } else if (spec.isInPosition(getRobot(), RobotAction.TURN_LEFT)) {
                robot.move(RobotAction.TURN_LEFT);
            } else if (spec.isInPosition(getRobot(), RobotAction.TURN_RIGHT)) {
                robot.move(RobotAction.TURN_RIGHT);
            }

            robot.dispatchCalibration(spec.getCalibrationType());
        }

        for (FastestPathCompletedListener listener : listeners) {
            listener.onFastestPathCompleted();
        }
    }

    /**
     * Gets the next map direction to move into
     *
     * @return
     */
    protected abstract Direction next();
    
    /**
     * Resets the policy current state
     */
    protected abstract void reset();

    /**
     * Performs preprocessing before starting to stream actions for robot
     */
    protected abstract boolean preprocess();

    /**
     * Gets the map state
     *
     * @return
     */
    protected MapState getMapState() {
        return mstate;
    }

    /**
     * Gets the robot
     *
     * @return
     */
    protected RobotBase getRobot() {
        return robot;
    }

    /**
     * Gets the destination
     *
     * @return
     */
    protected Point getDestination() {
        return destination;
    }

}
