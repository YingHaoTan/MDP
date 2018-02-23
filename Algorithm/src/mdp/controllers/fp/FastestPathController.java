package mdp.controllers.fp;

import java.awt.Point;

import mdp.models.Direction;
import mdp.models.MapState;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JINGYANG
 */
public class FastestPathController extends FastestPathBase {

	@Override
	protected Direction next() {
		Direction nextd;
		
		MapState mstate = getMapState();
		Point rpoint = mstate.getRobotPoint();
		Point dpoint = this.getDestination();
		
		if(rpoint.x != dpoint.x) {
			nextd = dpoint.x - rpoint.x > 0 ? Direction.RIGHT: Direction.LEFT;
			mstate.setRobotPoint(new Point(rpoint.x + (nextd == Direction.RIGHT? 1: -1), rpoint.y));
		}
		else if(rpoint.y != dpoint.y) {
			nextd = dpoint.y - rpoint.y > 0? Direction.UP: Direction.DOWN;
			mstate.setRobotPoint(new Point(rpoint.x, rpoint.y + (nextd == Direction.UP? 1: -1)));
		}
		else {
			nextd = null;
		}
		
		return nextd;
	}

	@Override
	protected boolean preprocess() {
		return true;
	}

}
