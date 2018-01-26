package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;

import mdp.models.Direction;
import mdp.robots.RobotBase;

/**
 * ExplorationController is a ExplorationBase implementation that performs the actual
 * exploration by using a robot and understanding its environment states
 * 
 * @author Ying Hao
 */
public class ExplorationController extends ExplorationBase {

	@Override
	public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
		super.explore(mapdim, robot, rcoordinate, ecoordinate);
		
		System.out.println("Code goes here");
        System.out.println(robot.getCurrentOrientation());
        robot.move(Direction.RIGHT);
        System.out.println(robot.getCurrentOrientation());
        //Map<SensorConfiguration, Integer> readings = robot.getSensorReading();
	}
}
