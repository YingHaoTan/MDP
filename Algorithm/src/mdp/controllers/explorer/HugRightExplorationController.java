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
import java.util.ListIterator;

import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.FastestPathCompletedListener;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapDescriptorFormat;
import mdp.models.RobotAction;
import mdp.models.MDFTuple;
import mdp.robots.CalibrationSpecification;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;

/**
 *
 * @author JINGYANG
 */
public class HugRightExplorationController extends ExplorationBase implements RobotActionListener, FastestPathCompletedListener {

    enum States {

        BOUNDARY, ABOUT_TURN, LOOPING, EXITING_LOOP, EXITING_INDEFLOOP ,STAIRS_PHASE_ONE, STAIRS_PHASE_TWO, EXPLORATION, EXPLORING, COMPLETED
    };
    FastestPathBase fastestPath;
    RobotAction[] actionPriority = {RobotAction.TURN_RIGHT, RobotAction.FORWARD, RobotAction.TURN_LEFT};
    List<Point> unexploredPoints;
    List<List<Point>> neighbourPoints;

    LinkedList<Integer> exploringUnexplored;
    int neighbourCounter;
    int aboutTurn;
    int indefLoopCounter;
    int indefThreshold = 45;
    int stairsPhase1Counter;
    int stairsPhase2Counter;
    int stairsToMove;
    //int justTurnedCounter;
    boolean justTurned;
    boolean stopped;
    boolean reachedGoal;
    States currentState;

    LinkedList<RobotAction> lastTenActions;
    RobotBase prev;
    MDFTuple prevMdf;


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
        sensorsScan(getRobot(), 1);

        currentState = States.BOUNDARY;
        unexploredPoints = new ArrayList<Point>();
        neighbourPoints = new ArrayList<>();
        lastTenActions = new LinkedList<RobotAction>();

        exploringUnexplored = new LinkedList<Integer>();
        neighbourCounter = 0;
        aboutTurn = 0;
        stairsPhase1Counter = 0;
        stairsPhase2Counter = 0;
        stairsToMove = 0;
        justTurned = false;
        stopped = false;
        reachedGoal = false;
        indefLoopCounter = 0;
        prevMdf = new MDFTuple(getMapState().toString(MapDescriptorFormat.MDF1), getMapState().toString(MapDescriptorFormat.MDF2));
        
        for (RobotAction action : actionPriority) {
            if (canMove(actionToMapDirection(action))) {
                if (action == RobotAction.TURN_RIGHT || action == RobotAction.TURN_LEFT) {
                    justTurned = true;
                } else {
                    justTurned = false;
                }
                prev = getRobot().clone();
                getRobot().move(action);
                return;
            }
        }
        justTurned = false;
        prev = getRobot().clone();
        getRobot().move(RobotAction.ABOUT_TURN);
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

        for (int x = 0; x < getMapState().getRobotSystemDimension().width; x++) {
            for (int y = 0; y < getMapState().getRobotSystemDimension().height; y++) {
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
        assert (unexploredPoints.size() == neighbourPoints.size());

        /*LinkedList<Integer> temp = new LinkedList();
        for (int i = 0; i < unexploredPoints.size(); i++) {
            temp.add(i);
        }
        return temp;*/
        // If empty, take from unexploredPoints, order and return
        if (exploringUnexplored.isEmpty()) {
            ArrayList<Integer> distances = new ArrayList();
            for (int i = 0; i < unexploredPoints.size(); i++) {
                int totalDistance = 0;
                int count = 0;
                for (int j = 0; j < neighbourPoints.get(i).size(); j++) {
                    int distance = fastestPath.numberOfMoves(getMapState(), getRobot(), neighbourPoints.get(i).get(j));
                    if (distance > 0) {
                        totalDistance += distance;
                        count++;
                    }
                }
                if (count == 0) {
                    // unreachables
                    distances.add(99);
                } else {
                    distances.add(totalDistance / count);
                }
            }
            UnexploredPointsComparator comparator = new UnexploredPointsComparator(distances.toArray(new Integer[0]));
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            return (new LinkedList(Arrays.asList(indexes)));
        } //return null;
        else {
            ArrayList<Integer> distances = new ArrayList();
            for (int i = 0; i < exploringUnexplored.size(); i++) {
                int totalDistance = 0;
                int count = 0;
                for (int j = 0; j < neighbourPoints.get(exploringUnexplored.get(i)).size(); j++) {
                    int distance = fastestPath.numberOfMoves(getMapState(), getRobot(), neighbourPoints.get(exploringUnexplored.get(i)).get(j));
                    if (distance > 0) {
                        totalDistance += distance;
                        count++;
                    }
                }

                if (count == 0) {
                    // unreachables
                    distances.add(99);
                } else {
                    distances.add(totalDistance / count);
                }
            }
            UnexploredPointsComparator comparator = new UnexploredPointsComparator(distances.toArray(new Integer[0]));
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);
            LinkedList<Integer> results = new LinkedList();
            for (int i = 0; i < indexes.length; i++) {
                results.add(exploringUnexplored.get(indexes[i]));
            }

            return results;
        }

    }

