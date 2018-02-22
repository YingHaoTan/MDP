package mdp.v2.models.maps;

/**
 * State is an enumeration that contains all possible states of a map cell
 * 
 * @author Ying Hao
 * @since 21 Feb 2018
 * @version 2.0
 */
public enum State {
	/**
	 * Unexplored Cell
	 */
	UNEXPLORED,
	/**
	 * Obstacle Cell
	 */
	OBSTACLE,
	/**
	 * Normal Cell
	 */
	NORMAL,
	/**
	 * Waypoint Cell
	 */
	WAYPOINT
}
