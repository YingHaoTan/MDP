package mdp.v2.models.robots;

/**
 * Sensor is an immutable model class that encapsulates sensor configurations
 * 
 * @author Ying Hao
 * @since 21 Feb 2018
 * @version 2.0
 */
public class Sensor {
	private Orientation orientation;
	private int coordinate;
	private int maxdistance;
	private double reliability;
	
	/**
	 * Creates a new instance of Sensor
	 * @param orientation The orientation of the sensor with reference to the robot orientation
	 * @param coordinate The coordinate of the sensor
	 * @param maxdistance The maximum distance that this sensor instance can accurately read
	 * @param reliability The reliability value of this sensor instance
	 */
	public Sensor(Orientation orientation, int coordinate, int maxdistance, double reliability) {
		this.orientation = orientation;
		this.coordinate = coordinate;
		this.maxdistance = maxdistance;
		this.reliability = reliability;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public int getCoordinate() {
		return coordinate;
	}

	public int getMaxdistance() {
		return maxdistance;
	}

	public double getReliability() {
		return reliability;
	}

}