    // return size of stairs
    private int checkStairs(int space, Point stairPoint, Direction orientation) {
        if (getMapState().getMapCellState(stairPoint) == CellState.OBSTACLE) {
            int temp = space;
            boolean flag = true;
            while (temp > 0) {
                if (orientation == Direction.UP) {
                    if (getMapState().getMapCellState(new Point(stairPoint.x - temp, stairPoint.y)) != CellState.NORMAL) {
                        flag = false;
                    }
                } else if (orientation == Direction.DOWN) {
                    if (getMapState().getMapCellState(new Point(stairPoint.x + temp, stairPoint.y)) != CellState.NORMAL) {
                        flag = false;
                    }
                } else if (orientation == Direction.LEFT) {
                    if (getMapState().getMapCellState(new Point(stairPoint.x, stairPoint.y - temp)) != CellState.NORMAL) {
                        flag = false;
                    }
                } else if (orientation == Direction.RIGHT) {
                    if (getMapState().getMapCellState(new Point(stairPoint.x, stairPoint.y + temp)) != CellState.NORMAL) {
                        flag = false;
                    }
                }
                temp--;

            }
            if (!flag) {
                // can stick to BOUNDARY
                return 0;
            } else {
                if (orientation == Direction.UP) {
                    return 1 + checkStairs(space + 1, new Point(stairPoint.x + 1, stairPoint.y + 1), orientation);
                } else if (orientation == Direction.DOWN) {
                    return 1 + checkStairs(space + 1, new Point(stairPoint.x - 1, stairPoint.y - 1), orientation);
                } else if (orientation == Direction.LEFT) {
                    return 1 + checkStairs(space + 1, new Point(stairPoint.x - 1, stairPoint.y + 1), orientation);
                } else if (orientation == Direction.RIGHT) {
                    return 1 + checkStairs(space + 1, new Point(stairPoint.x + 1, stairPoint.y - 1), orientation);
                }
            }
        } else {
            return 0;
        }
        return 0;
    }

    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
        if (actions[0] == RobotAction.CAL_CORNER || actions[0] == RobotAction.CAL_SIDE || actions[0] == RobotAction.CAL_JIEMING) {
            sensorsScan(prev, 1.5);
            return;
        }
        prev = getRobot().clone();

        //System.out.println("Robot action completed: " + actions[0]);
        this.setNoObstacleUpperLimit(getMapState().convertRobotPointToMapPoints(getRobot().getMapState().getRobotPoint()));

        // Update internal map state
        sensorsScan(getRobot(), 1);

        if (getMapState().getRobotPoint().equals(getMapState().getEndPoint())){
            reachedGoal = true;
            
        }
        //System.out.println("********Reached Goal = " + reachedGoal+ "*******************");
        if (currentState != States.COMPLETED && currentState != States.EXPLORING && obstaclesChanged()) {
            getRobot().move(RobotAction.SCAN);
            //System.out.println("Rescanning..");
            resetObstaclesChanged();
            return;
        }

        // check explorated nodes;
        if ((reachedTimeLimit() || reachedCoveragePercentage()) && currentState != States.COMPLETED) {
            preComplete();
            return;
        }

        if (currentState == States.BOUNDARY || currentState == States.EXITING_LOOP || currentState == States.STAIRS_PHASE_ONE || currentState ==  States.STAIRS_PHASE_TWO) {

            // Check if you're in that loop
            if (actions[0] != RobotAction.SCAN && currentState == States.BOUNDARY) {
                lastTenActions.add(actions[0]);
                if (lastTenActions.size() > 10) {
                    lastTenActions.pop();
                }
                // Check if last 10 actions are TR, F, TR, F, TR, F, TR, F, TR, F
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
                        indefLoopCounter = 0;
                    }
                }
                
               if(getMapState().toString(MapDescriptorFormat.MDF1).equals(prevMdf.mdf1) && getMapState().toString(MapDescriptorFormat.MDF2).equals(prevMdf.mdf2)){
                    indefLoopCounter++;
                } 
                else{
                    indefLoopCounter = 0;
                }
               prevMdf.mdf1 = getMapState().toString(MapDescriptorFormat.MDF1);
               prevMdf.mdf2 = getMapState().toString(MapDescriptorFormat.MDF2);
               
               if(indefLoopCounter >= indefThreshold){
                   currentState = States.EXITING_INDEFLOOP;
                   indefLoopCounter = 0;
                   fastestPath.move(getMapState(), getRobot(), getMapState().getStartPoint(), false);
                   return;
               }
               
