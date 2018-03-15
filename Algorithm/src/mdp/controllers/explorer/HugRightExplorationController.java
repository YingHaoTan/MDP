/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers.explorer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.FastestPathCompletedListener;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.robots.CalibrationSpecification;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 *
 * @author JINGYANG
 */
public class HugRightExplorationController extends ExplorationBase implements RobotActionListener, FastestPathCompletedListener {

    

    enum States {
        BOUNDARY, ABOUT_TURN, LOOPING, EXITING_LOOP, EXPLORATION, EXPLORING, COMPLETED
    };

    FastestPathBase fastestPath;

    RobotAction[] actionPriority = {RobotAction.TURN_RIGHT, RobotAction.FORWARD, RobotAction.TURN_LEFT};
    List<Point> unexploredPoints;
    List<List<Point>> neighbourPoints;

    LinkedList<Integer> exploringUnexplored;
    int neighbourCounter;
    int aboutTurn;
    //int justTurnedCounter;
    boolean justTurned;
    States currentState;

    LinkedList<RobotAction> lastTenActions;

    public HugRightExplorationController(FastestPathBase fastestPath) {
        super();
        fastestPath.addFastestPathCompletedListener(this);
        this.fastestPath = fastestPath;
        this.unexploredPoints = new ArrayList<>();
    }

