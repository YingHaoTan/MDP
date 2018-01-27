package mdp.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mdp.models.CellState;
import mdp.robots.RobotBase;

/**
 * ExplorationBase is an abstract base class for all exploration planner classes
 *
 * @author Ying Hao
 */
public abstract class ExplorationBase {

    private CellState[][] cellstates;
    private List<CellStateUpdateListener> listeners;
    private RobotBase robot;
    private Point ecoordinate;

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
        this.robot = robot;
        this.ecoordinate = ecoordinate;

        Dimension robotdim = robot.getDimension();

        this.cellstates = new CellState[mapdim.width][mapdim.height];

        for (CellState[] states : this.cellstates) {
            Arrays.fill(states, CellState.UNEXPLORED);
        }

        for (int x = 0; x < robotdim.width; x++) {
            for (int y = 0; y < robotdim.height; y++) {
                this.cellstates[rcoordinate.x + x][rcoordinate.y + y] = CellState.NORMAL;
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

        // Checks for out of bounds
        if (coordinate.x >= 0 && coordinate.y >= 0 && coordinate.x < this.cellstates.length && coordinate.y < this.cellstates[0].length) {
            this.cellstates[coordinate.x][coordinate.y] = state;

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
        if (coordinate.x >= 0 && coordinate.y >= 0 && coordinate.x < this.cellstates.length && coordinate.y < this.cellstates[0].length) {
            return this.cellstates[coordinate.x][coordinate.y];
        }
        return null;
    }

    protected RobotBase getRobot() {
        return this.robot;
    }

    protected Point getEndpoint() {
        return this.ecoordinate;
    }

}