               System.out.println("indefLoopCounter:" + indefLoopCounter);
            }

            if (currentState == States.LOOPING) {
                currentState = States.EXITING_LOOP;
                actionPriority[0] = RobotAction.FORWARD;
                actionPriority[1] = RobotAction.TURN_LEFT;
                actionPriority[2] = RobotAction.TURN_RIGHT;

            }

            // Checks if you should skip stairs;
            if (currentState == States.BOUNDARY) {
                int stairs = 0;
                Point robotPoint = getMapState().getRobotPoint();
                if (getRobot().getCurrentOrientation() == Direction.DOWN) {
                    Point lowerRight = new Point(robotPoint.x - 1, robotPoint.y + 2);
                    stairs = checkStairs(0, lowerRight, Direction.DOWN);
                } else if (getRobot().getCurrentOrientation() == Direction.UP) {
                    Point lowerRight = new Point(robotPoint.x + 3, robotPoint.y);
                    stairs = checkStairs(0, lowerRight, Direction.UP);
                }
                if (getRobot().getCurrentOrientation() == Direction.LEFT) {
                    Point lowerRight = new Point(robotPoint.x + 2, robotPoint.y + 3);
                    stairs = checkStairs(0, lowerRight, Direction.LEFT);
                } else if (getRobot().getCurrentOrientation() == Direction.RIGHT) {
                    Point lowerRight = new Point(robotPoint.x, robotPoint.y - 1);
                    stairs = checkStairs(0, lowerRight, Direction.RIGHT);
                }
                if (stairs > 1) {
                    currentState = States.STAIRS_PHASE_ONE;
                    stairsToMove = stairs;
                    actionPriority[0] = RobotAction.FORWARD;
                    actionPriority[1] = RobotAction.TURN_RIGHT;
                    actionPriority[2] = RobotAction.TURN_LEFT;
                }
            }
            
            if(currentState  == States.STAIRS_PHASE_ONE){
                if(stairsToMove == 0 || !canMove(actionToMapDirection(RobotAction.FORWARD))){
                    stairsPhase2Counter = stairsPhase1Counter;
                    currentState = States.STAIRS_PHASE_TWO;
                    getRobot().move(RobotAction.TURN_RIGHT);
                    return;
                }
            }
            
            if(currentState == States.STAIRS_PHASE_TWO){
                 if(stairsPhase2Counter == 0 || !canMove(actionToMapDirection(RobotAction.FORWARD))){
                    currentState = States.BOUNDARY;
                    actionPriority[0] = RobotAction.TURN_RIGHT;
                    actionPriority[1] = RobotAction.FORWARD;
                    actionPriority[2] = RobotAction.TURN_LEFT;
                    stairsToMove = 0;
                    stairsPhase1Counter = 0;
                    stairsPhase2Counter = 0;
                    
                    for (int i = 1; i < actionPriority.length; i++) {
                        RobotAction action = actionPriority[i];
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

                            getRobot().move(action);
                            return;
                        }
                    }
                    justTurned = false;
                    CalibrationSpecification spec = getRobot().getCalibrationSpecifications().get(0);
                    if (spec.isInPosition(getRobot(), RobotAction.TURN_LEFT)) {
                        getRobot().move(RobotAction.TURN_LEFT);
                    }
                    else{
                        getRobot().move(RobotAction.ABOUT_TURN);
                    }
                    return;
                }
            }

            // Don't do round 2
            System.out.println("Coverage");
            System.out.println(getCurrentCoveragePercentage());
            if(getMapState().getRobotPoint().equals(getMapState().getStartPoint()) && getCurrentCoveragePercentage() >= 60){
            //if(mapdirection != null && getMapState().getRobotPoint().equals(getMapState().getStartPoint()) && getCurrentCoveragePercentage() >= 95){
                currentState = States.COMPLETED;
                complete();
                return;
            }

            // To solve Zhi Jie's map where robot will go back to the Start while hugging right in a few moves, that's why this condition: "getCurrentCoveragePercentage() > 20" is added
            if ((getMapState().getRobotPoint().equals(getMapState().getStartPoint())) && ((getCurrentCoveragePercentage() < 60 && getCurrentCoveragePercentage() > 50))) {
            //if (mapdirection != null && getMapState().getRobotPoint().equals(getMapState().getStartPoint()) && getCurrentCoveragePercentage() < 95 && getCurrentCoveragePercentage() > 20) {
                System.out.println(getCurrentCoveragePercentage());
                currentState = States.EXPLORATION;
                //currentState = States.COMPLETED;
                //complete();
            } else {
                for (int i = 0; i < actionPriority.length; i++) {
                    RobotAction action = actionPriority[i];
                    //System.out.println("========================");
                    //System.out.println("Current Point: " + getMapState().getRobotPoint() + " Current orienttion: " + getRobot().getCurrentOrientation() + " Checking if I can move " + action);
                    //System.out.println("========================");
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

                        // If Robot cannot FORWARD
                        if (currentState == States.EXITING_LOOP && action == RobotAction.TURN_LEFT) {
                            actionPriority[0] = RobotAction.TURN_RIGHT;
                            actionPriority[1] = RobotAction.FORWARD;
                            actionPriority[2] = RobotAction.TURN_LEFT;
                            currentState = States.BOUNDARY;
                            indefLoopCounter = 0;
                        }
                        
                        if(currentState == States.STAIRS_PHASE_ONE & action == RobotAction.FORWARD){
                            stairsPhase1Counter++;
                            stairsToMove--;
                        }
                        
                        if(currentState == States.STAIRS_PHASE_TWO & action == RobotAction.FORWARD){
                            stairsPhase2Counter--;
                        }

                        getRobot().move(action);
                        return;
                    } else if (currentState == States.EXITING_LOOP && action == RobotAction.TURN_LEFT) {

                        // If Robot cannot FORWARD AND TURN_LEFT, will force to ABOUT_TURN
                        actionPriority[0] = RobotAction.TURN_RIGHT;
                        actionPriority[1] = RobotAction.FORWARD;
                        actionPriority[2] = RobotAction.TURN_LEFT;
                        currentState = States.BOUNDARY;
                        indefLoopCounter = 0;
                    }
                }
                currentState = States.ABOUT_TURN;
                actionPriority[0] = RobotAction.TURN_RIGHT;
                actionPriority[1] = RobotAction.FORWARD;
                actionPriority[2] = RobotAction.TURN_LEFT;
            }
        }

        if (currentState == States.ABOUT_TURN) {
            justTurned = false;
            CalibrationSpecification spec = getRobot().getCalibrationSpecifications().get(0);
            if (spec.isInPosition(getRobot(), RobotAction.TURN_LEFT)) {
                currentState = States.BOUNDARY;
                getRobot().move(RobotAction.TURN_LEFT);
            }
            else{
                currentState = States.BOUNDARY;
                getRobot().move(RobotAction.ABOUT_TURN);
            }
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
                    if (exploringUnexplored.isEmpty()) {
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
        if(currentState == States.EXITING_INDEFLOOP){
            currentState = States.BOUNDARY;
            // To go back to onRobotActionCompleted()
            getRobot().move(RobotAction.SCAN);
            return;
        }
        //System.out.println("Robot is at " + getRobot().getMapState().getRobotPoint());
        if (currentState != States.COMPLETED && currentState != States.EXITING_INDEFLOOP) {
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

                // need to reorder unexplored points
                if (!exploringUnexplored.isEmpty()) {
                    exploringUnexplored = orderUnexploredPoints();
                }

                while (!isUnexplored(unexploredPoints.get(exploringUnexplored.peek()))) {
                    exploringUnexplored.remove();
                    if (exploringUnexplored.isEmpty()) {
                        break;
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
                    if (exploringUnexplored.isEmpty()) {
                        break;
                    }
                    while (!isUnexplored(unexploredPoints.get(exploringUnexplored.peek()))) {
                        exploringUnexplored.remove();
                        if (exploringUnexplored.isEmpty()) {
                            break;
                        }
                    }
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
        if (!stopped) {
            getRobot().removeRobotActionListener(this);
            RobotBase robot = getRobot();
            CalibrationSpecification spec = robot.getCalibrationSpecifications().get(0);
            if (spec.isInPosition(getRobot(), RobotAction.ABOUT_TURN)) {
                System.out.println("Last:" + RobotAction.ABOUT_TURN);
                robot.move(RobotAction.ABOUT_TURN);
            } else if (spec.isInPosition(getRobot(), RobotAction.TURN_LEFT)) {
                System.out.println("Last:" + RobotAction.TURN_LEFT);
                robot.move(RobotAction.TURN_LEFT);
            } else if (spec.isInPosition(getRobot(), RobotAction.TURN_RIGHT)) {
                System.out.println("Last:" + RobotAction.TURN_RIGHT);
                robot.move(RobotAction.TURN_RIGHT);
            }
            robot.dispatchCalibration(spec.getCalibrationType());
            System.out.println("Last:" + spec.getCalibrationType());
            getRobot().stop();
            super.complete();
            stopped = true;
        }

    }

    private void preComplete() {
        //System.out.println("Precomplete");
        currentState = States.COMPLETED;
        //if(!getMapState().getRobotPoint().equals(getMapState().getStartPoint())){
        fastestPath.move(getMapState(), getRobot(), getMapState().getStartPoint(), false);
        //}
    }

}