    @Override
    public void explore(RobotBase robot, int percentage, double timelimit) {
        super.explore(robot, percentage, timelimit);
        robot.addRobotActionListener(this);
        sensorsScan();

        currentState = States.BOUNDARY;
        unexploredPoints = new ArrayList<Point>();
        neighbourPoints = new ArrayList<>();
        lastTenActions = new LinkedList<RobotAction>();

        exploringUnexplored = new LinkedList<Integer>();
        neighbourCounter = 0;
        aboutTurn = 0;
        //justTurnedCounter = 0;
        justTurned = false;

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
            default:
                break;
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

    // Returns false if points has unexplored or obstacle
    private boolean canMoveStream(Point robotPoint) {
        // Checks cell state;
        CellState state = CellState.NORMAL;
        List<Point> points = getMapState().convertRobotPointToMapPoints(robotPoint);

        for (Point p : points) {
            CellState pstate = getCellState(p);

            if (pstate == null || pstate == CellState.OBSTACLE || pstate == CellState.UNEXPLORED) {// || pstate == CellState.UNEXPLORED){
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
            /*if (getMapState().getMapCellState(point) == CellState.OBSTACLE) {
                return false;
            }*/
            if (getMapState().getMapCellState(point) == CellState.UNEXPLORED) {
                hasUnexplored = true;
            }
        }
        return hasUnexplored;
    }

    // Can also be optimized
    private ArrayList<Point> nearbyRobotPoints(Point rPoint) {
        ArrayList<Point> nearbyRobotPoints = new ArrayList<Point>();

        // Top of rPoint
        if (rPoint.y + 1 < getMapState().getMapSystemDimension().height) {
            nearbyRobotPoints.add(new Point(rPoint.x, rPoint.y + 1));
        }
        // Right of rPoint
        if (rPoint.x + 1 < getMapState().getMapSystemDimension().width) {
            nearbyRobotPoints.add(new Point(rPoint.x + 1, rPoint.y));
        }
        // Bottom of rPoint
        if (rPoint.y - 1 > 0) {
            nearbyRobotPoints.add(new Point(rPoint.x, rPoint.y - 1));
        }
        // Left of rPoint
        if (rPoint.x - 1 > 0) {
            nearbyRobotPoints.add(new Point(rPoint.x - 1, rPoint.y));
        }

        return nearbyRobotPoints;
    }

    // Returns robot points
    private ArrayList<Point> populateUnexploredPoints() {
        ArrayList<Point> temp = new ArrayList();

        for (int y = 0; y < getMapState().getRobotSystemDimension().height; y++) {
            for (int x = 0; x < getMapState().getRobotSystemDimension().width; x++) {
                Point tempPoint = new Point(x, y);
                if (isUnexplored(tempPoint)) {
                    temp.add(tempPoint);
                }
            }
        }
        return temp;
    }

    private ArrayList<List<Point>> populateNeighbourPoints(List<Point> points) {
        ArrayList<List<Point>> temp = new ArrayList();
        for (int i = 0; i < points.size(); i++) {
            List<Point> neighbours = nearbyRobotPoints(points.get(i));
            temp.add(neighbours);
        }
        return temp;
    }
    
    private LinkedList<Integer> orderUnexploredPoints() {
        LinkedList<Integer> temp = new LinkedList();
        for(int i = 0; i< unexploredPoints.size(); i++){
            temp.add(i);
        }
        return temp;
        /*
        // If empty, take from unexploredPoints, order and return
        if(exploringUnexplored.isEmpty()){
            ArrayList<Integer> distances = new ArrayList();
            for(int i = 0; i< unexploredPoints.size(); i++){
                int distance = fastestPath.numberOfMoves(getMapState(), getRobot(), unexploredPoints.get(i));
                distances.add(distance);
            }
            UnexploredPointsComparator comparator = new UnexploredPointsComparator(distances.toArray(new Integer[0]));
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            return (new LinkedList(Arrays.asList(indexes)));
        }
        //If not empty, returned a reorder of existing
        else{
            ArrayList<Integer> distances = new ArrayList();
            for(int i = 0; i< exploringUnexplored.size(); i++){
                int distance = fastestPath.numberOfMoves(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored.get(i)));
                distances.add(distance);
            }
            UnexploredPointsComparator comparator = new UnexploredPointsComparator(distances.toArray(new Integer[0]));
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            return (new LinkedList(Arrays.asList(indexes)));
        }*/
    }

    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {

        this.setNoObstacleUpperLimit(getMapState().convertRobotPointToMapPoints(getRobot().getMapState().getRobotPoint()));

        // Update internal map state
        sensorsScan();

        if (currentState != States.COMPLETED && currentState != States.EXPLORING && obstaclesChanged()) {
            getRobot().move(RobotAction.SCAN);
            System.out.println("Rescanning..");
            resetObstaclesChanged();
            return;
        }

        // check explorated nodes;
        if ((reachedTimeLimit() || reachedCoveragePercentage()) && currentState != States.COMPLETED) {
            preComplete();
            return;
        }

        if (currentState == States.BOUNDARY || currentState == States.EXITING_LOOP) {

            // Check if you're in that weird loop
            lastTenActions.add(actions[0]);
            if (lastTenActions.size() > 10) {
                lastTenActions.pop();
            }
            // Check if last eight actions are TR, F, TR, F, TR, F, TR, F, TR, F
            if (lastTenActions.size() == 10) {
                boolean flag = true;
                for (int i = 0; i < lastTenActions.size(); i++) {
                    if (i % 2 == 0) {
                        if (lastTenActions.get(i) != RobotAction.TURN_RIGHT) {
                            flag = false;
                            break;
                        }
                    } else if (lastTenActions.get(i) != RobotAction.FORWARD) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    currentState = States.LOOPING;
                }
            }

            if (currentState == States.LOOPING) {
                currentState = States.EXITING_LOOP;
                actionPriority[0] = RobotAction.FORWARD;
                actionPriority[1] = RobotAction.TURN_LEFT;
                actionPriority[2] = RobotAction.TURN_RIGHT;

            }

            if (mapdirection != null && getMapState().getRobotPoint().equals(getMapState().getStartPoint()) && getCurrentCoveragePercentage() > 80) {
                currentState = States.EXPLORATION;
                //currentState = States.COMPLETED;
                //complete();
            } else {
                for (int i = 0; i < actionPriority.length; i++) {
                    RobotAction action = actionPriority[i];
                    System.out.println("========================");
                    System.out.println("Current Point: " + getMapState().getRobotPoint() + " Current orienttion: " + getRobot().getCurrentOrientation() + " Checking if I can move " + action);
                    System.out.println("========================");
                    if (canMove(actionToMapDirection(action))) {
                        // Do not turn twice in a row while exploring boundary
                        if (action == RobotAction.TURN_RIGHT || action == RobotAction.TURN_LEFT) {
                            if (justTurned) {
                                continue;
                            } else {
                                justTurned = true;
                            }
                        } else {
                            justTurned = false;
                        }
                        // Stream actions?
                        /*
                        if(action == RobotAction.FORWARD){
                            boolean lookaheadFlag = true;
                            int forwardNo = 1;
                            Point newLocation = nextLocation(getRobot().getCurrentOrientation());
                            while(lookaheadFlag){
                                for(RobotAction lookAheadAction : actionPriority){
                                    if(canMoveStream(newLocation)){
                                        if(lookAheadAction != RobotAction.FORWARD){
                                            lookaheadFlag = false;
                                            break;
                                        }
                                    }
                                }
                                if(lookaheadFlag){
                                    // Check if to be explored is already explored
                                    //else
                                    
                                        lookaheadFlag = false;
                                        break;
                                }
                            }
                            if(lookaheadFlag == true && forwardNo > 1){
                                //stream
                                ArrayList<RobotAction> forwards = new ArrayList<RobotAction>();
                                for(int forward = 0; forward < forwardNo; forward++){
                                    forwards.add(RobotAction.FORWARD);
                                }
                                getRobot().moveStream(streamDirections);
                            }
                        }*/

                        if (currentState == States.EXITING_LOOP && action == RobotAction.TURN_LEFT) {
                            actionPriority[0] = RobotAction.TURN_RIGHT;
                            actionPriority[1] = RobotAction.FORWARD;
                            actionPriority[2] = RobotAction.TURN_LEFT;
                            currentState = States.BOUNDARY;
                        }

                        getRobot().move(action);
                        return;
                    } else if (currentState == States.EXITING_LOOP && action == RobotAction.TURN_LEFT) {
                        actionPriority[0] = RobotAction.TURN_RIGHT;
                        actionPriority[1] = RobotAction.FORWARD;
                        actionPriority[2] = RobotAction.TURN_LEFT;
                        currentState = States.BOUNDARY;
                    }
                }
                currentState = States.ABOUT_TURN;
                actionPriority[0] = RobotAction.TURN_RIGHT;
                actionPriority[1] = RobotAction.FORWARD;
                actionPriority[2] = RobotAction.TURN_LEFT;
            }
        }

        if (currentState == States.ABOUT_TURN) {
            System.out.println("========================");
            System.out.println("About turning now");
            System.out.println("========================");
            /*aboutTurn++;
            if (aboutTurn == 2) {
                currentState = States.BOUNDARY;
                aboutTurn = 0;
                justTurned = true;
            }
            getRobot().move(RobotAction.TURN_RIGHT);*/
            justTurned = true;
            currentState = States.BOUNDARY;
            // Tell Ying Hao that here confirm can send CAL_CORNER
            getRobot().move(RobotAction.ABOUT_TURN);
        }
        if (currentState == States.EXPLORATION) {
            System.out.println("Exploration Round 2");

            unexploredPoints = populateUnexploredPoints();
            neighbourPoints = populateNeighbourPoints(unexploredPoints);
            exploringUnexplored = orderUnexploredPoints();

            if (unexploredPoints.size() > 0) {
                currentState = States.EXPLORING;
                
                while (!fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored.peek()), false)) {
                    for (int i = 0; i < neighbourPoints.get(exploringUnexplored.peek()).size(); i++) {
                        if (fastestPath.move(getMapState(), getRobot(), neighbourPoints.get(exploringUnexplored.peek()).get(i), false)) {
                            neighbourCounter = i;
                            return;
                        }
                    }
                    exploringUnexplored.remove();
                    if(exploringUnexplored.isEmpty()){
                        preComplete();
                        return;
                    }
                }

                //fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored));
            } else {
                preComplete();
            }
        }

        if (currentState == States.COMPLETED && getMapState().getRobotPoint().equals(getMapState().getStartPoint())) {
            complete();
        }
    }

