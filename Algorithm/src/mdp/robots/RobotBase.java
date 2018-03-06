package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import mdp.models.Direction;
import mdp.models.MapState;
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
    private Timer scheduler;
    private MapState mstate;
    private List<SensorConfiguration> sensors;
    private List<CalibrationSpecification> cspecs;
    private List<RobotActionListener> listeners;

    public RobotBase(Dimension dimension, Direction orientation) {
        this.dimension = dimension;
        this.initialorientation = orientation;
        this.orientation = orientation;
        this.scheduler = new Timer();
        this.sensors = new ArrayList<>();
        this.cspecs = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Initializes this robot with the MapState
     * @param mstate
     */
    public void init(MapState mstate) {
    	this.mstate = mstate;
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
     * Gets sensor direction in terms of map direction
     *
     * @param sensor
     * @return
     */
    public Direction getSensorDirection(SensorConfiguration sensor) {
        Direction orientation = this.getCurrentOrientation();
        Direction sdirection = sensor.getDirection();

        if (orientation == Direction.DOWN) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.DOWN;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.UP;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.RIGHT;
            } else {
                sdirection = Direction.LEFT;
            }
        } else if (orientation == Direction.LEFT) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.LEFT;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.RIGHT;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.DOWN;
            } else {
                sdirection = Direction.UP;
            }
        } else if (orientation == Direction.RIGHT) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.RIGHT;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.LEFT;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.UP;
            } else {
                sdirection = Direction.DOWN;
            }
        }

        return sdirection;
    }

    /**
     * Gets the sensor coordinates
     *
     * @param sensor
     * @return
     */
    public Point getSensorCoordinate(SensorConfiguration sensor) {
    	MapState mstate = this.getMapState();
        List<Point> points = mstate.convertRobotPointToMapPoints(mstate.getRobotPoint());
        Point location = points.get(points.size() / 2);

        Dimension rdim = mstate.getRobotDimension();
        Direction sdirection = this.getSensorDirection(sensor);
        Point scoordinate;

        if (sdirection == Direction.UP) {
            scoordinate = new Point(location.x + sensor.getCoordinate(), location.y + rdim.height / 2);
        } else if (sdirection == Direction.DOWN) {
            scoordinate = new Point(location.x - sensor.getCoordinate(), location.y - rdim.height / 2);
        } else if (sdirection == Direction.LEFT) {
            scoordinate = new Point(location.x - rdim.width / 2, location.y + sensor.getCoordinate());
        } else {
            scoordinate = new Point(location.x + rdim.width / 2, location.y - sensor.getCoordinate());
        }

        return scoordinate;
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
     * Gets the scheduler
     * @return
     */
    public Timer getScheduler() {
    	return this.scheduler;
    }
    
    /**
     * Gets the MapState
     * @return
     */
    public MapState getMapState() {
    	return this.mstate;
    }

    /**
     * Sets the current orientation of this robot instance
     *
     */
    public void setCurrentOrientation(Direction direction) {
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
            move(null, action);
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
