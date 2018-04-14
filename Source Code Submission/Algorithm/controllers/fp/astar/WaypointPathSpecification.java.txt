package mdp.controllers.fp.astar;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import mdp.models.CellState;
import mdp.models.MapState;
import mdp.models.State;
import mdp.models.WaypointState;
import mdp.robots.RobotBase;

/**
 * WaypointPathSpecification is a BasicPathSpecification taking a waypoint into
 * consideration of forming its path specification
 * 
 * @author Ying Hao
 */
public class WaypointPathSpecification extends BasicPathSpecification {
	
	@Override
	public State calculateInitialState(MapState mstate, RobotBase robot) {
		return new WaypointState(mstate.getRobotPoint(), robot.getCurrentOrientation(), mstate.getWayPoint() == null);
	}

	@Override
	public List<State> getAvailableSuccessorStates(MapState mstate, State state) {
		WaypointState wstate = (WaypointState) state;
		State[] states = super.getAvailableSuccessorStates(mstate, state).toArray(new State[0]);
		
		for(int i = 0; i < states.length; i++) {
			State nstate = states[i];
			
			states[i] = new WaypointState(nstate.getLocation(), 
					nstate.getOrientation(), 
					mstate.getWayPoint() == null || mstate.getRobotCellState(nstate.getLocation()) == CellState.WAYPOINT || wstate.isWaypointVisited());
		}
		
		return Arrays.asList(states);
	}

	@Override
	public boolean isTerminalState(Point destination, State state) {
		return super.isTerminalState(destination, state) && ((WaypointState) state).isWaypointVisited();
	}

	@Override
	public double calculateHeuristicValue(MapState mstate, Point destination, State state) {
		/*WaypointState wstate = (WaypointState) state;
		
		if(mstate.getWayPoint() == null || wstate.isWaypointVisited())
			return super.calculateHeuristicValue(mstate, destination, wstate);
		else {
			Point statepoint = state.getLocation();
			Point waypoint = mstate.convertMapPointToRobotPoints(mstate.getWayPoint()).get(0);
			double wpdist = Math.abs(waypoint.x - statepoint.x) + Math.abs(waypoint.y - statepoint.y);
			double goaldist = Math.abs(destination.x - waypoint.x) + Math.abs(destination.y - waypoint.y);
			
			return wpdist + goaldist;
		}*/
                return 0;
	}
	
}
