/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers.explorer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mdp.controllers.explorer.CellStateUpdateListener;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.SensorConfiguration;
import mdp.robots.RobotBase;

/**
 *
 * @author JINGYANG
 */
public abstract class MovementBase {
    private MapState mstate;
    private RobotBase robot;
    private List<CellStateUpdateListener> cslisteners;
    
    
    public MovementBase(){
        this.cslisteners = new ArrayList<>();
    }
    
    /**
     * Adds CellStateUpdateListener
     * @param listener
     */
    public void addCellStateUpdateListener(CellStateUpdateListener listener) {
        this.cslisteners.add(listener);
    }

    /**
     * Removes CellStateUpdateListener
     * @param listener
     */
    public void removeCellStateUpdateListener(CellStateUpdateListener listener) {
        this.cslisteners.remove(listener);
    }
    
    /**
     * Scan area using sensors and updates cell states
     */
    protected void sensorsScan() {
        Map<SensorConfiguration, Integer> readings = getRobot().getSensorReading();
        List<SensorConfiguration> sensors = getRobot().getSensors();

        for (SensorConfiguration sensor : sensors) {

            //System.out.println("Direction: " + getRobot().getSensorDirection(sensor) + ", Coordinate:" + getRobot().getSensorCoordinate(sensor));
            int reading = readings.get(sensor);
            int test = reading;
            // Limits sensor range
            if (reading > sensor.getMaxDistance()) {
                reading = 0;
            }

            // If detects an obstacle
            if (reading > 0) {
                Direction sDirection = getRobot().getSensorDirection(sensor);
                Point sCoordinate = getRobot().getSensorCoordinate(sensor);

                // Should also check for out-of-bounds (more applicable in physical robot)
                switch (sDirection) {
                    case UP:
                        this.setCellState(new Point(sCoordinate.x, sCoordinate.y + reading), CellState.OBSTACLE, null);
                        for (int range = 1; range < reading; range++) {
                            this.setCellState(new Point(sCoordinate.x, sCoordinate.y + range), CellState.NORMAL, null);
                        }
                        break;
                    case DOWN:
                        this.setCellState(new Point(sCoordinate.x, sCoordinate.y - reading), CellState.OBSTACLE, null);
                        for (int range = 1; range < reading; range++) {
                            this.setCellState(new Point(sCoordinate.x, sCoordinate.y - range), CellState.NORMAL, null);
                        }
                        break;
                    case LEFT:
                        this.setCellState(new Point(sCoordinate.x - reading, sCoordinate.y), CellState.OBSTACLE, null);
                        for (int range = 1; range < reading; range++) {
                            this.setCellState(new Point(sCoordinate.x - range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                    case RIGHT:
                        this.setCellState(new Point(sCoordinate.x + reading, sCoordinate.y), CellState.OBSTACLE, null);
                        for (int range = 1; range < reading; range++) {
                            this.setCellState(new Point(sCoordinate.x + range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                }
            } else {
                int maxRange = sensor.getMaxDistance();
                Direction sDirection = getRobot().getSensorDirection(sensor);
                Point sCoordinate = getRobot().getSensorCoordinate(sensor);
                switch (sDirection) {
                    case UP:
                        for (int range = 1; range <= maxRange; range++) {
                        	if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + range)) != CellState.WAYPOINT)
                            	this.setCellState(new Point(sCoordinate.x, sCoordinate.y + range), CellState.NORMAL, null);
                        }
                        break;
                    case DOWN:
                        for (int range = 1; range <= maxRange; range++) {
                        	if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - range)) != CellState.WAYPOINT)
                            	this.setCellState(new Point(sCoordinate.x, sCoordinate.y - range), CellState.NORMAL, null);
                        }
                        break;
                    case LEFT:
                        for (int range = 1; range <= maxRange; range++) {
                        	if(mstate.getMapCellState(new Point(sCoordinate.x - range, sCoordinate.y)) != CellState.WAYPOINT)
                            	this.setCellState(new Point(sCoordinate.x - range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                    case RIGHT:
                        for (int range = 1; range <= maxRange; range++) {
                        	if(mstate.getMapCellState(new Point(sCoordinate.x + range, sCoordinate.y)) != CellState.WAYPOINT)
                            	this.setCellState(new Point(sCoordinate.x + range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                }
            }
        }
    }
    
    
    /**
     * Sets the CellState at a particular coordinate
     * @param coordinate
     * @param state
     * @param label
     */
    protected void setCellState(Point coordinate, CellState state, String label) {
        if (mstate.setMapCellState(coordinate, state)){
            for (CellStateUpdateListener listener : cslisteners)
                listener.onCellStateUpdate(coordinate, state, label);
        }
    }

    /**
     * Gets the CellState at a particular coordinate
     * @param coordinate
     * @return
     */
    protected CellState getCellState(Point coordinate) {

        return mstate.getMapCellState(coordinate);
    }

    protected void setMapState(MapState mstate){
        this.mstate = mstate;
    }
    
    /**
     * Gets the map state
     * @return
     */
    protected MapState getMapState() {
        return mstate;
    }
    
    /**
     * Sets the robot
     */
    protected void setRobot(RobotBase robot){
        this.robot = robot;
    }
    

    /**
     * Gets the robot
     * @return
     */
    protected RobotBase getRobot() {
        return robot;
    }
}
