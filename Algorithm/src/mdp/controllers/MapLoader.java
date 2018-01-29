package mdp.controllers;

import mdp.graphics.map.MdpMap;

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
	public void load(MdpMap map);
}