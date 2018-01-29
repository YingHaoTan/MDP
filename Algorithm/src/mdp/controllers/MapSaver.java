package mdp.controllers;

import mdp.graphics.map.MdpMap;

/**
 * MapSaver provides contract method needed for MdpWindowController to delegate map saving task
 * 
 * @author Ying Hao
 */
public interface MapSaver {
	/**
	 * Perform saving of map
	 */
	public void save(MdpMap map);
}
