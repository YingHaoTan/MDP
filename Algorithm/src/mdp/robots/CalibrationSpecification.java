package mdp.robots;

import mdp.models.CellState;
import mdp.models.RobotAction;

/**
 * CalibrationSpecification provides methods to communicate path information
 * 
 * @author Ying Hao
 */
public interface CalibrationSpecification {
	
	/**
	 * Validates if the current vicinity of the robot fulfils the condition for calibration
	 * @param up
	 * @param down
	 * @param left
	 * @param right
	 * @return
	 */
	public boolean isInPosition(CellState up, CellState down, CellState left, CellState right);
	
	/**
	 * Gets the calibration type for this specification instance
	 * @return
	 */
	public RobotAction getCalibrationType();

}
