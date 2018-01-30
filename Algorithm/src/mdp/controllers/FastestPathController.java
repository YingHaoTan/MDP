package mdp.controllers;

import java.awt.Point;
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
        mstate.setRobotPoint(destination);
        //should sensorsScan() when moving too
    }
}
