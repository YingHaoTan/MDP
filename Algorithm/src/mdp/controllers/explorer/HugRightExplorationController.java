/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers.explorer;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.FastestPathCompletedListener;
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
public class HugRightExplorationController extends ExplorationBase implements RobotActionListener, FastestPathCompletedListener {

    enum States {
        BOUNDARY, ABOUT_TURN, EXPLORATION, EXPLORING, COMPLETED
    };

    FastestPathBase fastestPath;

    RobotAction[] actionPriority = {RobotAction.TURN_RIGHT, RobotAction.FORWARD, RobotAction.TURN_LEFT};
    List<Point> unexploredPoints;

    int exploringUnexplored;
    int aboutTurn;
    boolean justTurned;
    boolean leftStartPoint;
    States currentState;

    public HugRightExplorationController(FastestPathBase fastestPath) {
        super();
        fastestPath.addFastestPathCompletedListener(this);
        this.fastestPath = fastestPath;
        this.unexploredPoints = new ArrayList();
    }

    @Override
    public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
        super.explore(mapdim, robot, rcoordinate, ecoordinate);
        robot.addRobotActionListener(this);
        sensorsScan();

        currentState = States.BOUNDARY;
        unexploredPoints = new ArrayList<Point>();
        exploringUnexplored = 0;
        aboutTurn = 0;
        justTurned = false;
        leftStartPoint = false;
                
        for (RobotAction action : actionPriority) {
            if (canMove(actionToMapDirection(action))) {
                if (action == RobotAction.TURN_RIGHT || action == RobotAction.TURN_LEFT) {
                    justTurned = true;
                } else {
                    justTurned = false;
                }
                getRobot().move(action);
                break;
            }
        }

    }

    /**
     * For TURN_RIGHT and TURN_LEFT, will return the map direction the robot is
     * facing after turning For FORWARD and REVERSE, returns the map direction
     * that the robot will moving 1 step towards
     *
     * @param action
     * @return Direction from the perspective of the map
     */
    private Direction actionToMapDirection(RobotAction action) {
        switch (action) {
            case TURN_RIGHT:
                switch (getRobot().getCurrentOrientation()) {
                    case UP:
                        return Direction.RIGHT;
                    case DOWN:
                        return Direction.LEFT;
                    case LEFT:
                        return Direction.UP;
                    case RIGHT:
                        return Direction.DOWN;

                }
                break;
            case TURN_LEFT:
                switch (getRobot().getCurrentOrientation()) {
                    case UP:
                        return Direction.LEFT;
                    case DOWN:
                        return Direction.RIGHT;
                    case LEFT:
                        return Direction.DOWN;
                    case RIGHT:
                        return Direction.UP;
                }
            case FORWARD:
                return getRobot().getCurrentOrientation();
            case REVERSE:
                switch (getRobot().getCurrentOrientation()) {
                    case UP:
                        return Direction.DOWN;
                    case DOWN:
                        return Direction.UP;
                    case LEFT:
                        return Direction.RIGHT;
                    case RIGHT:
                        return Direction.LEFT;
                }
        }
        return null;
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

    /**
     * Returns true if specified robotPoint does not have any obstacles and have
     * unexplored cells
     *
     * @param robotPoint
     * @return
     */
    private boolean isUnexplored(Point robotPoint) {
        List<Point> points = getMapState().convertRobotPointToMapPoints(robotPoint);
        boolean hasUnexplored = false;
        for (Point point : points) {
            if (getMapState().getMapCellState(point) == CellState.OBSTACLE) {
                return false;
            }
            if (getMapState().getMapCellState(point) == CellState.UNEXPLORED) {
                hasUnexplored = true;
            }
        }
        return hasUnexplored;
    }
    
    private List<Point> nearbyRobotPoints(Point rPoint){
        List<Point> nearbyRobotPoints = new ArrayList<Point>();
        return nearbyRobotPoints;
    }
    
    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
        Point robotPoint = getMapState().getRobotPoint();
        if (mapdirection != null) {
            leftStartPoint = true;
            switch (mapdirection) {
                case UP:
                    getMapState().setRobotPoint(new Point(robotPoint.x, robotPoint.y + 1));
                    break;
                case DOWN:
                    getMapState().setRobotPoint(new Point(robotPoint.x, robotPoint.y - 1));
                    break;
                case LEFT:
                    getMapState().setRobotPoint(new Point(robotPoint.x - 1, robotPoint.y));
                    break;
                case RIGHT:
                    getMapState().setRobotPoint(new Point(robotPoint.x + 1, robotPoint.y));
                    break;
            }
        }

        sensorsScan();

        if (currentState == States.BOUNDARY) {
            if (leftStartPoint && getMapState().getRobotPoint().equals(getMapState().getStartPoint())) {
                currentState = States.EXPLORATION;
            } else {
                for (RobotAction action : actionPriority) {
                    if (canMove(actionToMapDirection(action))) {

                        // Do not turn twice in a row while exploring boundary
                        if (action == RobotAction.TURN_RIGHT || action == RobotAction.TURN_LEFT) {
                            if (justTurned) {
                                continue;
                            }
                        }

                        getRobot().move(action);
                        if (action == RobotAction.TURN_RIGHT || action == RobotAction.TURN_LEFT) {
                            justTurned = true;
                        } else {
                            justTurned = false;
                        }
                        return;
                    }
                }

                currentState = States.ABOUT_TURN;
            }
        }
        if (currentState == States.ABOUT_TURN) {
            getRobot().move(RobotAction.TURN_RIGHT);
            aboutTurn++;
            if (aboutTurn == 2) {
                currentState = States.BOUNDARY;
                aboutTurn = 0;
            }
        }
        if (currentState == States.EXPLORATION) {
            for (int y = 0; y < getMapState().getRobotSystemDimension().height; y++) {
                for (int x = 0; x < getMapState().getRobotSystemDimension().width; x++) {
                    Point tempPoint = new Point(x, y);

                    if (isUnexplored(tempPoint)) {
                        unexploredPoints.add(tempPoint);
                        //fastestPath.move(getMapState(), getRobot(), tempPoint);
                    }
                }
            }

            if (unexploredPoints.size() > 0) {
                currentState = States.EXPLORING;
                
                fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored));
                
            } else {
                this.complete();
            }
        }
    }

    @Override
    public void onFastestPathCompleted() {
        exploringUnexplored++;
        if (exploringUnexplored < unexploredPoints.size()) {
            while(!isUnexplored(unexploredPoints.get(exploringUnexplored))){
                exploringUnexplored++;
                if(exploringUnexplored==unexploredPoints.size()){
                    this.complete();
                    return;
                }
            }
            fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored));
        } else {
            this.complete();
        }
    }

    @Override
    public void complete() {
        currentState = States.COMPLETED;
        fastestPath.move(getMapState(), getRobot(), getMapState().getStartPoint());
        super.complete();
    }

    

}
