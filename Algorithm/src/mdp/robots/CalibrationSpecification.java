package mdp.robots;

import java.awt.Point;

import mdp.models.CellState;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 * CalibrationSpecification provides methods to communicate path information
 * 
 * @author Ying Hao
 */
public class CalibrationSpecification {
	private RobotAction calibrationType;
	private SensorConfiguration[] sensors;
	
	public CalibrationSpecification(RobotAction calibrationType, SensorConfiguration... sensors) {
		this.calibrationType = calibrationType;
		this.sensors = sensors;
	}
	
	/**
	 * Validates if the current vicinity of the robot fulfils the condition for calibration
	 * @param up
	 * @param down
	 * @param left
	 * @param right
	 * @return
	 */
	public boolean isInPosition(RobotBase robot) {
		boolean inPosition = true;
		
		for(SensorConfiguration sensor: sensors) {
			Point location = robot.getSensorCoordinate(sensor);
			
			switch(robot.getSensorDirection(sensor)) {
				case UP:
					location = new Point(location.x, location.y + 1);
					break;
				case DOWN:
					location = new Point(location.x, location.y - 1);
					break;
				case LEFT:
					location = new Point(location.x - 1, location.y);
					break;
				case RIGHT:
					location = new Point(location.x + 1, location.y);
					break;
				default:
					break;
			}
			
			CellState state = robot.getMapState().getMapCellState(location);
			inPosition &= state == null || state == CellState.OBSTACLE;
		}
		
		return inPosition;
	}
	
	/**
	 * Gets the calibration type for this specification instance
	 * @return
	 */
	public RobotAction getCalibrationType() {
		return calibrationType;
	}

}
