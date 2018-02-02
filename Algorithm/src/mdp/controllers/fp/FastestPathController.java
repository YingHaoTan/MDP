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
			nextd = dpoint.x - rpoint.x > 0? Direction.RIGHT: Direction.LEFT;
		}
		else if(rpoint.y != dpoint.y) {
			nextd = dpoint.y - rpoint.x > 0? Direction.UP: Direction.DOWN;
		}
		else {
			nextd = null;
		}
		
		return nextd;
	}

}
