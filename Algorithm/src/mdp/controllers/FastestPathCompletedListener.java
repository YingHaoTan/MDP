package mdp.controllers;

/**
 * FastestPathCompletedListener provides method needed for fastest path to perform notification
 * whenever the movement is completed
 * 
 * @author Ying Hao
 */
public interface FastestPathCompletedListener {
	
	/**
	 * Notifies listeners about a fastest path complete event
	 * @param state
	 */
	public void onFastestPathCompleted();

}
