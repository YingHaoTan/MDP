package mdp.models;

import java.awt.Dimension;
import java.awt.Point;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MapState is a model class that stores the CellState and conversion methods
 * between map and robot coordinate system
 *
 * Example:
 * <pre>
 * Assuming a robot dimension of (3, 3) with a map coordinate system of size (4 x 5)
 * will be reduced to (2 x 3) robot coordinate system
 *
 * Map Coordinate System (4 x 5)
 * ------------------------------------
 * | 0, 3 | 1, 3 | 2, 3 | 3, 3 | 4, 3 |
 * ------------------------------------
 * | 0, 2 | 1, 2 | 2, 2 | 3, 2 | 4, 2 |
 * ------------------------------------
 * | 0, 1 | 1, 1 | 2, 1 | 3, 1 | 4, 1 |
 * ------------------------------------
 * | 0, 0 | 1, 0 | 2, 0 | 3, 0 | 4, 0 |
 * ------------------------------------
 *
 * Robot Coordinate System (2 x 3)
 *
 * -----------------------------
 * | 0, 1 | 1, 1 | 2, 1 | 3, 1 |
 * -----------------------------
 * | 0, 0 | 1, 0 | 2, 0 | 3, 0 |
 * -----------------------------
 *
 * Where the following mapping between Map Coordinate System <=> Robot Coordinate System exists:
 * {(0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1), (0, 2), (1, 2), (2, 2)} <=> (0, 0)
 * {(1, 0), (2, 0), (3, 0), (1, 1), (2, 1), (3, 1), (1, 2), (2, 2), (3, 2)} <=> (1, 0)
 * {(0, 1), (1, 1), (2, 1), (0, 2), (1, 2), (2, 2), (0, 3), (1, 3), (2, 3)} <=> (0, 1)
 *
 * </pre>
 *
 * @author Ying Hao
 */
public class MapState {

    private Dimension mapdim;
    private Dimension robotdim;
    private Point robotpoint;
    private Point endpoint;
    private Point waypoint;
    private Point startpoint;
    private CellState[][] cellstates;

    public MapState(Dimension mapdim, Dimension robotdim) {
        this.mapdim = mapdim;
        this.robotdim = robotdim;
        this.robotpoint = new Point(0, 0);
        this.startpoint = new Point(0, 0);

        Dimension rsystemdim = getRobotSystemDimension();
        this.endpoint = new Point(rsystemdim.width - 1, rsystemdim.height - 1);
        this.cellstates = new CellState[mapdim.width][mapdim.height];

        setMapCellState(CellState.NORMAL);
    }

    /**
     * Gets the dimension of the map coordinate system
     *
     * @return
     */
    public Dimension getMapSystemDimension() {
        return mapdim;
    }

    /**
     * Gets the dimension of the robot coordinate system
     *
     * @return
     */
    public Dimension getRobotSystemDimension() {
        return new Dimension(this.mapdim.width - this.robotdim.width + 1, this.mapdim.height - this.robotdim.height + 1);
    }

    /**
     * Gets the dimension of the robot
     *
     * @return
     */
    public Dimension getRobotDimension() {
        return robotdim;
    }

    /**
     * Gets the robot point in robot coordinate system
     *
     * @return
     */
    public Point getRobotPoint() {
        return robotpoint;
    }

    /**
     * Sets the robot point in robot coordinate system
     *
     * @param robotpoint
     */
    public void setRobotPoint(Point robotpoint) {
        this.robotpoint = robotpoint;
    }
    
    /**
     * Gets the start point point in robot coordinate system
     *
     * @return
     */
    public Point getStartPoint() {
        return startpoint;
    }

    /**
     * Sets the start point in robot coordinate system
     *
     * @param endpoint
     */
    public void setStartPoint(Point startpoint) {
        this.startpoint = startpoint;
    }

    /**
     * Gets the end point in robot coordinate system
     *
     * @return
     */
    public Point getEndPoint() {
        return endpoint;
    }

