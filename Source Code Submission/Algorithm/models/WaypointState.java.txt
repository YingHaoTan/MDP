package mdp.models;

import java.awt.Point;

/**
 * WaypointState is a State class representing a state for a path information with an additional waypoint
 * value
 * 
 * @author Ying Hao
 */
public class WaypointState extends State {
	private boolean waypointvisited;

	public WaypointState(Point location, Direction orientation, boolean waypointvisited) {
		super(location, orientation);
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

}
