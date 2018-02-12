package mdp.models;

import java.awt.Point;

/**
 * Direction contains the enumeration of all possible mutually exclusive directions
 * 
 * @author Ying Hao
 */
public enum Direction {
	UP, DOWN, LEFT, RIGHT;
	
	/**
	 * Converts the source point into another point instance which represents the transformation
	 * after performing the a movement in the current direction
	 * @param source
	 * @param action
	 * @return
	 */
	public Point convert(Point source) {
		switch(this) {
		case UP:
			return new Point(source.x, source.y + 1);
		case DOWN:
			return new Point(source.x, source.y - 1);
		case LEFT:
			return new Point(source.x - 1, source.y);
		case RIGHT:
			return new Point(source.x + 1, source.y);
		}
		
		return null;
	}
}
