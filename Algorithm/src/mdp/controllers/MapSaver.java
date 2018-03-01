package mdp.controllers;

import mdp.models.MapState;

/**
 * MapSaver provides contract method needed for MdpWindowController to delegate map saving task
 * 
 * @author Ying Hao
 */
public interface MapSaver {
	/**
	 * Perform saving of map
	 */
	public void save(MapState map);
}
