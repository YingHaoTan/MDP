package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import java.util.Map;
import mdp.models.CellState;

import mdp.models.Direction;
import mdp.models.ExplorationNode;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 * ExplorationController is a ExplorationBase implementation that performs the
 * actual exploration by using a robot and understanding its environment states
 *
 * @author Ying Hao
 */
public class ExplorationController extends ExplorationBase implements RobotActionListener {

    private enum ExplorationState {
        EXPLORING, RETURNING, BACKTRACKING
    };

    ExplorationNode[][] explorationNodes;
    int dist = 0;
    int R = 1000;
    Point lastVisited;
    ExplorationState currentState = ExplorationState.EXPLORING;

    Direction[] directionalPriority = {Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN};

    @Override
    public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
        super.explore(mapdim, robot, rcoordinate, ecoordinate);

        robot.addRobotActionListener(this);
        MapState internalMapState = getMapState();

        // build graph
        explorationNodes = new ExplorationNode[internalMapState.getRobotSystemDimension().width][internalMapState.getRobotSystemDimension().height];
        for (int y = 0; y < internalMapState.getRobotSystemDimension().height; y++) {
            for (int x = 0; x < internalMapState.getRobotSystemDimension().width; x++) {
                explorationNodes[x][y] = new ExplorationNode();
                if (x - 1 > 0) {
                    explorationNodes[x][y].setLeft(new Point(x - 1, y));
                }
                if (x + 1 < internalMapState.getRobotSystemDimension().width) {
                    explorationNodes[x][y].setRight(new Point(x + 1, y));
                }
                if (y - 1 > 0) {
                    explorationNodes[x][y].setDown(new Point(x, y - 1));
                }
                if (y + 1 < internalMapState.getRobotSystemDimension().height) {
                    explorationNodes[x][y].setUp(new Point(x, y + 1));
                }
            }
        }

        // initial movement
        sensorsScan();
        for (Direction d : directionalPriority) {
            if (canMove(d)) {
                explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].traversed();
                robot.move(d);
                break;
            } else {
                explorationNodes[internalMapState.getRobotPoint().x][internalMapState.getRobotPoint().y].setNeighbour(d, null);
            }
        }

    }

    private double angleBetween(Point v1, Point v2) {
        double cosAngle = dotProduct(v1, v2) / (Math.sqrt(v1.x * v1.x + v1.y * v1.y) * Math.sqrt(v2.x * v2.x + v2.y * v2.y));
        return Math.acos(cosAngle);

    }

    private double dotProduct(Point v1, Point v2) {
        return (v1.getX() * v2.getX()) + (v1.getY() * v2.getY());
    }

    /**
     * Returns best naive direction to take to the goal followed by the second
     * best direction to take, so on and so forth. Best direction is in [0]
     *
     * @param rcoordinate
     * @param ecoordinate
     * @return Array of directions
     */
    private Direction[] directionToGoal(Point rcoordinate, Point ecoordinate) {

        Direction[] results = new Direction[3];

        Point goalVector = new Point(ecoordinate.x - rcoordinate.x, ecoordinate.y - rcoordinate.y);
        Point leftVector = new Point(-1, 0);
        Point rightVector = new Point(1, 0);
        Point upVector = new Point(0, 1);

        double leftAngle = angleBetween(goalVector, leftVector);
        double rightAngle = angleBetween(goalVector, rightVector);
        double upAngle = angleBetween(goalVector, upVector);

        //System.out.println("Left: " + leftAngle);
        //System.out.println("Right: " + rightAngle);
        //System.out.println("Up: " + upAngle);
        if (leftAngle < rightAngle && leftAngle < upAngle) {
            results[0] = Direction.LEFT;
            if (rightAngle < upAngle) {
                results[1] = Direction.RIGHT;
                results[2] = Direction.UP;
            } else {
                results[1] = Direction.UP;
                results[2] = Direction.RIGHT;
            }
        } else if (rightAngle < leftAngle && rightAngle < upAngle) {
            results[0] = Direction.RIGHT;
            if (leftAngle < upAngle) {
                results[1] = Direction.LEFT;
                results[2] = Direction.UP;
            } else {
                results[1] = Direction.UP;
                results[2] = Direction.LEFT;
            }
        } else {
            results[0] = Direction.UP;
            if (rightAngle < leftAngle) {
                results[1] = Direction.RIGHT;
                results[2] = Direction.LEFT;
            } else {
                results[1] = Direction.LEFT;
                results[2] = Direction.RIGHT;
            }
        }
        return results;
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

    private boolean hasVisited(Direction direction) {
        return explorationNodes[nextLocation(direction).x][nextLocation(direction).y].isTraversed();
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

        // update robot position
        Point robotPoint = getMapState().getRobotPoint();
        switch (mapdirection) {
            case UP:
                getMapState().setRobotPoint(new Point(robotPoint.x, robotPoint.y + 1));
                if (currentState == ExplorationState.EXPLORING) {
                    explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setParentDirection(Direction.DOWN);
                }
                break;
            case DOWN:
                getMapState().setRobotPoint(new Point(robotPoint.x, robotPoint.y - 1));
                if (currentState == ExplorationState.EXPLORING) {
                    explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setParentDirection(Direction.UP);
                }
                break;
            case LEFT:
                getMapState().setRobotPoint(new Point(robotPoint.x - 1, robotPoint.y));
                if (currentState == ExplorationState.EXPLORING) {
                    explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setParentDirection(Direction.RIGHT);
                }

                break;
            case RIGHT:
                getMapState().setRobotPoint(new Point(robotPoint.x + 1, robotPoint.y));
                if (currentState == ExplorationState.EXPLORING) {
                    explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setParentDirection(Direction.LEFT);
                }
                break;
        }

        if (currentState == ExplorationState.EXPLORING) {
            dist++;
        }
        if (dist == R) {
            lastVisited = getMapState().getRobotPoint();
            System.out.println("Returning");
            currentState = ExplorationState.RETURNING;
        }

        if (getMapState().getRobotPoint().equals(getMapState().getStartPoint())) {
            System.out.println("Returned");
            dist = 0;
            currentState = ExplorationState.EXPLORING;

            // Checked if all explored
            // else find fastest path to last visited node
        }

        explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].traversed();
        /*for (Point p : getMapState().convertRobotPointToMapPoints(getMapState().getRobotPoint())) {
            this.setCellState(p, getMapState().getMapCellState(p), "traversed");
        }*/

        sensorsScan();

        if (currentState == ExplorationState.RETURNING) {

            // If there exists a path back to starting point (CellState.NORMAL) 
            // fastest path back
            // Else retrace
            getRobot().move(explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].getParentDirection());
            explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setParentDirection(null);
        } else {
            for (Direction d : directionalPriority) {
                if (canMove(d)) {
                    for (Point p : getMapState().convertRobotPointToMapPoints(nextLocation(d))) {
                        this.setCellState(p, getMapState().getMapCellState(p), "can move");
                    }
                    if (!hasVisited(d)) {
                        System.out.println(d);
                        currentState = ExplorationState.EXPLORING;
                        getRobot().move(d);
                        return;
                    }
                } else {
                    
                    explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].setNeighbour(d, null);
                }
            }
            currentState = ExplorationState.BACKTRACKING;
            getRobot().move(explorationNodes[getMapState().getRobotPoint().x][getMapState().getRobotPoint().y].getParentDirection());
        }

    }

}
