package mdp.controllers;

/**
 * ExplorationCompletedListener provides method needed for exploration planner to perform notification
 * whenever their exploration is completed
 * 
 * @author Ying Hao
 */
public interface ExplorationCompletedListener {
	
	/**
	 * Notifies listeners about a exploration complete event
	 * @param state
	 */
	public void onExplorationComplete();

}
