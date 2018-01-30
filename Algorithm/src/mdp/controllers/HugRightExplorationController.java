/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 *
 * @author JINGYANG
 */
public class HugRightExplorationController extends ExplorationBase implements RobotActionListener{

    RobotAction[] actionPriority = {RobotAction.TURN_RIGHT, RobotAction.FORWARD, RobotAction.TURN_LEFT};
    
    
    @Override
    public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
        super.explore(mapdim, robot, rcoordinate, ecoordinate);
        robot.addRobotActionListener(this);
        
        sensorsScan();
        
        
        
    
    }
    
    // Can be optimized
    /**
     * Checks if you can move in that direction given the current cell state
     *
     * @param direction
     * @return
     */
    private boolean canMove(Direction direction) {

        // Checks cell state;
        CellState state = CellState.NORMAL;
        List<Point> points = getMapState().convertRobotPointToMapPoints(nextLocation(direction));

        for (Point p : points) {
            CellState pstate = getCellState(p);

            if (pstate == null || pstate == CellState.OBSTACLE) {// || pstate == CellState.UNEXPLORED){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the new location in robot coordinates if you move 1 step in the
     * specified direction, does not actually move
     *
     * @params Return new point
     */
    private Point nextLocation(Direction direction) {
        Point newPoint = new Point(getMapState().getRobotPoint().x, (getMapState().getRobotPoint().y));
        switch (direction) {
            case UP:
                newPoint.y += 1;
                break;
            case DOWN:
                newPoint.y -= 1;
                break;
            case LEFT:
                newPoint.x -= 1;
                break;
            case RIGHT:
                newPoint.x += 1;
                break;
        }
        return newPoint;
    }
    
    
    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Scan area using sensors and updates cell states
     */
    private void sensorsScan() {
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
                            this.setCellState(new Point(sCoordinate.x, sCoordinate.y + range), CellState.NORMAL, null);
                        }
                        break;
                    case DOWN:
                        for (int range = 1; range <= maxRange; range++) {
                            this.setCellState(new Point(sCoordinate.x, sCoordinate.y - range), CellState.NORMAL, null);
                        }
                        break;
                    case LEFT:
                        for (int range = 1; range <= maxRange; range++) {

                            this.setCellState(new Point(sCoordinate.x - range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                    case RIGHT:
                        for (int range = 1; range <= maxRange; range++) {

                            this.setCellState(new Point(sCoordinate.x + range, sCoordinate.y), CellState.NORMAL, null);
                        }
                        break;
                }
            }
        }

    }
    
}
