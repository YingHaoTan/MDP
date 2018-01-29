package mdp.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;

import mdp.graphics.ExecutionMode;
import mdp.graphics.MapInteractionMode;
import mdp.graphics.MdpWindow;
import mdp.graphics.input.CoordinateInputPane;
import mdp.graphics.input.CoordinateInputPane.CoordinateInputListener;
import mdp.graphics.input.MainInputPane;
import mdp.graphics.map.MdpMap;
import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;
import mdp.robots.SimulatorRobot;

/**
 * MdpWindowController encapsulates logic required for handling user inputs from
 * MainInputPane and routing control to other controller classes
 *
 * @author Ying Hao
 */
public class MdpWindowController implements CoordinateInputListener, MouseClickListener, ActionListener, RobotActionListener, CellStateUpdateListener {

    /**
     * ExecutionState contains the enumeration of all possible mutually
     * exclusive execution states
     *
     * @author Ying Hao
     */
    private enum ExecutionState {
        EXPLORE, FASTEST_PATH;

        @Override
        public String toString() {
            if (this == EXPLORE) {
                return "Explore";
            } else {
                return "Fastest Path";
            }
        }
    }

    private MdpMap map;
    private MainInputPane inputpane;
    private MapLoader maploader;
    private MapSaver mapsaver;
    private SimulatorRobot srobot;
    private RobotBase probot;
    private ExplorationBase explorer;

    public MdpWindowController(MdpWindow window) {
        this.map = window.getMap();
        this.inputpane = window.getMainInputPane();

        map.addMouseListener(this);
        inputpane.getStartCoordinateInput().addCoordinateInputListener(this);
        inputpane.getEndCoordinateInput().addCoordinateInputListener(this);
        inputpane.getLoadMapButton().addActionListener(this);
        inputpane.getSaveMapButton().addActionListener(this);
        inputpane.getExecutionButton().addActionListener(this);
        inputpane.getCancelButton().addActionListener(this);
        inputpane.getResetButton().addActionListener(this);
    }

    /**
     * Gets the map loader
     *
     * @return
     */
    public MapLoader getMapLoader() {
        return this.maploader;
    }

    /**
     * Sets the map loader
     *
     * @param loader
     */
    public void setMapLoader(MapLoader loader) {
        this.maploader = loader;
    }

    /**
     * Gets the map saver
     *
     * @return
     */
    public MapSaver getMapSaver() {
        return this.mapsaver;
    }

    /**
     * Sets the map saver
     *
     * @param saver
     */
    public void setMapSaver(MapSaver saver) {
        this.mapsaver = saver;
    }

    /**
     * Gets the simulator robot
     *
     * @return
     */
    public SimulatorRobot getSimulatorRobot() {
        return srobot;
    }

    /**
     * Sets the simulator robot
     *
     * @param srobot
     */
    public void setSimulatorRobot(SimulatorRobot srobot) {
        if (this.srobot != null) {
            this.srobot.removeRobotActionListener(this);
        }

        this.srobot = srobot;
        this.srobot.addRobotActionListener(this);
    }

    /**
     * Gets the physical robot
     *
     * @return
     */
    public RobotBase getPhysicalRobot() {
        return probot;
    }

    /**
     * Sets the physical robot
     *
     * @param probot
     */
    public void setPhysicalRobot(RobotBase probot) {
        if (this.probot != null) {
            this.probot.removeRobotActionListener(this);
        }

        this.probot = probot;
        this.probot.addRobotActionListener(this);
    }

    /**
     * Sets the explorer
     *
     * @return
     */
    public ExplorationBase getExplorer() {
        return explorer;
    }

    /**
     * Gets the explorer
     *
     * @param explorer
     */
    public void setExplorer(ExplorationBase explorer) {
        if (this.explorer != null) {
            this.explorer.removeCellStateUpdateListener(this);
        }

        this.explorer = explorer;
        this.explorer.addCellStateUpdateListener(this);
    }

