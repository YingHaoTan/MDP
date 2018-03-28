package mdp.models;

import java.awt.Point;

/**
 * State is a model class representing a state for a path information
 * 
 * @author Ying Hao
 */
public class State {
	private Point location;
	private Direction orientation;
	
	public State(Point location, Direction orientation) {
		this.location = new Point(location.x, location.y);
		this.orientation = orientation;
	}
	
	/**
	 * Gets the location
	 * @return
	 */
	public Point getLocation() {
		return location;
	}
	
	/**
	 * Gets the orientation
	 * @return
	 */
	public Direction getOrientation() {
		return orientation;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		
		hash |= this.location.x;
		hash |= this.location.y << 8;
		hash |= this.orientation.ordinal() << 16;
		
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof State) && obj.hashCode() == this.hashCode();
	}

}
