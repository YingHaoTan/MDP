package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mdp.models.CellState;
import mdp.models.MapState;
import mdp.robots.RobotBase;

/**
 * ExplorationBase is an abstract base class for all exploration planner classes
 *
 * @author Ying Hao
 */
public abstract class ExplorationBase {

    private MapState mstate;
    private List<CellStateUpdateListener> listeners;
    private RobotBase robot;

    public ExplorationBase() {
        this.listeners = new ArrayList<>();
    }

    /**
     * Performs exploration with the provided robot and current coordinate of
     * the robot in robot coordinate system
     *
     * @param robot
     * @param start
     */
    public void explore(Dimension mapdim, RobotBase robot, Point rcoordinate, Point ecoordinate) {
        Dimension robotdim = robot.getDimension();
        this.robot = robot;
        mstate = new MapState(mapdim, robot.getDimension());
        mstate.setMapCellState(CellState.UNEXPLORED);
        mstate.setEndPoint(ecoordinate);
        mstate.setRobotPoint(rcoordinate);

        for (int x = 0; x < robotdim.width; x++) {
            for (int y = 0; y < robotdim.height; y++) {
                mstate.setMapCellState(new Point(rcoordinate.x + x, rcoordinate.y + y), CellState.NORMAL);
            }
        }
    }

    /**
     * Adds CellStateUpdateListener
     *
     * @param listener
     */
    public void addCellStateUpdateListener(CellStateUpdateListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes CellStateUpdateListener
     *
     * @param listener
     */
    public void removeCellStateUpdateListener(CellStateUpdateListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Sets the CellState at a particular coordinate
     *
     * @param coordinate
     * @param state
     * @param label
     */
    protected void setCellState(Point coordinate, CellState state, String label) {

        if (mstate.setMapCellState(coordinate, state)) {

            for (CellStateUpdateListener listener : listeners) {
                listener.onCellStateUpdate(coordinate, state, label);
            }
        }
    }

    /**
     * Gets the CellState at a particular coordinate
     *
     * @param coordinate
     * @return
     */
    protected CellState getCellState(Point coordinate) {

        return mstate.getMapCellState(coordinate);
    }

    /**
     * Gets the map state
     *
     * @return
     */
    protected MapState getMapState() {
        return mstate;
    }

    protected RobotBase getRobot() {
        return robot;
    }

}
