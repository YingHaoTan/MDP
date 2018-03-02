package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
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

    private Timer timer;
    private MapState mstate;
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
     * Initializes this SimulatorRobot instance with simulation map data
     *
     * @param map
     */
    public void init(MapState mstate) {
        this.timer = new Timer(true);
        this.mstate = mstate.clone();
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
    protected void move(Direction mapdirection, RobotAction... actions) {
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
        
        /*for(RobotAction action : actions){
            System.out.println(action);
        }*/

        NotifyTask task = new NotifyTask(mapdirection, actions);
        taskqueue.offer(task);
        if (taskqueue.size() == 1) {
            timer.schedule(task, delay);
        }
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
        List<Point> points = this.mstate.convertRobotPointToMapPoints(this.mstate.getRobotPoint());
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
     * Gets the obstacle distance from the sensor
     *
     * @param sensor
     * @return
     */
    private int getObstacleDistance(SensorConfiguration sensor) {
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

        return distance;
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
                timer.schedule(SimulatorRobot.this.taskqueue.peek(), delay);
            }
        }

    }

}
