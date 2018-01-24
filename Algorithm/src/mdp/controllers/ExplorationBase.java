package mdp.controllers;

import java.util.ArrayList;
import java.util.List;

import mdp.robots.RobotBase;

/**
 * ExplorationBase is an abstract base class for all exploration planner classes
 * 
 * @author Ying Hao
 */
public abstract class ExplorationBase {
	private List<CellStateUpdateListener> listeners;
	
	public ExplorationBase() {
		this.listeners = new ArrayList<>();
	}
	
	/**
	 * Performs exploration with the provided robot
	 * @param robot
	 */
	public abstract void explore(RobotBase robot);
	
	/**
	 * Adds CellStateUpdateListener
	 * @param listener
	 */
	public void addCellStateUpdateListener(CellStateUpdateListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Removes CellStateUpdateListener
	 * @param listener
	 */
	public void removeCellStateUpdateListener(CellStateUpdateListener listener) {
		this.listeners.remove(listener);
	}

}
