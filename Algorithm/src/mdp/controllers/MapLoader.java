package mdp.controllers;

import mdp.models.MapState;

/**
 * MapLoader provides contract method needed for MdpWindowController to delegate
 * map loading task
 * 
 * @author Ying Hao
 */
public interface MapLoader {
	/**
	 * Perform loading of map
	 */
	public void load(MapState map);
}