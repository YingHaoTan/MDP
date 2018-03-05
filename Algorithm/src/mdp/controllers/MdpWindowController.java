package mdp.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;

import mdp.controllers.explorer.CellStateUpdateListener;
import mdp.controllers.explorer.ExplorationBase;
import mdp.controllers.explorer.ExplorationCompletedListener;
import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.FastestPathCompletedListener;
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
import mdp.robots.PhysicalRobot;
import mdp.robots.RobotActionListener;
import mdp.robots.RobotBase;
import mdp.robots.SimulatorRobot;

/**
 * MdpWindowController encapsulates logic required for handling user inputs from
 * MainInputPane and routing control to other controller classes
 *
 * @author Ying Hao
 */
public class MdpWindowController implements CoordinateInputListener, MouseClickListener, ActionListener, RobotActionListener, CellStateUpdateListener, ExplorationCompletedListener, FastestPathCompletedListener, ItemListener {

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
	private PhysicalRobot probot;
	private ExplorationBase explorer;
	private FastestPathBase planner;
	private XController xcontroller;

	public MdpWindowController(MdpWindow window) {
		this.map = window.getMap();
		this.inputpane = window.getMainInputPane();

		map.addMouseListener(this);
		inputpane.getStartCoordinateInput().addCoordinateInputListener(this);
		inputpane.getEndCoordinateInput().addCoordinateInputListener(this);
		inputpane.getExecutionModeInput().addItemListener(this);
		inputpane.getLoadMapButton().addActionListener(this);
		inputpane.getSaveMapButton().addActionListener(this);
		inputpane.getExecutionButton().addActionListener(this);
		inputpane.getResetButton().addActionListener(this);
	}

	/**
	 * Gets the map loader
	 * @return
	 */
	public MapLoader getMapLoader() {
		return this.maploader;
	}

	/**
	 * Sets the map loader
	 * @param loader
	 */
	public void setMapLoader(MapLoader loader) {
		this.maploader = loader;
	}

	/**
	 * Gets the map saver
	 * @return
	 */
	public MapSaver getMapSaver() {
		return this.mapsaver;
	}

	/**
	 * Sets the map saver
	 * @param saver
	 */
	public void setMapSaver(MapSaver saver) {
		this.mapsaver = saver;
	}

	/**
	 * Gets the simulator robot
	 * @return
	 */
	public SimulatorRobot getSimulatorRobot() {
		return srobot;
	}

	/**
	 * Sets the simulator robot
	 * @param srobot
	 */
	public void setSimulatorRobot(SimulatorRobot srobot) {
		if (this.srobot != null)
			this.srobot.removeRobotActionListener(this);

		this.srobot = srobot;
		if(inputpane.getExecutionModeInput().getSelectedValue() == ExecutionMode.SIMULATION)
			srobot.addRobotActionListener(this);
	}

	/**
	 * Gets the physical robot
	 * @return
	 */
	public RobotBase getPhysicalRobot() {
		return probot;
	}

	/**
	 * Sets the physical robot
	 * @param probot
	 */

	public void setPhysicalRobot(PhysicalRobot probot) {
		if (this.probot != null) {
			this.probot.removeRobotActionListener(this);
                }

		this.probot = probot;
		if(inputpane.getExecutionModeInput().getSelectedValue() == ExecutionMode.PHYSICAL)
			probot.addRobotActionListener(this);
	}

	/**
	 * Sets the explorer
	 * @return
	 */
	public ExplorationBase getExplorer() {
		return explorer;
	}

	/**
	 * Gets the explorer
	 * @param explorer
	 */
	public void setExplorer(ExplorationBase explorer) {
		if (this.explorer != null) {
			this.explorer.removeCellStateUpdateListener(this);
			this.explorer.removeExplorationCompletedListener(this);
		}

		this.explorer = explorer;
		this.explorer.addCellStateUpdateListener(this);
		this.explorer.addExplorationCompletedListener(this);
	}

	/**
	 * Gets the fastest path planner
	 * @return
	 */
	public FastestPathBase getFastestPathPlanner() {
		return planner;
	}

