/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Map;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 *
 * @author JINGYANG
 */
public class PhysicalRobot extends RobotBase {

    public PhysicalRobot(Dimension dimension, Direction orientation) {
        super(dimension, orientation);
    }

    @Override
    public Map<SensorConfiguration, Integer> getSensorReading() {
        // If readings are null, tells TCP to send SCAN command
        // Waits for it then return
        // Else
        // Returns most recently updated readings, MUST ONLY CALL THIS AFTER SENSOR READING IS UPDATED WHICH IS AFTER AFTER MOVING
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void init() {
        // Tells TCP to send START command
        
        
        // a = SynchronousQueue.take()
        // Waits for TCP's reply^
    }

    @Override
    public Direction getSensorDirection(SensorConfiguration sensor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Point getSensorCoordinate(SensorConfiguration sensor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void move(Direction mapdirection, RobotAction... actions) {
        // for actions
            // Tells TCP Connector to send move instruction
            // outgoingArduinoQueue.add(instruction);
            // a = SychronousQueue.take()
            // update sensor readings 
           
        // Waits for robot to reach, then notify
        // this.notify(mapdirection, actions);

    }

}
