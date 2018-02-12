package mdp.controllers.fp.astar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import mdp.controllers.fp.FastestPathBase;
import mdp.models.Direction;
import mdp.models.State;

/**
 * AStarFastestPath is a FastestPathBase that calculates the optimal path using a star search
 * 
 * @author Ying Hao
 */
public class AStarFastestPath extends FastestPathBase {
	private State current;
	private PathSpecification specs;
	private List<State> policy;
	
	public AStarFastestPath(PathSpecification specs) {
		this.specs = specs;
	}

	@Override
	protected Direction next() {
		Direction nextaction = null;
		
		if(policy != null && !specs.isTerminalState(getDestination(), current)) {
			int index = 0;
			
			if(!current.equals(specs.calculateInitialState(getMapState(), getRobot())))
				index = policy.indexOf(current) + 1;
			
			current = policy.get(index);
			nextaction = current.getOrientation();
		}
		
		return nextaction;
	}

	@Override
	protected void preprocess() {
		State initialstate = specs.calculateInitialState(getMapState(), getRobot());
		Set<State> closedset = new HashSet<State>();
		Set<State> openedset = new HashSet<State>();
		Map<State, State> pstatemap = new HashMap<State, State>();
		Map<State, Double> ncostmap = new HashMap<State, Double>();
		Map<State, Double> fcostmap = new HashMap<State, Double>();
		PriorityQueue<State> pqueue = new PriorityQueue<State>((s1, s2) -> (int) Math.signum(fcostmap.get(s1) - fcostmap.get(s2)));
	
		pqueue.offer(initialstate);
		ncostmap.put(initialstate, 0.0);
		fcostmap.put(initialstate, specs.calculateHeuristicValue(getMapState(), getDestination(), initialstate));
		
		policy = null;
		State pstate;
		while(policy == null && (pstate = pqueue.poll()) != null) {
			if(specs.isTerminalState(getDestination(), pstate))
				policy = reconstructPath(pstatemap, pstate);
			else {
				closedset.add(pstate);
				openedset.remove(pstate);
				
				for(State successor: specs.getAvailableSuccessorStates(getMapState(), pstate)) {
					if(!closedset.contains(successor)) {
						double ncost = ncostmap.get(pstate) + specs.calculatePathCost(getMapState(), pstate, successor);
						
						if(ncostmap.get(successor) == null || ncostmap.get(successor) > ncost) {
							pstatemap.put(successor, pstate);
							ncostmap.put(successor, ncost);
							fcostmap.put(successor, ncost + specs.calculateHeuristicValue(getMapState(), getDestination(), successor));
						}
						
						if(openedset.add(successor))
							pqueue.add(successor);
					}
				}
			}
		}
		
		current = initialstate;
	}
	
	private List<State> reconstructPath(Map<State, State> pstatemap, State current) {
		List<State> path = new ArrayList<State>();
		
		while(pstatemap.containsKey(current)) {
			path.add(0, current);
			current = pstatemap.get(current);
		}
		
		return path;
	}

}