    /**
     * Sets the end point in robot coordinate system
     *
     * @param endpoint
     */
    public void setEndPoint(Point endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the waypoint in map coordinate system
     *
     * @return
     */
    public Point getWayPoint() {
        return waypoint;
    }

    /**
     * Gets the CellState at the specified point in robot coordinate system
     *
     * @param point
     * @return
     */
    public CellState getRobotCellState(Point point) {
        CellState state = null;
        Dimension rdim = getRobotSystemDimension();

        if(point.x >= 0 && point.y >= 0 && point.x < rdim.width && point.y < rdim.height) {
        	state = CellState.NORMAL;
        	
	        List<Point> points = this.convertRobotPointToMapPoints(point);
	        for (Point p : points) {
	            CellState pstate = getMapCellState(p);
	
	            if (pstate == CellState.OBSTACLE
	                    || (pstate == CellState.UNEXPLORED && state != CellState.OBSTACLE)
	                    || (pstate == CellState.WAYPOINT && state != CellState.UNEXPLORED && state != CellState.OBSTACLE)) {
	                state = pstate;
	            }
	        }
        }

        return state;
    }

    /**
     * Gets the CellState at the specified point in map coordinate system
     *
     * @param point
     */
    public CellState getMapCellState(Point point) {
        if (point.x >= 0 && point.y >= 0 && point.x < cellstates.length && point.y < cellstates[point.x].length)
            return this.cellstates[point.x][point.y];
            
        return null;
    }

    /**
     * Sets the CellState at the specified point in map coordinate system
     *
     * @param point
     * @return true if successful, false if unsuccessful
     */
    public boolean setMapCellState(Point point, CellState state) {

        if (point.x >= 0 && point.y >= 0 && point.x < cellstates.length && point.y < cellstates[0].length) {
            this.cellstates[point.x][point.y] = state;

            if (state == CellState.WAYPOINT) {
                // Clears previous waypoint
                if (this.waypoint != null && this.waypoint != point) {
                    this.setMapCellState(waypoint, CellState.NORMAL);
                }

                // Set waypoint value
                this.waypoint = point;
            }
            return true;
        }
        return false;
    }

    /**
     * Sets all the CellState in map coordinate system to state, ignoring those
     * that cannot be assigned the CellState
     *
     * @param point
     * @param state
     */
    public void setMapCellState(CellState state) {
        for (int x = 0; x < this.cellstates.length; x++) {
            for (int y = 0; y < this.cellstates[x].length; y++) {
                setMapCellState(new Point(x, y), state);
            }
        }
    }

    /**
     * Resets the robot to the starting point
     */
    public void reset() {
    	this.robotpoint = this.startpoint;
    }

    
    // I think this is wrong
    /**
     * Converts a point in map coordinate system to a set of points in robot
     * coordinate system
     *
     * @param mappoint
     * @return
     */
    public List<Point> convertMapPointToRobotPoints(Point mappoint) {
        List<Point> points = new ArrayList<>();

        for (int x = 0; x < robotdim.width; x++) {
            for (int y = 0; y < robotdim.height; y++) {
                Point p = new Point(mappoint.x + x - (robotdim.width / 2), mappoint.y + y - (robotdim.height / 2));

                if (p.x >= 0 && p.x < cellstates.length && p.y >= 0 && p.y < cellstates[p.x].length) {
                    points.add(p);
                }
            }
        }

        return points;
    }

    /**
     * Converts a point in robot coordinate system to a set of points in map
     * coordinate system
     *
     * @param robotpoint
     * @return
     */
    public List<Point> convertRobotPointToMapPoints(Point robotpoint) {
        List<Point> points = new ArrayList<>();

        for (int x = 0; x < robotdim.width; x++) {
            for (int y = 0; y < robotdim.height; y++) {
                points.add(new Point(robotpoint.x + x, robotpoint.y + y));
            }
        }

        return points;
    }

    /**
     * Parses the mdf1 and mdf2 representation and loads the map with the
     * descriptors
     *
     * @param mdf1
     * @param mdf2
     * @return
     */
    public void parseString(String mdf1, String mdf2) {
        String mdf1bin = new BigInteger(mdf1, 16).toString(2);
        String mdf2bin = new BigInteger(mdf2, 16).toString(2);

        mdf2bin = String.join("", Collections.nCopies(mdf2.length() * 4 - mdf2bin.length(), "0")) + mdf2bin;

        int mdf1counter = 0;
        int mdf2counter = 0;
        mdf1bin = mdf1bin.substring(2, mdf1bin.length() - 2);
        
        for (int y = 0; y < this.mapdim.height; y++) {
            for (int x = 0; x < this.mapdim.width; x++) {
                if (mdf1bin.substring(mdf1counter, mdf1counter + 1).equals("0")) {
                    this.cellstates[x][y] = CellState.UNEXPLORED;
                } else {
                    if (mdf2bin.substring(mdf2counter, mdf2counter + 1).equals("1")) {
                        this.cellstates[x][y] = CellState.OBSTACLE;
                    }
                    else {
                    	this.cellstates[x][y] = CellState.NORMAL;
                    }

                    mdf2counter++;
                }

                mdf1counter++;
            }
        }
    }

    /**
     * Returns a string representation of this map based on the
     * MapDescriptorFormat specified in hexadecimal
     *
     * @param format
     * @return
     */
    public String toString(MapDescriptorFormat format) {
        String descriptor = new String();

        int bitcount = 0;
        for (int y = 0; y < this.mapdim.height; y++) {
            for (int x = 0; x < this.mapdim.width; x++) {
                boolean explored = this.cellstates[x][y] != CellState.UNEXPLORED;

                if (format == MapDescriptorFormat.MDF1) {
                    descriptor += explored ? "1" : "0";
                } else if (explored) {
                    descriptor += this.cellstates[x][y] == CellState.OBSTACLE ? "1" : "0";
                    bitcount++;
                }
            }
        }

        if (format == MapDescriptorFormat.MDF1) {
            descriptor = "11" + descriptor + "11";
        } else {
            bitcount = bitcount % 8;
            if (bitcount > 0) {
                descriptor += String.join("", Collections.nCopies(8 - bitcount, "0"));
            }
        }

        int expectedlength = descriptor.length() / 4;
        String hexstring = new BigInteger(descriptor, 2).toString(16).toUpperCase();
        if (expectedlength > hexstring.length()) {
            hexstring = String.join("", Collections.nCopies(expectedlength - hexstring.length(), "0")) + hexstring;
        }

        return hexstring;
    }

    /**
     * Clones a new instance of MapState that contains identical information
     * with this MapState instance
     */
    public MapState clone() {
        MapState mstate = new MapState(new Dimension(mapdim.width, mapdim.height), new Dimension(robotdim.width, robotdim.height));
        mstate.robotpoint = new Point(robotpoint.x, robotpoint.y);
        mstate.endpoint = new Point(endpoint.x, endpoint.y);
        if (waypoint != null) {
            mstate.waypoint = new Point(waypoint.x, waypoint.y);
        }

        for (int i = 0; i < cellstates.length; i++) {
            System.arraycopy(cellstates[i], 0, mstate.cellstates[i], 0, mstate.cellstates[i].length);
        }

        return mstate;
    }

}
