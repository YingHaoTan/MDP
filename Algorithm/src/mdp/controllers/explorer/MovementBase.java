
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers.explorer;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

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
    private List<Runnable> scanlisteners;
    
    
    // need to worry about reset too
    private double[][] obstaclesCounter;
    private double[][] noObstaclesCounter;
    
    // this is in robot points
    private boolean[][] travelled; 
    private boolean obstacleChangedFlag = false;
    
    
    
    public MovementBase(){
        this.cslisteners = new ArrayList<>();
        this.scanlisteners = new ArrayList<>();
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
     * Adds scan completed listener
     */
    public void addScanCompletedListener(Runnable listener) {
    	this.scanlisteners.add(listener);
    }
    
    /**
     * Removes scan completed listener
     */
    public void removeScanCompletedListener(Runnable listener) {
    	this.scanlisteners.remove(listener);
    }
    
    
    protected boolean canStream(Point robotPoint, Direction orientation){
        if(robotPoint.x >= 0 && robotPoint.x <= mstate.getRobotSystemDimension().width && robotPoint.y >= 0 && robotPoint.y <= mstate.getRobotSystemDimension().height){
            
            List<Point> points = getMapState().convertRobotPointToMapPoints(robotPoint);
            boolean safe = true;
            
            // are you really that confident that the area has no obstacles?
            for(Point point: points){
                // +2 cause front sensors range is two
                if(noObstaclesCounter[point.x][point.y] <= obstaclesCounter[point.x][point.y] + 1.5){
                    safe = false;
                }
            }
            
            // are the surrounding areas explored?
            List<SensorConfiguration> sensors = robot.getSensors();
            for(SensorConfiguration sensor : sensors){
                Direction sDirection = getSimulatedSensorDirection(sensor, orientation);
                Point sCoordinate = getSimulatedSensorCoordinate(sensor, robotPoint, orientation);
                
                int maxRange = sensor.getMaxDistance();
                
                switch (sDirection) {
                    case UP:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + range)) == CellState.OBSTACLE){
                                break;
                            }
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + range)) == CellState.UNEXPLORED){
                                return false;
                            }
                        }
                        break;
                    case DOWN:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - range)) == CellState.OBSTACLE){
                                break;
                            }    
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - range)) == CellState.UNEXPLORED){
                                return false;
                            }    
                        }
                        
                        
                        break;
                    case LEFT:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x - range, sCoordinate.y)) == CellState.OBSTACLE){
                                break;
                            }    
                            if(mstate.getMapCellState(new Point(sCoordinate.x - range, sCoordinate.y)) == CellState.UNEXPLORED){
                                return false;
                            }    
                        }
                        break;
                    case RIGHT:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x + range, sCoordinate.y)) == CellState.OBSTACLE){
                                break;
                            }   
                            if(mstate.getMapCellState(new Point(sCoordinate.x + range, sCoordinate.y)) == CellState.UNEXPLORED){
                                return false;
                            }    
                        }
                        break;
                }
                
            }
            
            return travelled[robotPoint.x][robotPoint.y] || safe;
            //return canStream;
        }
        return false;
    }
    
    /**
     * Scan area using sensors and updates cell states
     */
    protected void sensorsScan(RobotBase robot) {
        Map<SensorConfiguration, Integer> readings = getRobot().getSensorReading();
        List<SensorConfiguration> sensors = robot.getSensors(); //getRobot().getSensors();

        for (SensorConfiguration sensor : sensors) {

            //System.out.println("Direction: " + getRobot().getSensorDirection(sensor) + ", Coordinate:" + getRobot().getSensorCoordinate(sensor));
            int reading = readings.get(sensor);
            // Limits sensor range
            if (reading > sensor.getMaxDistance()) {
                reading = 0;
            }

            double increment = sensor.getWeightedReliability();
            
            // If detects an obstacle
            if (reading > 0) {
                Direction sDirection = robot.getSensorDirection(sensor);
                Point sCoordinate = robot.getSensorCoordinate(sensor);
                

                // Should also check for out-of-bounds (more applicable in physical robot)
                switch (sDirection) {
                    case UP:
                        if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + reading)) != CellState.WAYPOINT){
                            incrementObstacleCounter(new Point(sCoordinate.x, sCoordinate.y + reading), increment);
                            obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y + reading)) ? true : obstacleChangedFlag;
                        }
                        for (int range = sensor.getMinDistance() + 1; range < reading; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + range)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x, sCoordinate.y + range), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y + range)) ? true : obstacleChangedFlag;
                            }
                        }
                        break;
                    case DOWN:
                        if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - reading)) != CellState.WAYPOINT){
                            incrementObstacleCounter(new Point(sCoordinate.x, sCoordinate.y - reading), increment);
                            obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y - reading)) ? true : obstacleChangedFlag;
                        }
                        for (int range = sensor.getMinDistance() + 1; range < reading; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - range)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x, sCoordinate.y - range), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y - range)) ? true : obstacleChangedFlag;
                            }
                        }
                        break;
                    case LEFT:
                        if(mstate.getMapCellState(new Point(sCoordinate.x - reading, sCoordinate.y)) != CellState.WAYPOINT){
                            incrementObstacleCounter(new Point(sCoordinate.x - reading, sCoordinate.y), increment);
                            obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x - reading, sCoordinate.y)) ? true : obstacleChangedFlag;
                        }
                        for (int range = sensor.getMinDistance() + 1; range < reading; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x - range, sCoordinate.y)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x - range, sCoordinate.y), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x - range, sCoordinate.y)) ? true : obstacleChangedFlag;
                            }
                        }
                        
                        break;
                    case RIGHT:
                        if(mstate.getMapCellState(new Point(sCoordinate.x + reading, sCoordinate.y)) != CellState.WAYPOINT){
                            incrementObstacleCounter(new Point(sCoordinate.x + reading, sCoordinate.y), increment);
                            obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x + reading, sCoordinate.y)) ? true : obstacleChangedFlag;
                        }
                        for (int range = sensor.getMinDistance() + 1; range < reading; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x + range, sCoordinate.y)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x + range, sCoordinate.y), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x + range, sCoordinate.y)) ? true : obstacleChangedFlag;
                            }
                        }
                        break;
                }
            } else {
                int maxRange = sensor.getMaxDistance();
                Direction sDirection = robot.getSensorDirection(sensor);
                Point sCoordinate = robot.getSensorCoordinate(sensor);

                switch (sDirection) {
                    case UP:
                        
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y + range)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x, sCoordinate.y + range), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y + range)) ? true : obstacleChangedFlag;
                            }
                            
                            
                        }
                        
                        
                        break;
                    case DOWN:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x, sCoordinate.y - range)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x, sCoordinate.y - range), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x, sCoordinate.y - range)) ? true : obstacleChangedFlag;
                            }    
                        }
                        
                        
                        break;
                    case LEFT:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x - range, sCoordinate.y)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x - range, sCoordinate.y), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x - range, sCoordinate.y)) ? true : obstacleChangedFlag;
                            }    
                        }
                        
                        
                        break;
                    case RIGHT:
                        for (int range = sensor.getMinDistance() + 1; range <= maxRange; range++) {
                            if(mstate.getMapCellState(new Point(sCoordinate.x + range, sCoordinate.y)) != CellState.WAYPOINT){
                                incrementNoObstacleCounter(new Point(sCoordinate.x + range, sCoordinate.y), increment);
                                obstacleChangedFlag = changeAndCheckCellState(new Point(sCoordinate.x + range, sCoordinate.y)) ? true : obstacleChangedFlag;
                            }    
                        }

                        break;
                }
            }
        }
        
        /*
        System.out.println("========== Obstacles Counter =============");
        printGrid(obstaclesCounter);
        System.out.println("========== No Obstacles Counter =============");
        printGrid(noObstaclesCounter);
        */

        for(Runnable listener: this.scanlisteners)
        	listener.run();
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
        this.noObstaclesCounter = new double[mstate.getMapSystemDimension().width][mstate.getMapSystemDimension().height];
        this.obstaclesCounter = new double[mstate.getMapSystemDimension().width][mstate.getMapSystemDimension().height]; 
        this.travelled = new boolean[mstate.getRobotSystemDimension().width][mstate.getRobotSystemDimension().height];
        setNoObstacleUpperLimit(mstate.convertRobotPointToMapPoints(mstate.getStartPoint()));
        setNoObstacleUpperLimit(mstate.convertRobotPointToMapPoints(mstate.getEndPoint()));
        travelled(mstate.getStartPoint());
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
    
    protected boolean obstaclesChanged(){
        return this.obstacleChangedFlag;
    }
    
    protected void resetObstaclesChanged(){
        this.obstacleChangedFlag = false;
    }
    
    protected void setNoObstacleUpperLimit(List<Point> points){
        for(Point point : points){
            noObstaclesCounter[point.x][point.y] = 99;
        }
    
    }
    
    // Checks obstaclesCounter and noObstaclesCounter, to determine it's an obstacle or not
    private boolean isThereAnObstacle(Point point) {
        if(point.x >= 0 && point.x < obstaclesCounter.length && point.y >= 0 && point.y < obstaclesCounter[0].length){
            if(obstaclesCounter[point.x][point.y] >= noObstaclesCounter[point.x][point.y])
                return true;
            else{
                return false;
            }
        }
        // If out of bounds, return true
        return true;
    }
    
    
    private void incrementObstacleCounter(Point point, double increment){
        if(point.x >= 0 && point.x < obstaclesCounter.length && point.y >= 0 && point.y < obstaclesCounter[0].length)
            this.obstaclesCounter[point.x][point.y]+=increment;
    }
    
    private void incrementNoObstacleCounter(Point point, double increment){
        if(point.x >= 0 && point.x < obstaclesCounter.length && point.y >= 0 && point.y < obstaclesCounter[0].length)
            this.noObstaclesCounter[point.x][point.y]+=increment;
    }
    
    
    // sets cell state based on the counters and 
    // returns true if there's a change from OBSTACLE to EXPLORED or OBSTACLE to EXPLORED, false if otherwise
    private boolean changeAndCheckCellState(Point point){
        
        CellState currentState = this.getCellState(point);
        if(currentState!=null){
            boolean obstacle = isThereAnObstacle(point);
            CellState newState = obstacle ? CellState.OBSTACLE : CellState.NORMAL;
            this.setCellState(point, newState, null);
            if(currentState == CellState.OBSTACLE && newState == CellState.NORMAL){
                return true;
            }
            if(currentState == CellState.NORMAL && newState == CellState.OBSTACLE){
                return true;
            }
        }
        return false;
        
    }
    
    protected void clearNLowestConfidentBlocks(int n){
    	int width = mstate.getMapSystemDimension().width;
    	Double[] differences = new Double[mstate.getMapSystemDimension().width * mstate.getMapSystemDimension().height];  
    	
    	for(int x = 0; x < mstate.getMapSystemDimension().width; x++) {
    		for(int y = 0; y < mstate.getMapSystemDimension().height; y++) {
    			differences[y *  mstate.getMapSystemDimension().width + x] = obstaclesCounter[x][y] - noObstaclesCounter[x][y];
    			if(differences[y *  mstate.getMapSystemDimension().width + x]<0) {
    				differences[y *  mstate.getMapSystemDimension().width + x] = Double.MAX_VALUE;
    			}
    		}
    	}
    	
    	for(int i = 0; i < n; i++) {
    		OptionalDouble minValue = Arrays.stream(differences).mapToDouble((val) -> (double) val).min();
    		if(minValue.isPresent()) {
    			int index = Arrays.asList(differences).indexOf(minValue.getAsDouble());
    			differences[index] = Double.MAX_VALUE;
    			setCellState(new Point(index % width, index / width), CellState.NORMAL, null);
    		}
    	}
    }
    
    public void setUnexploredAsExplored(){
        for(int x = 0; x < mstate.getMapSystemDimension().width; x++){
            for(int y = 0; y < mstate.getMapSystemDimension().height; y++){
                if(mstate.getMapCellState(new Point(x,y)) == CellState.UNEXPLORED){
                	setCellState(new Point(x,y), CellState.NORMAL, null);
                }
            }
        }
    }
    
    
    // set robot point as travelled
    protected void travelled(Point robotPoint){
        this.travelled[robotPoint.x][robotPoint.y] = true;
    }
    
    /**
     * Gets simulated sensor direction in terms of map direction
     *
     * @param sensor
     * @return
     */
    private Direction getSimulatedSensorDirection(SensorConfiguration sensor, Direction simulatedOrientation) {
        Direction orientation = simulatedOrientation;
        Direction sdirection = sensor.getDirection();

        if (orientation == Direction.DOWN) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.DOWN;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.UP;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.RIGHT;
            } else {
                sdirection = Direction.LEFT;
            }
        } else if (orientation == Direction.LEFT) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.LEFT;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.RIGHT;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.DOWN;
            } else {
                sdirection = Direction.UP;
            }
        } else if (orientation == Direction.RIGHT) {
            if (sdirection == Direction.UP) {
                sdirection = Direction.RIGHT;
            } else if (sdirection == Direction.DOWN) {
                sdirection = Direction.LEFT;
            } else if (sdirection == Direction.LEFT) {
                sdirection = Direction.UP;
            } else {
                sdirection = Direction.DOWN;
            }
        }

        return sdirection;
    }

    /**
     * Gets the simulated sensor coordinates
     *
     * @param sensor
     * @return
     */
    private Point getSimulatedSensorCoordinate(SensorConfiguration sensor, Point simulatedRobotPoint, Direction simulatedOrientation) {
        List<Point> points = mstate.convertRobotPointToMapPoints(simulatedRobotPoint);
        Point location = points.get(points.size() / 2);

        Dimension rdim = mstate.getRobotDimension();
        Direction sdirection = this.getSimulatedSensorDirection(sensor, simulatedOrientation);
        Point scoordinate;

        if (sdirection == Direction.UP) {
            scoordinate = new Point(location.x + sensor.getCoordinate(), location.y + rdim.height / 2);
        } else if (sdirection == Direction.DOWN) {
            scoordinate = new Point(location.x - sensor.getCoordinate(), location.y - rdim.height / 2);
        } else if (sdirection == Direction.LEFT) {
            scoordinate = new Point(location.x - rdim.width / 2, location.y + sensor.getCoordinate());
        } else {
            scoordinate = new Point(location.x + rdim.width / 2, location.y - sensor.getCoordinate());
        }

        return scoordinate;
    }
    
    private void printGrid(double[][] grid){
        for(int y = grid[0].length - 1; y >= 0; y--){
            for(int x = 0; x< grid.length; x++){
                System.out.print(grid[x][y] +" ");
            }
            System.out.println();
        }
    
    }
}
