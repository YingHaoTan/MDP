package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
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

    private Dimension dimension;
    private Direction initialorientation;
    private Direction orientation;
    private List<SensorConfiguration> sensors;
    private List<CalibrationSpecification> cspecs;
    private List<RobotActionListener> listeners;

    public RobotBase(Dimension dimension, Direction orientation) {
        this.dimension = dimension;
        this.initialorientation = orientation;
        this.orientation = orientation;
        this.sensors = new ArrayList<>();
        this.cspecs = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Installs a sensor into this robot instance
     *
     * @param sensor
     */
    public void install(SensorConfiguration sensor) {
        this.sensors.add(sensor);
    }

    /**
     * Gets a list of sensors installed in this robot instance
     *
     * @return
     */
    public List<SensorConfiguration> getSensors() {
        return new ArrayList<>(this.sensors);
    }
    
    /**
     * Gets a list of calibration specifications added to this robot instance
     * @return
     */
    public List<CalibrationSpecification> getCalibrationSpecifications() {
    	return new ArrayList<>(this.cspecs);
    }

    /**
     * Gets the robot dimension
     *
     * @return
     */
    public Dimension getDimension() {
        return this.dimension;
    }

    /**
     * Gets the current orientation of this robot instance
     *
     * @return
     */
    public Direction getCurrentOrientation() {
        return this.orientation;
    }

    /**
     * Sets the current orientation of this robot instance
     *
     */
    private void setCurrentOrientation(Direction direction) {
        this.orientation = direction;
    }

    /**
     * Adds a RobotActionListener
     *
     * @param listener
     */
    public void addRobotActionListener(RobotActionListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a RobotActionListener
     *
     * @param listener
     */
    public void removeRobotActionListener(RobotActionListener listener) {
        this.listeners.remove(listener);
    }
    
    /**
     * Adds a CalibrationSpecification
     * @param spec
     */
    public void addCalibrationSpecification(CalibrationSpecification spec) {
    	this.cspecs.add(spec);
    }
    
    /**
     * Removes a CalibrationSpecification
     * @param spec
     */
    public void removeCalibrationSpecification(CalibrationSpecification spec) {
    	this.cspecs.remove(spec);
    }

    /**
     * Moves the robot in the specified map direction
     *
     * @param direction
     */
    public void move(Direction mapdirection) {
        List<RobotAction> actionsequence = new ArrayList<>();

        // Generates the action to move the robot
        if (orientation == Direction.UP) {
            if (mapdirection == Direction.LEFT) {
                actionsequence.add(RobotAction.TURN_LEFT);
            } else if (mapdirection == Direction.RIGHT) {
                actionsequence.add(RobotAction.TURN_RIGHT);
            } else if (mapdirection == Direction.DOWN) {
                actionsequence.add(RobotAction.TURN_RIGHT);
                actionsequence.add(RobotAction.TURN_RIGHT);
            }
        } else if (orientation == Direction.DOWN) {
            if (mapdirection == Direction.RIGHT) {
                actionsequence.add(RobotAction.TURN_LEFT);
            } else if (mapdirection == Direction.LEFT) {
                actionsequence.add(RobotAction.TURN_RIGHT);
            } else if (mapdirection == Direction.UP) {
                actionsequence.add(RobotAction.TURN_RIGHT);
                actionsequence.add(RobotAction.TURN_RIGHT);
            }
        } else if (orientation == Direction.LEFT) {
            if (mapdirection == Direction.DOWN) {
                actionsequence.add(RobotAction.TURN_LEFT);
            } else if (mapdirection == Direction.UP) {
                actionsequence.add(RobotAction.TURN_RIGHT);
            } else if (mapdirection == Direction.RIGHT) {
                actionsequence.add(RobotAction.TURN_RIGHT);
                actionsequence.add(RobotAction.TURN_RIGHT);
            }
        } else if (mapdirection == Direction.UP) {
            actionsequence.add(RobotAction.TURN_LEFT);
        } else if (mapdirection == Direction.DOWN) {
            actionsequence.add(RobotAction.TURN_RIGHT);
        } else if (mapdirection == Direction.LEFT) {
            actionsequence.add(RobotAction.TURN_RIGHT);
            actionsequence.add(RobotAction.TURN_RIGHT);
        }

        actionsequence.add(RobotAction.FORWARD);

        setCurrentOrientation(mapdirection);

        // Performs the actual moving of the robot
        move(mapdirection, actionsequence.toArray(new RobotAction[0]));
    }

    public void moveStream(ArrayList<Direction> streamDirections) {
        
        List<RobotAction> actionsequence = new ArrayList<>();
        List<Direction> orientations = new ArrayList<>();
        for (int i = 0; i < streamDirections.size(); i++) {
            Direction mapdirection = streamDirections.get(i);

            if (orientation == Direction.UP) {
                if (mapdirection == Direction.LEFT) {
                    actionsequence.add(RobotAction.TURN_LEFT);
                } else if (mapdirection == Direction.RIGHT) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                } else if (mapdirection == Direction.DOWN) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                    actionsequence.add(RobotAction.TURN_RIGHT);
                }
            } else if (orientation == Direction.DOWN) {
                if (mapdirection == Direction.RIGHT) {
                    actionsequence.add(RobotAction.TURN_LEFT);
                } else if (mapdirection == Direction.LEFT) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                } else if (mapdirection == Direction.UP) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                    actionsequence.add(RobotAction.TURN_RIGHT);
                }
            } else if (orientation == Direction.LEFT) {
                if (mapdirection == Direction.DOWN) {
                    actionsequence.add(RobotAction.TURN_LEFT);
                } else if (mapdirection == Direction.UP) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                } else if (mapdirection == Direction.RIGHT) {
                    actionsequence.add(RobotAction.TURN_RIGHT);
                    actionsequence.add(RobotAction.TURN_RIGHT);
                }
            } else if (mapdirection == Direction.UP) {
                actionsequence.add(RobotAction.TURN_LEFT);
            } else if (mapdirection == Direction.DOWN) {
                actionsequence.add(RobotAction.TURN_RIGHT);
            } else if (mapdirection == Direction.LEFT) {
                actionsequence.add(RobotAction.TURN_RIGHT);
                actionsequence.add(RobotAction.TURN_RIGHT);
            }
            
            actionsequence.add(RobotAction.FORWARD);
            orientations.add(mapdirection);
            setCurrentOrientation(mapdirection);
        }
        
        moveRobotStream(actionsequence, orientations);

    }

    public void move(RobotAction action) {
        if (action == RobotAction.TURN_LEFT || action == RobotAction.TURN_RIGHT) {
            move(null, action);
            Direction newDirection = getCurrentOrientation();

            if (action == RobotAction.TURN_RIGHT) {
                switch (getCurrentOrientation()) {
                    case UP:
                        newDirection = Direction.RIGHT;
                        break;
                    case DOWN:
                        newDirection = Direction.LEFT;
                        break;
                    case LEFT:
                        newDirection = Direction.UP;
                        break;
                    case RIGHT:
                        newDirection = Direction.DOWN;
                        break;

                }
            } else {
                switch (getCurrentOrientation()) {
                    case UP:
                        newDirection = Direction.LEFT;
                        break;
                    case DOWN:
                        newDirection = Direction.RIGHT;
                        break;
                    case LEFT:
                        newDirection = Direction.DOWN;
                        break;
                    case RIGHT:
                        newDirection = Direction.UP;
                        break;
                }
            }

            setCurrentOrientation(newDirection);

        } else {
            move(orientation, action);
        }
    }

    /*
    *  Things to do when you stop the robot
     */
    public abstract void stop();

    /**
     * Resets the robot orientation
     */
    public void reset() {
        orientation = initialorientation;
    }

    /**
     * Notifies all RobotActionListener instances registered to this Robot
     * instance
     *
     * @param mapdirection
     * @param actions
     */
    protected void notify(Direction mapdirection, RobotAction[] actions) {
        for (RobotActionListener listener : new ArrayList<>(listeners)) {
            listener.onRobotActionCompleted(mapdirection, actions);
        }
    }

    /**
     * Gets a map of sensor readings from the Robot where each integer
     * correspond to number of blocks away from the robot an obstacle is
     * detected
     *
     * @return
     */
    public abstract Map<SensorConfiguration, Integer> getSensorReading();

    /**
     * Gets the direction of sensor with reference to the grid, e.g. the sensor
     * facing left when the robot is facing right becomes the sensor facing
     * upwards
     *
     * @param sensor
     * @returns relative direction
     */
    public abstract Direction getSensorDirection(SensorConfiguration sensor);

    /**
     * Gets map coordinates of sensor
     *
     * @param sensor
     * @return
     */
    public abstract Point getSensorCoordinate(SensorConfiguration sensor);

    /**
     * Moves the robot by performing the actions in order
     *
     * @mapdirection
     * @param actions
     */
    protected abstract void move(Direction mapdirection, RobotAction... actions);

    
    /**
     * Have to set orientation inside here...
     */
    protected abstract void moveRobotStream(List<RobotAction> actions, List<Direction> orientations);
}
