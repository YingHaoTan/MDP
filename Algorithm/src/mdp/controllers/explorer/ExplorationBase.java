package mdp.controllers.explorer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.CellState;
import mdp.robots.RobotBase;

/**
 * ExplorationBase is an abstract base class for all exploration planner classes
 *
 * @author Ying Hao
 */
public abstract class ExplorationBase extends MovementBase {

    private List<ExplorationCompletedListener> eclisteners;
    private int coveragepercentage;
    private double timelimit;
    private long starttime;

    public ExplorationBase() {
        this.eclisteners = new ArrayList<>();
    }

    /**
     * Performs exploration with the provided robot and current coordinate of
     * the robot in robot coordinate system
     *
     * @param mstate
     * @param robot
     * @param start
     */
    public void explore(RobotBase robot, int percentage, double timelimit) {
        this.coveragepercentage = percentage;
        this.timelimit = timelimit;
        
        setRobot(robot);
        setMapState(robot.getMapState());

        this.starttime = System.currentTimeMillis();
    }

    private int getTargetCoveragePercentage() {
        return this.coveragepercentage;
    }

    protected boolean reachedCoveragePercentage() {
        int targetPercentage = getTargetCoveragePercentage();
        int explored = 0;
        for (int y = 0; y < getMapState().getMapSystemDimension().height; y++) {
            for (int x = 0; x < getMapState().getMapSystemDimension().width; x++) {
                if (getMapState().getMapCellState(new Point(x, y)) != CellState.UNEXPLORED) {
                    explored++;
                }
            }
        }
        if (getMapState().getMapSystemDimension().height * getMapState().getMapSystemDimension().width * targetPercentage / 100 <= explored) {
            return true;
        }
        return false;
    }
    
    protected int getCurrentCoveragePercentage(){
    int explored = 0;
        for (int y = 0; y < getMapState().getMapSystemDimension().height; y++) {
            for (int x = 0; x < getMapState().getMapSystemDimension().width; x++) {
                if (getMapState().getMapCellState(new Point(x, y)) != CellState.UNEXPLORED) {
                    explored++;
                }
            }
        }
        return (explored * 100)/(getMapState().getMapSystemDimension().height * getMapState().getMapSystemDimension().width);
    }

    /**
     * Gets time limit of exploration in seconds
     *
     * @return Gets time limit of exploration in seconds, -1 if there's no time
     * limit
     */
    private double getTimeLimit() {
        return this.timelimit;
    }

    protected boolean reachedTimeLimit() {
        if (timelimit > 0) {
            if (System.currentTimeMillis() >= this.starttime + getTimeLimit() * 1000) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds ExplorationCompletedListener
     *
     * @param listener
     */
    public void addExplorationCompletedListener(ExplorationCompletedListener listener) {
        this.eclisteners.add(listener);
    }

    /**
     * Removes ExplorationCompletedListener
     *
     * @param listener
     */
    public void removeExplorationCompletedListener(ExplorationCompletedListener listener) {
        this.eclisteners.remove(listener);
    }

    /**
     * Notifies listeners of completion of exploration
     */
    protected void complete() {
        for (ExplorationCompletedListener listener : eclisteners) {
            listener.onExplorationComplete();
        }
    }
}
