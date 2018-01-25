package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mdp.graphics.map.MdpMap;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 * SimulatorRobot is an implementation of RobotBase that provides sensor reading data based on
 * simulation obstacles
 * 
 * @author Ying Hao
 */
public class SimulatorRobot extends RobotBase {
	private boolean[][] obstacles;
	private long delay;
	private Timer timer;
	private Point location;
	private Dimension rdim;
	
	/**
	 * Creates an instance of SimulatorRobot with a default delay timer of 1 second
	 * @param map
	 * @param orientation
	 */
	public SimulatorRobot(Direction orientation) {
		super(orientation);
	}
	
	/**
	 * Initializes this SimulatorRobot instance with simulation map data
	 * @param map
	 */
	public void init(MdpMap map) {
		Dimension mapdim = map.getMapCoordinateDimension();
		obstacles = new boolean[mapdim.width][mapdim.height];
		delay = 1000;
		timer = new Timer(true);
		location = map.getRobotMapPoint();
		rdim = map.getRobotDimension();
		
		for(int x = 0; x < obstacles.length; x++) {
			for(int y = 0; y < obstacles[x].length; y++) {
				obstacles[x][y] = map.getCellState(new Point(x, y)) == CellState.OBSTACLE;
			}
		}
	}
	
	/**
	 * Gets the delay timer specified in milliseconds
	 * @return
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Sets the delay timer specified in milliseconds
	 * @param delay
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Override
	public Map<SensorConfiguration, Integer> getSensorReading() {
		Map<SensorConfiguration, Integer> readings = new HashMap<>();
		List<SensorConfiguration> sensors = this.getSensors();
		
		for(SensorConfiguration sensor: sensors)
			readings.put(sensor, getObstacleDistance(sensor));
		
		return readings;
	}

	@Override
	protected void move(Direction mapdirection, RobotAction... actions) {
		if(mapdirection == Direction.UP)
			this.location = new Point(location.x, location.y + 1);
		else if(mapdirection == Direction.DOWN)
			this.location = new Point(location.x, location.y - 1);
		else if(mapdirection == Direction.LEFT)
			this.location = new Point(location.x - 1, location.y);
		else
			this.location = new Point(location.x + 1, location.x);
		
		timer.schedule(new NotifyTask(mapdirection, actions), delay);
	}
	
	/**
	 * Gets sensor direction in terms of map direction
	 * @param sensor
	 * @return
	 */
	public Direction getSensorDirection(SensorConfiguration sensor) {
		Direction orientation = this.getCurrentOrientation();
		Direction sdirection = sensor.getDirection();
		
		if(orientation == Direction.DOWN) {
			if(sdirection == Direction.UP)
				sdirection = Direction.DOWN;
			else if(sdirection == Direction.DOWN)
				sdirection = Direction.UP;
			else if(sdirection == Direction.LEFT)
				sdirection = Direction.RIGHT;
			else
				sdirection = Direction.LEFT;
		}
		else if(orientation == Direction.LEFT) {
			if(sdirection == Direction.UP)
				sdirection = Direction.LEFT;
			else if(sdirection == Direction.DOWN)
				sdirection = Direction.RIGHT;
			else if(sdirection == Direction.LEFT)
				sdirection = Direction.DOWN;
			else
				sdirection = Direction.UP;
		}
		else if(orientation == Direction.RIGHT) {
			if(sdirection == Direction.UP)
				sdirection = Direction.RIGHT;
			else if(sdirection == Direction.DOWN)
				sdirection = Direction.LEFT;
			else if(sdirection == Direction.LEFT)
				sdirection = Direction.UP;
			else
				sdirection = Direction.DOWN;
		}
		
		return sdirection;
	}
	
	/**
	 * Gets the sensor coordinates
	 * @param sensor
	 * @return
	 */
	private Point getSensorCoordinate(SensorConfiguration sensor) {
		Direction sdirection = this.getSensorDirection(sensor);
		Point scoordinate;
		
		if(sdirection == Direction.UP)
			scoordinate = new Point(location.x + sensor.getCoordinate(), location.y + rdim.height / 2);
		else if(sdirection == Direction.DOWN)
			scoordinate = new Point(location.x - sensor.getCoordinate(), location.y - rdim.height / 2);
		else if(sdirection == Direction.LEFT)
			scoordinate = new Point(location.x - rdim.width / 2, location.y + sensor.getCoordinate());
		else
			scoordinate = new Point(location.x + rdim.width / 2, location.y - sensor.getCoordinate());
		
		return scoordinate;
	}
	
	/**
	 * Gets the obstacle distance from the sensor
	 * @param sensor
	 * @return
	 */
	private int getObstacleDistance(SensorConfiguration sensor) {
		Direction sdirection = this.getSensorDirection(sensor);
		Point scoordinate = this.getSensorCoordinate(sensor);
		int distance = 0;
		
		if(sdirection == Direction.UP) {
			for(int y = scoordinate.y + 1; y < this.obstacles[scoordinate.x].length && distance == 0; y++)
				if(this.obstacles[scoordinate.x][y])
					distance = y - scoordinate.y;
		}
		else if(sdirection == Direction.DOWN) {
			for(int y = scoordinate.y - 1; y >= 0 && distance == 0; y--)
				if(this.obstacles[scoordinate.x][y])
					distance = scoordinate.y - y;
		}
		else if(sdirection == Direction.LEFT) {
			for(int x = scoordinate.x - 1; x >= 0 && distance == 0; x--)
				if(this.obstacles[x][scoordinate.y])
					distance = scoordinate.x - x;
		}
		else {
			for(int x = scoordinate.x + 1; x < this.obstacles.length; x++)
				if(this.obstacles[x][scoordinate.y])
					distance = x - scoordinate.x;
		}
		
		return distance;
	}
	
	/**
	 * NotifyTask is a TimerTask that notifies registered RobotActionListener on a specific
	 * robot action sequence completion
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
		}
		
	}
	
}
