package mdp.controllers.fp;

import java.awt.Point;

import mdp.models.Direction;
import mdp.models.MapState;
import mdp.robots.RobotBase;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author JINGYANG
 */
public class FastestPathController extends FastestPathBase{
	
    @Override
    public void move(MapState mstate, RobotBase robot, Point destination){
    	Point rpoint = mstate.getRobotPoint();
        
    	int dx = destination.x - rpoint.x;
    	int dy = destination.y - rpoint.y;
    	
    	if(dx > 0)
    		for(int i = 0; i < dx; i++)
    			robot.move(Direction.RIGHT);
    	else if(dx < 0)
    		for(int i = dx; i < 0; i++)
    			robot.move(Direction.LEFT);
    	
    	if(dy > 0)
    		for(int i = 0; i < dy; i++)
    			robot.move(Direction.UP);
    	else if(dy < 0)
    		for(int i = dy; i < 0; i++)
    			robot.move(Direction.DOWN);
        //should sensorsScan() when moving too
    }
    
}
