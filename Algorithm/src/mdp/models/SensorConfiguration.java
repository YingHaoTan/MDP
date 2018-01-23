package mdp.models;

/**
 * SensorConfiguration is a model class that represents a sensor configuration in a robot<br /><br />
 * SensorConfiguration has a 1-dimensional coordinate that moves perpendicular along the direction axis,
 * where the center of robot is 0
 * 
 * @author Ying Hao
 */
public class SensorConfiguration {
	private Direction direction;
	private int coordinate;
	private int maxdistance;
	private double reliability;
	
	public SensorConfiguration(Direction direction, int coordinate, int maxdistance, double reliability) {
		this.direction = direction;
		this.coordinate = coordinate;
	}
	
	/**
	 * Gets the direction the sensor is facing with reference to a robot
	 * @return
	 */
	public Direction getDirection() {
		return direction;
	}
	
	/**
	 * Gets the coordinate that this sensor is located
	 * @return
	 */
	public int getCoordinate() {
		return coordinate;
	}
	
	/**
	 * Gets the maximum distance that the sensor can acquire data
	 * @return
	 */
	public int getMaxDistance() {
		return maxdistance;
	}
	
	/**
	 * Gets the reliability of this sensor with 1.0 being the maximum and 0.0 being the minimum
	 * @return
	 */
	public double getReliability() {
		return reliability;
	}
	
}
