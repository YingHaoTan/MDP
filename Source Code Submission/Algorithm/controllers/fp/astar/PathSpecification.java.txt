package mdp.controllers.fp.astar;

import java.awt.Point;
import java.util.List;
import mdp.models.MapState;
import mdp.models.State;
import mdp.robots.RobotBase;

/**
 * PathSpecification provides methods to communicate path information
 * 
 * @author Ying Hao
 */
public interface PathSpecification {
	
	/**
	 * Calculates the initial state based on the specified map state and robot
	 * @param mstate
	 * @param robot
	 * @return
	 */
	public State calculateInitialState(MapState mstate, RobotBase robot);
	
	/**
	 * Gets a list of successor states for the specified mapstate and state
	 * @param state
	 * @return
	 */
	public List<State> getAvailableSuccessorStates(MapState mstate, State state);
	
	/**
	 * Gets a flag indicating if the specified state is a terminal state based on the given destination point
	 * @param state
	 * @return
	 */
	public boolean isTerminalState(Point destination, State state);
	
	/**
	 * Calculates the path cost based on the specified mapstate, current state and next state
	 * @param state
	 * @param action
	 * @return
	 */
	public double calculatePathCost(MapState mstate, State cstate, State nstate);
	
	/**
	 * Calculates the heuristic value for the specified state
	 * @param mstate
	 * @param stata
	 * @return
	 */
	public double calculateHeuristicValue(MapState mstate, Point destination, State state);

}