    @Override
    public void onCoordinateInput(CoordinateInputPane source, Point point) {
        CoordinateInputPane sinput = inputpane.getStartCoordinateInput();
        CoordinateInputPane einput = inputpane.getEndCoordinateInput();
        MapState mstate = map.getMapState();

        if (mstate.getRobotCellState(point) == CellState.NORMAL) {
            if (source == sinput) {
                mstate.setRobotPoint(point);
            } else {
                mstate.setEndPoint(point);
            }

            map.repaint();
        } else if (source == sinput) {
            sinput.setCoordinate(mstate.getRobotPoint());
        } else {
            einput.setCoordinate(mstate.getEndPoint());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = map.convertScreenPointToMapPoint(e.getPoint());
        MapInteractionMode mode = inputpane.getMapInteractionModeInput().getSelectedValue();
        MapState mstate = map.getMapState();

        Set<Point> rpoints = new HashSet<>(mstate.convertRobotPointToMapPoints(mstate.getRobotPoint()));
        rpoints.addAll(mstate.convertRobotPointToMapPoints(mstate.getEndPoint()));

        CellState pstate = mstate.getMapCellState(p);
        // Only add obstacle/set waypoint when CellState is normal and
        // is not intersecting current robot or endpoint
        if (pstate == CellState.NORMAL && !rpoints.contains(p)) {
            if (mode == MapInteractionMode.ADD_OBSTACLE) {
                mstate.setMapCellState(p, CellState.OBSTACLE);
            } else if (mode == MapInteractionMode.SET_WAYPOINT) {
                mstate.setMapCellState(p, CellState.WAYPOINT);
            }
        } else if (mode == MapInteractionMode.ADD_OBSTACLE && pstate == CellState.OBSTACLE) {
            mstate.setMapCellState(p, CellState.NORMAL);
        }

        // Updates MDF 2 label
        if (mode == MapInteractionMode.ADD_OBSTACLE) {
            inputpane.sync(mstate);
        }

        map.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == inputpane.getLoadMapButton()) {
            loadmap();
        } else if (e.getSource() == inputpane.getSaveMapButton()) {
            savemap();
        } else if (e.getSource() == inputpane.getExecutionButton()) {
            execute();
        } else if (e.getSource() == inputpane.getCancelButton()) {
            cancel();
        } else if (e.getSource() == inputpane.getResetButton()) {
            cancel();
            map.getMapState().reset();
            map.repaint();
        }

        inputpane.sync(map.getMapState());
    }

    @Override
    public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
        MapState mstate = map.getMapState();
        Point rlocation = mstate.getRobotPoint();

        switch (mapdirection) {
            case UP:
                mstate.setRobotPoint(new Point(rlocation.x, rlocation.y + 1));
                break;
            case DOWN:
                mstate.setRobotPoint(new Point(rlocation.x, rlocation.y - 1));
                break;
            case LEFT:
                mstate.setRobotPoint(new Point(rlocation.x - 1, rlocation.y));
                break;
            default:
                mstate.setRobotPoint(new Point(rlocation.x + 1, rlocation.y));
                break;
        }

        map.repaint();
    }

    @Override
    public void onCellStateUpdate(Point location, CellState state, String label) {
        map.getMapState().setMapCellState(location, state);
        map.setCellLabel(location, label);

        map.repaint();
    }

    private void loadmap() {
        if (this.maploader != null) {
            this.maploader.load(map);
        }
    }

    private void savemap() {
        if (this.mapsaver != null) {
            this.mapsaver.save(map);
        }
    }

    private void execute() {
        JButton executionbtn = inputpane.getExecutionButton();
        ExecutionMode mode = inputpane.getExecutionModeInput().getSelectedValue();
        MapState mstate = map.getMapState();
        boolean explore = executionbtn.getText().equals(ExecutionState.EXPLORE.toString());

        if (explore) {
            // Must call init() before reseting cell states to UNEXPLORED
            if (mode == ExecutionMode.SIMULATION) {
                srobot.init(mstate);
            }

            mstate.setMapCellState(CellState.UNEXPLORED);

            Set<Point> exploredpoints = new HashSet<>(mstate.convertRobotPointToMapPoints(mstate.getRobotPoint()));
            exploredpoints.addAll(mstate.convertRobotPointToMapPoints(mstate.getEndPoint()));

            for (Point p : exploredpoints) {
                mstate.setMapCellState(p, CellState.NORMAL);
            }

            map.repaint();

            if (explorer != null) {
                explorer.explore(mstate.getMapSystemDimension(), mode == ExecutionMode.PHYSICAL ? probot : srobot, mstate.getRobotPoint(), mstate.getEndPoint());
            }
            executionbtn.setText(ExecutionState.FASTEST_PATH.toString());
        } else {
            executionbtn.setText(ExecutionState.EXPLORE.toString());
        }
    }

    private void cancel() {
        this.map.getMapState().setMapCellState(CellState.NORMAL);
        inputpane.getExecutionButton().setText(ExecutionState.EXPLORE.toString());
    }
}