    @Override
    public void onFastestPathCompleted() {
        if (currentState != States.COMPLETED) {
            if (!exploringUnexplored.isEmpty()) {
                if (isUnexplored(unexploredPoints.get(exploringUnexplored.peek()))) {
                    neighbourCounter++;
                    for (int i = neighbourCounter; i < neighbourPoints.get(exploringUnexplored.peek()).size(); i++) {
                        if (fastestPath.move(getMapState(), getRobot(), neighbourPoints.get(exploringUnexplored.peek()).get(i), false)) {
                            return;
                        }
                    }

                }
                exploringUnexplored.remove();
                neighbourCounter = 0;
                while (!isUnexplored(unexploredPoints.get(exploringUnexplored.peek()))) {
                    exploringUnexplored.remove();
                    if (exploringUnexplored.isEmpty()) {
                        break;
                        //preComplete();
                        //return;
                    }
                }
                //exploringUnexplored = orderUnexploredPoints();
                while (!exploringUnexplored.isEmpty() && !fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored.peek()), false)) {
                    for (int i = 0; i < neighbourPoints.get(exploringUnexplored.peek()).size(); i++) {
                        if (fastestPath.move(getMapState(), getRobot(), neighbourPoints.get(exploringUnexplored.peek()).get(i), false)) {
                            neighbourCounter = i;
                            return;
                        }
                    }
                    exploringUnexplored.remove();
                }
            }
            // Check again 
            unexploredPoints = new ArrayList();
            neighbourPoints = new ArrayList();
            exploringUnexplored = new LinkedList<Integer>();;
            neighbourCounter = 0;

