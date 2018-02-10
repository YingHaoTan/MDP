package mdp.controllers.fp.mdp;

import mdp.models.Direction;

/**
 * State is a model class representing a state formulated for the evaluation by markov decision process
 * 
 * @author Ying Hao
 */
public class State {
	private int x;
	private int y;
	private Direction orientation;
	
	public State(int x, int y, Direction orientation) {
		this.x = x;
		this.y = y;
		this.orientation = orientation;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Direction getOrientation() {
		return orientation;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		
		hash |= x;
		hash |= y << 8;
		hash |= this.orientation.ordinal() << 16;
		
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return true;
	}

}
