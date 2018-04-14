package mdp.controllers.explorer;

import java.awt.Point;

import mdp.models.CellState;

/**
 * CellStateUpdateListener provides method needed for exploration planner to perform notification
 * whenever their cell state is updated
 * 
 * @author Ying Hao
 */
public interface CellStateUpdateListener {
	
	/**
	 * Notifies listeners about a cell state update for an exploration planner
	 * @param location
	 * @param state
	 * @param label
	 */
	public void onCellStateUpdate(Point location, CellState state, String label);

}
