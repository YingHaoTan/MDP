package mdp.v2.models;

import java.awt.Dimension;

/**
 * Map is a BaseModel that stores the state of every map cell
 * 
 * @author Ying Hao
 * @since 21 Feb 2018
 * @version 2.0
 */
public class Map extends BaseModel {
	public final static String STATE_PROPERTY = "state";
	
	private int width;
	private int height;
	private State[][] states;
	
	/**
	 * Creates an instance of MapModel with all of its cells initialized to {@link State#NORMAL}
	 * @param width
	 * @param height
	 */
	public Map(int width, int height) {
		this(width, height, State.NORMAL);
	}
	
	/**
	 * Creates an instance of MapModel
	 * @param width
	 * @param height
	 */
	public Map(int width, int height, State state) {
		this.registerProperty(STATE_PROPERTY);
		
		this.width = width;
		this.height = height;
		this.states = new State[width][height];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				states[x][y] = state;
	}
	
	/**
	 * Gets the size of the map model
	 * @return
	 */
	public Dimension getSize() {
		return new Dimension(width, height);
	}
	
	/**
	 * Sets the state at the specified coordinate
	 * @param x
	 * @param y
	 * @param state
	 */
	public void setState(int x, int y, State state) {
		states[x][y] = state;
		notifyPropertyChanged(STATE_PROPERTY);
	}
	
	/**
	 * Gets the state at the specified coordinate
	 * @param x
	 * @param y
	 * @return
	 */
	public State getState(int x, int y) {
		return states[x][y];
	}

}
