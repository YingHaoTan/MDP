package mdp.v2.models.robots;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * RobotBase is an abstract base class for all robot models
 * 
 * @author Ying Hao
 * @since 21 Feb 2018
 * @version 2.0
 */
public abstract class RobotBase {
	private int width;
	private int height;
	private List<Sensor> sensors;
	
	/**
	 * Creates an instance of RobotBase
	 * @param width
	 * @param height
	 */
	public RobotBase(int width, int height) {
		this.width = width;
		this.height = height;
		this.sensors = new ArrayList<>();
	}
	
	/**
	 * Gets the size of the RobotBase
	 */
	public Dimension getSize() {
		return new Dimension(width, height);
	}
	
	/**
	 * Installs a sensor in this RobotBase instance
	 * @param sensor
	 */
	public void install(Sensor sensor) {
		this.sensors.add(sensor);
	}

	/**
	 * Gets all sensors installed in this RobotBase instance 
	 * @return
	 */
	public List<Sensor> getSensors() {
		return sensors;
	}
	
	/**
	 * Gets sensor readings from all sensors installed in this RobotBase instance
	 * @return
	 */
	public abstract Map<Sensor, Integer> getSensorReadings();
	
	/**
	 * Sends a command to the robot
	 * @param command Command to be sent to the robot
	 * @param state A state object that will be passed in to the callback function
	 * @param callback A callback function that will be invoked when the command has been sent successfully to the robot
	 */
	public abstract <T> void send(Command command, T state, Function<T, Void> callback);

}
