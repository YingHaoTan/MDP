package mdp.robots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
	
	/**
	 * Creates an instance of SimulatorRobot based on the simulation obstacle and robot direction
	 * with a default delay timer of 1 second
	 * @param obstacles
	 * @param orientation
	 */
	public SimulatorRobot(boolean[][] obstacles, Direction orientation) {
		super(orientation);
		
		delay = 1000;
		timer = new Timer(true);
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
		
		// TODO Auto-generated method stub
		
		return readings;
	}

	@Override
	protected void move(Direction mapdirection, RobotAction... actions) {
		timer.schedule(new NotifyTask(mapdirection, actions), delay);
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
