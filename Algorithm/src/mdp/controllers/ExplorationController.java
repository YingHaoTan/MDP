package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mdp.models.CellState;

import mdp.models.Direction;
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

    private Point supposedCurrentLocation;

    @Override
    public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
        super.explore(mapdim, robot, rcoordinate, ecoordinate);
        robot.addRobotActionListener(this);

        Direction[] bestDirections = directionToGoal(rcoordinate, ecoordinate);
        this.supposedCurrentLocation = rcoordinate;

        sensorsScan();
        for (Direction d : bestDirections) {
            if (canMove(d)) {
                robot.move(d);
                break;
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

        System.out.println("Left: " + leftAngle);
        System.out.println("Right: " + rightAngle);
        System.out.println("Up: " + upAngle);

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
     * Checks if you can move in that direction given the current cellstate
     *
     * @param direction
     * @return
     */
    private boolean canMove(Direction direction) {

        // Checks cell state;
        CellState state = CellState.NORMAL;
        Set<Point> points = convertRobotPointToMapPoints(nextLocation(direction));
        for (Point p : points) {
            CellState pstate = getCellState(p);

            if (pstate == CellState.UNEXPLORED
                    || (pstate == CellState.OBSTACLE && state != CellState.UNEXPLORED)
                    || (pstate == CellState.WAYPOINT && state != CellState.UNEXPLORED && state != CellState.OBSTACLE)) {
                state = pstate;
            }
        }
        if (state == CellState.OBSTACLE) {
            return false;
        }
        return true;
    }

    /**
     * Scan area using sensors and updates cell states
     */
    private void sensorsScan() {
        Map<SensorConfiguration, Integer> readings = getRobot().getSensorReading();
        List<SensorConfiguration> sensors = getRobot().getSensors();

        for (SensorConfiguration sensor : sensors) {
            int reading = readings.get(sensor);

            System.out.println("Sensor Reading:" + readings.get(sensor));

            // Limits sensor range
            if (reading > sensor.getMaxDistance()) {
                System.out.println(reading);
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

    private Set<Point> convertRobotPointToMapPoints(Point p) {
        Set<Point> points = new HashSet<>();

        for (int x = 0; x < getRobot().getDimension().width; x++) {
            for (int y = 0; y < getRobot().getDimension().height; y++) {
                points.add(new Point(p.x + x, p.y + y));
            }
        }

        return points;
    }

    /**
     * Returns the new location in robot coordinates if you move 1 step in the
     * specified direction, does not actually move
     *
     * @params Return new point
     */
    private Point nextLocation(Direction direction) {
        Point newPoint = new Point(this.supposedCurrentLocation.x, this.supposedCurrentLocation.y);
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

        switch (mapdirection) {
            case UP:
                this.supposedCurrentLocation.y += 1;
                break;
            case DOWN:
                this.supposedCurrentLocation.y -= 1;
                break;
            case LEFT:
                this.supposedCurrentLocation.x -= 1;
                break;
            case RIGHT:
                this.supposedCurrentLocation.x += 1;
                break;
        }

        // Sense end location
    }

}
