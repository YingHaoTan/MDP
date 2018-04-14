package mdp.robots;

import mdp.models.Direction;
import mdp.models.RobotAction;

/**
 * RobotActionListener provides method needed for Robot instances to perform notification whenever
 * a specified action sequence is completed
 * 
 * @author Ying Hao
 */
public interface RobotActionListener {
	
	/**
	 * Callback method to be invoked when a sequence of robot actions are completed
	 * @param mapdirection
	 * @param actions
	 */
	public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions);

}
