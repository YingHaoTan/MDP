package mdp.robots;

import java.util.ArrayList;
import java.util.List;

import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.RobotAction;

/**
 * Builder for a CalibrationSpecification
 * 
 * @author Ying Hao
 */
public class CalibrationSpecificationBuilder {
	private List<Direction> directions;
	private RobotAction calibrationType;
	
	public CalibrationSpecificationBuilder() {
		this.directions = new ArrayList<>();
		this.calibrationType = RobotAction.CAL_SIDE;
	}
	
	/**
	 * Adds the direction to be a part of the AND direction conditions required for identifying a CalibrationSpecification
	 * @param direction
	 */
	public CalibrationSpecificationBuilder add(Direction direction) {
		this.directions.add(direction);
		return this;
	}
	
	/**
	 * Sets the calibration type
	 * @param calibrationType
	 */
	public CalibrationSpecificationBuilder setCalibrationType(RobotAction calibrationType) {
		this.calibrationType = calibrationType;
		return this;
	}
	
	/**
	 * Builds the CalibrationSpecification
	 * @return
	 */
	public CalibrationSpecification build() {
		return new CalibrationSpecification() {

			@Override
			public boolean isInPosition(CellState up, CellState down, CellState left, CellState right) {
				boolean inPosition = true;
				
				for(Direction direction: directions) {
					switch(direction) {
						case UP:
							inPosition &= up == CellState.OBSTACLE;
							break;
						case DOWN:
							inPosition &= down == CellState.OBSTACLE;
							break;
						case LEFT:
							inPosition &= left == CellState.OBSTACLE;
							break;
						case RIGHT:
							inPosition &= right == CellState.OBSTACLE;
							break;
						default:
							break;
					}
				}
				
				return inPosition;
			}

			@Override
			public RobotAction getCalibrationType() {
				return calibrationType;
			}
			
		};
	}

}
