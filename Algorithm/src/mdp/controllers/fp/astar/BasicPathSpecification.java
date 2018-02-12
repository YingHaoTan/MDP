package mdp.controllers.fp.astar;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.State;
import mdp.robots.RobotBase;

/**
 * BasicPathSpecification is an implementation of PathSpecification
 * to provide basic information for a path
 * 
 * @author Ying Hao
 */
public class BasicPathSpecification implements PathSpecification {

	@Override
	public State calculateInitialState(MapState mstate, RobotBase robot) {
		return new State(mstate.getRobotPoint(), robot.getCurrentOrientation());
	}

	@Override
	public List<State> getAvailableSuccessorStates(MapState mstate, State state) {
		List<State> states = new ArrayList<State>();
		
		Point location = state.getLocation();
		Direction[] directions = Direction.values();
		
		for(Direction direction: directions) {
			Point neighbor = direction.convert(location);
			CellState cellstate = mstate.getRobotCellState(neighbor);
			
			if(cellstate != null && cellstate != CellState.OBSTACLE && cellstate != CellState.UNEXPLORED)
				states.add(new State(neighbor, direction));
		}
		
		return states;
	}

	@Override
	public boolean isTerminalState(Point destination, State state) {
		return state.getLocation().equals(destination);
	}

	@Override
	public double calculatePathCost(MapState mstate, State cstate, State nstate) {
		if(nstate.getOrientation() == cstate.getOrientation())
			return 1.0;
		else
			return 3.0;
	}

	@Override
	public double calculateHeuristicValue(MapState mstate, Point destination, State state) {
		return Math.abs(destination.x - state.getLocation().x) + Math.abs(destination.y - state.getLocation().y);
	}

}
