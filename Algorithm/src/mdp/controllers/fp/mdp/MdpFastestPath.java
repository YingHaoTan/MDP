package mdp.controllers.fp.mdp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mdp.controllers.fp.FastestPathBase;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;

/**
 * MdpFastestPath is a FastestPathBase that calculates the optimal path using markov decision process
 * 
 * @author Ying Hao
 */
public class MdpFastestPath extends FastestPathBase {
	private static final double DISCOUNT_VALUE = 0.9;
	private static final double EPSILON = 1e-5;
	
	private State currentstate;
	private Map<State, Direction> policy;
	
	/**
	 * Gets the next state given a specified state and action
	 * @param state
	 * @param action
	 * @return
	 */
	protected State getNextState(State state, Direction action) {
		int x = state.getX(), y = state.getY();
		
		switch(action) {
		case UP:
			y = y + 1;
			break;
		case DOWN:
			y = y - 1;
			break;
		case LEFT:
			x = x - 1;
			break;
		case RIGHT:
			x = x + 1;
			break;
		}
		
		return new State(x, y, action);
	}
	
	/**
	 * Gets a flag indicating if the specified state is a terminal state
	 * @return
	 */
	protected boolean isTerminalState(State state) {
		Point dest = this.getDestination();
		
		return state.getX() == dest.x && state.getY() == dest.y;
	}
	
	/**
	 * Gets the immediate reward for the specified state and action
	 * @param state
	 * @param action
	 * @return
	 */
	protected double getImmediateReward(State state, Direction action) {
		double reward;
		State nextstate = getNextState(state, action);
		
		if(isTerminalState(nextstate))
			reward = 1.0;
		else if(state.getOrientation() == action)
			reward = -0.05;
		else
			reward = -0.15;
		
		return reward;
	}
	
	protected State getInitialState() {
		Point rpoint = this.getMapState().getRobotPoint();
		return new State(rpoint.x, rpoint.y, this.getRobot().getCurrentOrientation());
	}
	
	/**
	 * Gets the available actions for the given state
	 * @param state
	 * @return
	 */
	private List<Direction> getAvailableActions(State state) {
		List<Direction> actions = new ArrayList<Direction>();
		MapState mstate = this.getMapState();
		
		int x = state.getX(), y = state.getY();
		if(!isTerminalState(state)) {
			CellState lstate = mstate.getRobotCellState(new Point(x - 1, y));
			CellState rstate = mstate.getRobotCellState(new Point(x + 1, y));
			CellState dstate = mstate.getRobotCellState(new Point(x, y - 1));
			CellState ustate = mstate.getRobotCellState(new Point(x, y + 1));
			
			if(lstate != null && lstate != CellState.OBSTACLE && lstate != CellState.UNEXPLORED)
				actions.add(Direction.LEFT);
			if(rstate != null && rstate != CellState.OBSTACLE && rstate != CellState.UNEXPLORED)
				actions.add(Direction.RIGHT);
			if(dstate != null && dstate != CellState.OBSTACLE && dstate != CellState.UNEXPLORED)
				actions.add(Direction.DOWN);
			if(ustate != null && ustate != CellState.OBSTACLE && ustate != CellState.UNEXPLORED)
				actions.add(Direction.UP);
		}
		
		return actions;
	}
	
	/**
	 * Initialize the policy map with the specified initial state
	 * @param initialstate
	 */
	private void initializePolicyMap(PolicyImprover improver, State initialstate) {
		if(!isTerminalState(initialstate) && !improver.policymap.containsKey(initialstate)) {
			improver.improve(initialstate);
			
			for(Direction action: this.getAvailableActions(initialstate)) {
				State nstate = getNextState(initialstate, action);
				initializePolicyMap(improver, nstate);
			}
		}
	}
	
