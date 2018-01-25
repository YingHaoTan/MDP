package mdp.robots;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 * RobotBase is an abstract model class for a Robot
 * 
 * @author Ying Hao
 */
public abstract class RobotBase {
	private Direction orientation;
	private List<SensorConfiguration> sensors;
	private List<RobotActionListener> listeners;
	
	public RobotBase(Direction orientation) {
		this.orientation = orientation;
		this.sensors = new ArrayList<>();
		this.listeners = new ArrayList<>();
	}
	
	/**
	 * Installs a sensor into this robot instance
	 * @param sensor
	 */
	public void install(SensorConfiguration sensor) {
		this.sensors.add(sensor);
	}
	
	/**
	 * Gets a list of sensors installed in this robot instance
	 * @return
	 */
	public List<SensorConfiguration> getSensors() {
		return new ArrayList<>(this.sensors);
	}
	
	/**
	 * Gets the current orientation of this robot instance
	 * @return
	 */
	public Direction getCurrentOrientation() {
		return this.orientation;
	}
        
        /**
         * Sets the current orientation of this robot instance
         * 
         */
        private void setCurrentOrientation(Direction direction){
            this.orientation = direction;
        }
	
	/**
	 * Adds a RobotActionListener
	 * @param listener
	 */
	public void addRobotActionListener(RobotActionListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Removes a RobotActionListener
	 * @param listener
	 */
	public void removeRobotActionListener(RobotActionListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * Moves the robot in the specified map direction
	 * @param direction
	 */
	public void move(Direction mapdirection) {
		List<RobotAction> actionsequence = new ArrayList<>();
		
		// Generates the action to move the robot
		if(orientation == Direction.UP) {
			if(mapdirection == Direction.LEFT)
				actionsequence.add(RobotAction.TURN_LEFT);
			else if(mapdirection == Direction.RIGHT)
				actionsequence.add(RobotAction.TURN_RIGHT);
			else if(mapdirection == Direction.DOWN) {
				actionsequence.add(RobotAction.TURN_RIGHT);
				actionsequence.add(RobotAction.TURN_RIGHT);
			}
		}
		else if(orientation == Direction.DOWN) {
			if(mapdirection == Direction.RIGHT)
				actionsequence.add(RobotAction.TURN_LEFT);
			else if(mapdirection == Direction.LEFT)
				actionsequence.add(RobotAction.TURN_RIGHT);
			else if(mapdirection == Direction.UP) {
				actionsequence.add(RobotAction.TURN_RIGHT);
				actionsequence.add(RobotAction.TURN_RIGHT);
			}
		}
		else if(orientation == Direction.LEFT) {
			if(mapdirection == Direction.DOWN)
				actionsequence.add(RobotAction.TURN_LEFT);
			else if(mapdirection == Direction.UP)
				actionsequence.add(RobotAction.TURN_RIGHT);
			else if(mapdirection == Direction.RIGHT) {
				actionsequence.add(RobotAction.TURN_RIGHT);
				actionsequence.add(RobotAction.TURN_RIGHT);
			}
		}
		else {
			if(mapdirection == Direction.UP)
				actionsequence.add(RobotAction.TURN_LEFT);
			else if(mapdirection == Direction.DOWN)
				actionsequence.add(RobotAction.TURN_RIGHT);
			else if(mapdirection == Direction.LEFT) {
				actionsequence.add(RobotAction.TURN_RIGHT);
				actionsequence.add(RobotAction.TURN_RIGHT);
			}
		}
		
		actionsequence.add(RobotAction.FORWARD);
		
                setCurrentOrientation(mapdirection);
		// Performs the actual moving of the robot
		move(mapdirection, actionsequence.toArray(new RobotAction[0]));
	}
	
	/**
	 * Notifies all RobotActionListener instances registered to this Robot instance
	 * @param mapdirection
	 * @param actions
	 */
	protected void notify(Direction mapdirection, RobotAction[] actions) {
		for(RobotActionListener listener: listeners)
			listener.onRobotActionCompleted(mapdirection, actions);
	}
	
	/**
	 * Gets a map of sensor readings from the Robot where each integer correspond to number of blocks away
	 * from the robot an obstacle is detected
	 * @return
	 */
	public abstract Map<SensorConfiguration, Integer> getSensorReading();
        
        
        /**
	 * Gets the direction of sensor with reference to the grid, 
         * e.g. the sensor facing left when the robot is facing right becomes the sensor facing upwards
         * @param sensor
	 * @returns relative direction
	 */
	public abstract Direction getSensorDirection(SensorConfiguration sensor);
	
	/**
	 * Moves the robot by performing the actions in order
	 * @mapdirection
	 * @param actions
	 */
	protected abstract void move(Direction mapdirection, RobotAction... actions); 

}
