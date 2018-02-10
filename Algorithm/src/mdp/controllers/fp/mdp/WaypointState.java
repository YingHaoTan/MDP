package mdp.controllers.fp.mdp;

import mdp.models.Direction;

/**
 * WaypointState is a State class representing a state that incldues a waypointvisited variable
 * formulated for the evaluation by markov decision process
 * 
 * @author Ying Hao
 */
public class WaypointState extends State {
	private boolean waypointvisited;

	public WaypointState(int x, int y, Direction orientation, boolean waypointvisited) {
		super(x, y, orientation);
		this.waypointvisited = waypointvisited;
	}
	
	/**
	 * Gets if the waypoint have been visited
	 * @return
	 */
	public boolean isWaypointVisited() {
		return this.waypointvisited;
	}

	@Override
	public int hashCode() {
		return super.hashCode() | (this.waypointvisited? 1: 0) << 24;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && (obj instanceof WaypointState && ((WaypointState) obj).waypointvisited == this.waypointvisited);
	}

}