            // Tune here depending on the map!
            // U can switch the positioning of the two for loops depending on the map
            unexploredPoints = populateUnexploredPoints();
            neighbourPoints = populateNeighbourPoints(unexploredPoints);
            exploringUnexplored = orderUnexploredPoints();

            if (!unexploredPoints.isEmpty()) {
                currentState = States.EXPLORING;

                while (!fastestPath.move(getMapState(), getRobot(), unexploredPoints.get(exploringUnexplored.peek()), false)) {
                    for (int i = 0; i < neighbourPoints.get(exploringUnexplored.peek()).size(); i++) {
                        if (fastestPath.move(getMapState(), getRobot(), neighbourPoints.get(exploringUnexplored.peek()).get(i), false)) {
                            neighbourCounter = i;
                            return;
                        }
                    }
                    exploringUnexplored.remove();
                    if (exploringUnexplored.isEmpty()) {
                        preComplete();
                        return;
                    }
                }

            }
            if (currentState != States.COMPLETED) {
                preComplete();
            }
        }
        if (currentState == States.COMPLETED) {
            complete();
        }
    }

    @Override
    public void complete() {
        getRobot().removeRobotActionListener(this);

        RobotBase robot = getRobot();
        CalibrationSpecification spec = robot.getCalibrationSpecifications().get(0);
        if (spec.isInPosition(getRobot(), RobotAction.ABOUT_TURN)) {
            robot.move(RobotAction.ABOUT_TURN);
        } else if (spec.isInPosition(getRobot(), RobotAction.TURN_LEFT)) {
            robot.move(RobotAction.TURN_LEFT);
        } else if (spec.isInPosition(getRobot(), RobotAction.TURN_RIGHT)) {
            robot.move(RobotAction.TURN_RIGHT);
        }
        robot.dispatchCalibration(spec.getCalibrationType());

        getRobot().stop();
        super.complete();

    }

    private void preComplete() {
        System.out.println("Precomplete");
        currentState = States.COMPLETED;
        //if(!getMapState().getRobotPoint().equals(getMapState().getStartPoint())){
        fastestPath.move(getMapState(), getRobot(), getMapState().getStartPoint(), false);
        //}
    }

}