	/**
	 * Sets the fastest path planner
	 * @param planner
	 */
	public void setFastestPathPlanner(FastestPathBase planner) {
		if(this.planner != null)
			this.planner.removeFastestPathCompletedListener(this);

		this.planner = planner;
		this.planner.addFastestPathCompletedListener(this);
	}
	
	/**
	 * Gets the XController
	 * @return
	 */
	public XController getXController() {
		return xcontroller;
	}

	/**
	 * Sets the XController
	 * @param xcontroller
	 */
	public void setXController(XController xcontroller) {
		this.xcontroller = xcontroller;
	}

	@Override
	public void onCoordinateInput(CoordinateInputPane source, Point point) {
		CoordinateInputPane sinput = inputpane.getStartCoordinateInput();
		MapState mstate = map.getMapState();

		if (mstate.getRobotCellState(point) == CellState.NORMAL) {

			if (source == sinput) {
				mstate.setStartPoint(point);
				mstate.setRobotPoint(point);
			} else {
				mstate.setEndPoint(point);
			}

			map.repaint();
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
			this.maploader.load(map.getMapState());
		} else if (e.getSource() == inputpane.getSaveMapButton()) {
			this.mapsaver.save(map.getMapState());
		} else if(xcontroller != null) { 
			if (e.getSource() == inputpane.getExecutionButton()) {
				execute();
			} else if (e.getSource() == inputpane.getResetButton()) {
				xcontroller.reset(map.getMapState());
				inputpane.getExecutionButton().setText(ExecutionState.EXPLORE.toString());
			}
		}
		
		inputpane.sync(map.getMapState());
		map.repaint();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		RobotBase robot;
		switch((ExecutionMode) e.getItem()) {
			case PHYSICAL:
				robot = probot;
				break;
			default:
				robot = srobot;
		}
		
		if(robot != null) {
			if(e.getStateChange() == ItemEvent.SELECTED)
				robot.addRobotActionListener(this);
			else
				robot.removeRobotActionListener(this);
		}
	}

	@Override
	public void onRobotActionCompleted(Direction mapdirection, RobotAction[] actions) {
		MapState mstate = map.getMapState();
		Point rlocation = mstate.getRobotPoint();

		if(mapdirection != null){
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
			case RIGHT:
				mstate.setRobotPoint(new Point(rlocation.x + 1, rlocation.y));
				break;
			}
		}

		map.repaint();
	}

	@Override
	public void onCellStateUpdate(Point location, CellState state, String label) {
		MapState mstate = map.getMapState();

		mstate.setMapCellState(location, state);
		map.setCellLabel(location, label);

		inputpane.sync(mstate);
		map.repaint();
	}

	@Override
	public void onExplorationComplete() {
		JButton executionbtn = inputpane.getExecutionButton();

		executionbtn.setText(ExecutionState.FASTEST_PATH.toString());
		inputpane.enable();
	}

	@Override
	public void onFastestPathCompleted() {
		JButton executionbtn = inputpane.getExecutionButton();

		executionbtn.setText(ExecutionState.EXPLORE.toString());
		inputpane.enable();
	}
	
	/**
	 * Request synchronization between input pane, map and models
	 */
	public void requestSynchronization() {
		map.repaint();
		inputpane.sync(map.getMapState());
	}
	
	private void execute() {
		JButton executionbtn = inputpane.getExecutionButton();
		boolean explore = executionbtn.getText().equals(ExecutionState.EXPLORE.toString());
		
		srobot.setDelay((long)(inputpane.getDelaySeconds()*1000));
		
		if (explore) {
			int coverage = inputpane.getCoveragePercentage();
			double timelimit = inputpane.getTimeLimit();
			
			xcontroller.explore(inputpane.getExecutionModeInput().getSelectedValue(), coverage, timelimit);
		}
		else {
			xcontroller.fastestpath(inputpane.getExecutionModeInput().getSelectedValue());
		}

		inputpane.disable();
	}

}
