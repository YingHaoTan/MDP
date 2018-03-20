package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;

import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 * SimulatorRobot is an implementation of RobotBase that provides sensor reading
 * data based on simulation obstacles
 *
 * @author Ying Hao
 */
public class SimulatorRobot extends RobotBase {
	private MapState simulationMapState;
    private Queue<NotifyTask> taskqueue;
    private long delay;

    /**
     * Creates an instance of SimulatorRobot with a default delay timer of 1
     * second
     *
     * @param map
     * @param orientation
     */
    public SimulatorRobot(Dimension dimension, Direction orientation) {
        super(dimension, orientation);
        taskqueue = new LinkedList<>();
        delay = 10;
    }

    /**
     * Gets the delay timer specified in milliseconds
     *
     * @return
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Sets the delay timer specified in milliseconds
     *
     * @param delay
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    /**
     * Gets the simulation map state
     * @return
     */
    public MapState getSimulationMapState() {
    	return simulationMapState;
    }
    
    /**
     * Sets the simulation map state
     * @param mstate
     */
    public void setSimulationMapState(MapState mstate) {
    	this.simulationMapState = mstate;
    }

    @Override
    public Map<SensorConfiguration, Integer> getSensorReading() {
        Map<SensorConfiguration, Integer> readings = new HashMap<>();
        List<SensorConfiguration> sensors = this.getSensors();

        for (SensorConfiguration sensor : sensors) {
            readings.put(sensor, getObstacleDistance(sensor));
        }

        return readings;
    }

    @Override
    protected void dispatchMovement(Direction mapdirection, RobotAction... actions) {
        
        
    	MapState mstate = this.getMapState();
        Point location = mstate.getRobotPoint();

        if (mapdirection == Direction.UP) {
            mstate.setRobotPoint(new Point(location.x, location.y + 1));
        } else if (mapdirection == Direction.DOWN) {
            mstate.setRobotPoint(new Point(location.x, location.y - 1));
        } else if (mapdirection == Direction.LEFT) {
            mstate.setRobotPoint(new Point(location.x - 1, location.y));
        } else if (mapdirection == Direction.RIGHT) {
            mstate.setRobotPoint(new Point(location.x + 1, location.y));
        }

        //for (RobotAction action : actions) {
        //    System.out.println("In Simulator Robot: " + action);
        //}

        NotifyTask task = new NotifyTask(mapdirection, actions);
        taskqueue.offer(task);
        if (taskqueue.size() == 1) {
            this.getScheduler().schedule(task, delay);
        }
    }
    
    @Override
	public void dispatchCalibration(RobotAction action) {
            //System.out.println("Calibration Data: " + action);
	}

    @Override
    protected void moveRobotStream(List<RobotAction> actions, List<Direction> orientations) {
        
        
        int orientationIndex = 0;
        MapState mstate = getMapState();

        for (Direction mapdirection : orientations) {
            Point location = mstate.getRobotPoint();
            
            if (mapdirection == Direction.UP) {
                mstate.setRobotPoint(new Point(location.x, location.y + 1));
            } else if (mapdirection == Direction.DOWN) {
                mstate.setRobotPoint(new Point(location.x, location.y - 1));
            } else if (mapdirection == Direction.LEFT) {
                mstate.setRobotPoint(new Point(location.x - 1, location.y));
            } else if (mapdirection == Direction.RIGHT) {
                mstate.setRobotPoint(new Point(location.x + 1, location.y));
            }
        }
        
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == RobotAction.TURN_LEFT || actions.get(i) == RobotAction.TURN_RIGHT) {
                NotifyTask task = new NotifyTask(null, new RobotAction[] {actions.get(i)});
                taskqueue.offer(task);
                if (taskqueue.size() == 1) {
                    this.getScheduler().schedule(task, delay);
                }
            }
            else{
                NotifyTask task = new NotifyTask(orientations.get(orientationIndex++), new RobotAction[] {actions.get(i)});
                taskqueue.offer(task);
                if (taskqueue.size() == 1) {
                    this.getScheduler().schedule(task, delay);
                }
                
            }
        }
        
    }

    /**
     * Gets the obstacle distance from the sensor
     *
     * @param sensor
     * @return
     */
    private int getObstacleDistance(SensorConfiguration sensor) {
        
    	MapState mstate = this.getSimulationMapState();
        Direction sdirection = this.getSensorDirection(sensor);
        Point scoordinate = this.getSensorCoordinate(sensor);
        Dimension mdim = mstate.getMapSystemDimension();
        int distance = 0;

        if (sdirection == Direction.UP) {
            for (int y = scoordinate.y + 1; y < mdim.height && distance == 0; y++) {
                if (mstate.getMapCellState(new Point(scoordinate.x, y)) == CellState.OBSTACLE) {
                    distance = y - scoordinate.y;
                }
            }
        } else if (sdirection == Direction.DOWN) {
            for (int y = scoordinate.y - 1; y >= 0 && distance == 0; y--) {
                if (mstate.getMapCellState(new Point(scoordinate.x, y)) == CellState.OBSTACLE) {
                    distance = scoordinate.y - y;
                }
            }
        } else if (sdirection == Direction.LEFT) {
            for (int x = scoordinate.x - 1; x >= 0 && distance == 0; x--) {
                if (mstate.getMapCellState(new Point(x, scoordinate.y)) == CellState.OBSTACLE) {
                    distance = scoordinate.x - x;
                }
            }
        } else {
            for (int x = scoordinate.x + 1; x < mdim.width && distance == 0; x++) {
                if (mstate.getMapCellState(new Point(x, scoordinate.y)) == CellState.OBSTACLE) {
                    distance = x - scoordinate.x;
                }
            }
        }

        /*
        
        // Simulate false readings
        double reliability = sensor.getReliability();
        double seed = Math.random();
        
        // Send error readings
        if(seed > reliability && distance > 0){
            distance = (Math.random() >= 0.5) ? distance + 1 : distance - 1;
            //distance = distance - 1;
        }
        */
        
        return distance;
    }

    @Override
    public void stop() {
        System.out.println("Simulated Robot stopped.");
    }

    /**
     * NotifyTask is a TimerTask that notifies registered RobotActionListener on
     * a specific robot action sequence completion
     *
     * @author Ying Hao
     */
    private class NotifyTask extends TimerTask {

        private Direction mapdirection;
        private RobotAction[] actions;

        public NotifyTask(Direction mapdirection, RobotAction[] actions) {
            this.mapdirection = mapdirection;
            this.actions = actions;
        }

        @Override
        public void run() {
            SimulatorRobot.this.notify(mapdirection, actions);
            SimulatorRobot.this.taskqueue.poll();

            if (SimulatorRobot.this.taskqueue.size() > 0) {
                SimulatorRobot.this.getScheduler().schedule(SimulatorRobot.this.taskqueue.peek(), delay);
            }
        }

    }

}
