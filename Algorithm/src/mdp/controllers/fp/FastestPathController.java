package mdp.controllers.fp;

import java.awt.Point;
import mdp.models.Direction;

import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.robots.RobotActionListener;
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
public class FastestPathController extends FastestPathBase implements RobotActionListener{

    @Override
    public void move(MapState mstate, RobotBase robot, Point destination) {
        // Cannot just for loop move, need a call back
        Point rpoint = mstate.getRobotPoint();

        int dx = destination.x - rpoint.x;
        int dy = destination.y - rpoint.y;

        if (dx > 0) {
            //for (int i = 0; i < dx; i++) {
                robot.move(Direction.RIGHT);
                return;
            //}
        } else if (dx < 0) {
            //for (int i = dx; i < 0; i++) {
                robot.move(Direction.LEFT);
                return;
            //}
        }

        if (dy > 0) {
            //for (int i = 0; i < dy; i++) {
                robot.move(Direction.UP);
                return;
            //}
        } else if (dy < 0) {
            //for (int i = dy; i < 0; i++) {
                robot.move(Direction.DOWN);
                return;
            //}
        }

        notifyMovementComplete();
    }

    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
        
        
        // need to update location of robot on mapstate too
        // need to do a sensor scan here
        
        // since this is so similar to the ExplorationController's onRobotActionCompleted, should FastestPathBase and ExplorationBase have the same parent class?
        
    }

}
