package mdp.controllers.fp.mdp;

import java.awt.Point;

import mdp.models.CellState;
import mdp.models.Direction;

/**
 * MdpWaypointFastestPath is a MdpFastestPath that calculates the optimal path using markov decision process
 * with an addition of waypoint that must be passed through
 * 
 * @author Ying Hao
 */
public class MdpWaypointFastestPath extends MdpFastestPath {

	@Override
	protected State getNextState(State state, Direction action) {
		Point wpoint = this.getMapState().getWayPoint();
		WaypointState wstate = (WaypointState) state;
		State nstate = super.getNextState(state, action);
		
		if(wpoint == null) {
			wstate = new WaypointState(nstate.getX(), nstate.getY(), nstate.getOrientation(), false);
		}
		else {
			if(this.getMapState().getRobotCellState(new Point(nstate.getX(), nstate.getY())) == CellState.WAYPOINT)
				wstate = new WaypointState(nstate.getX(), nstate.getY(), nstate.getOrientation(), true);
			else
				wstate = new WaypointState(nstate.getX(), nstate.getY(), nstate.getOrientation(), wstate.isWaypointVisited());
		}
		
		return wstate;
	}

	@Override
	protected boolean isTerminalState(State state) {
		Point wpoint = this.getMapState().getWayPoint();
		
		return super.isTerminalState(state) && (wpoint == null || ((WaypointState) state).isWaypointVisited());
	}

	@Override
	protected double getImmediateReward(State state, Direction action) {
		double reward = super.getImmediateReward(state, action);
		WaypointState wstate = (WaypointState) state;
		WaypointState wnstate = (WaypointState) getNextState(state, action);
		Point wpoint = this.getMapState().getWayPoint();
		
		if(wpoint != null && !wstate.isWaypointVisited() && this.getMapState().getRobotCellState(new Point(wnstate.getX(), wnstate.getY())) == CellState.WAYPOINT)
			reward = 0.75;
		
		return reward;
	}

	@Override
	protected State getInitialState() {
		State state = super.getInitialState();
		
		return new WaypointState(state.getX(), state.getY(), state.getOrientation(), false);
	}

}