	/**
	 * Optimizes a policy map to maximize the reward value
	 */
	private Map<State, Direction> optimizePolicyMap(Map<State, Direction> policymap) {
		boolean policychanges = true;
		
		PolicyEvaluator evaluator = new PolicyEvaluator(policymap, new ConcurrentHashMap<State, Double>(policymap.size()), new ConcurrentHashMap<State, Double>(policymap.size()));
		while(policychanges) {
			policychanges = false;
			
			// Calculate utility values based on current policy until it converges
			double delta = 1.0;
			while(delta > EPSILON) { 
				delta = 0.0;
				
				// Create new policy evaluator instance with switched utility maps
				evaluator = new PolicyEvaluator(policymap, evaluator.putility, evaluator.cutility);
				
				delta = policymap.keySet()
						.parallelStream()
						.mapToDouble(evaluator::evaluate)	
						.sum();
			}
			
			// Assign best policy for all states based on evaluated utilities
			PolicyImprover improver = new PolicyImprover(policymap, evaluator.cutility);
			policychanges = policymap.keySet()
					.parallelStream().mapToInt(improver::improve).sum() > 0;
		}
		
		return policymap;
	}

	@Override
	protected void preprocess() {
		State initialstate = getInitialState();
		
		PolicyImprover improver = new PolicyImprover(new ConcurrentHashMap<State, Direction>(), new ConcurrentHashMap<State, Double>());
		this.initializePolicyMap(improver, initialstate);
		this.policy = optimizePolicyMap(improver.policymap);
		this.currentstate = initialstate;
	}

	@Override
	protected Direction next() {
		State cstate = this.currentstate;
		Direction action = this.policy.get(cstate);
		
		if(action != null)
			this.currentstate = this.getNextState(cstate, action);
		
		return action;
	}
	
	/**
	 * PolicyEvaluator class is an auxiliary class that provides functionality to evaluate state utility values
	 * 
	 * @author Ying Hao
	 */
	private class PolicyEvaluator {
		private Map<State, Direction> policymap;
		private Map<State, Double> cutility;
		private Map<State, Double> putility;
		
		private PolicyEvaluator(Map<State, Direction> policymap, Map<State, Double> current, Map<State, Double> previous) {
			this.policymap = policymap;
			this.cutility = current;
			this.putility = previous;
		}
		
		/**
		 * Evaluates the state utility based on its immediate and future rewards
		 * @param state
		 * @return
		 */
		private double evaluate(State state) {
			Direction policyaction = policymap.get(state);
			Double currentutility = putility.get(state);
			Double futurerewards = putility.get(getNextState(state, policyaction));
			
			double immediaterewards = getImmediateReward(state, policyaction);
			double evaluatedrewards = immediaterewards;
			
			if(futurerewards != null)
				evaluatedrewards = evaluatedrewards + DISCOUNT_VALUE * futurerewards;
			
			cutility.put(state, evaluatedrewards);
			
			return Math.abs(currentutility == null? evaluatedrewards: currentutility - evaluatedrewards);
		}
	}
	
	/**
	 * PolicyImprover class is an auxiliary class that provides functionality to improve upon policies
	 * based on the specified utility values and immediate rewards
	 * 
	 * @author Ying Hao
	 */
	private class PolicyImprover {
		private Map<State, Direction> policymap;
		private Map<State, Double> utilitymap;
		
		private PolicyImprover(Map<State, Direction> policymap, Map<State, Double> utilitymap) {
			this.policymap = policymap;
			this.utilitymap = utilitymap;
		}
		
		/**
		 * Improves its internal policy map to perform its best possible action for the given state
		 * @param state
		 * @return
		 */
		private int improve(State state) {
			Direction previous = policymap.get(state);
			Direction evaluated = previous;
			Double maxval = null;
			
			for(Direction action: getAvailableActions(state)) {
				State nstate = getNextState(state, action);
				double utility;
				
				if(!isTerminalState(nstate)) {
					utility = getImmediateReward(state, action) + DISCOUNT_VALUE * utilitymap.getOrDefault(nstate, 0.0);
					if(maxval == null || utility > maxval) {
						evaluated = action;
						maxval = utility;
					}
				}
				else {
					evaluated = action;
					break;
				}
			}
			
			policymap.put(state, evaluated);
			
			return previous == evaluated? 0: 1;
		}
	}

}
