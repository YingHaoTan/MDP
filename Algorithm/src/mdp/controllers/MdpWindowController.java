package mdp.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import mdp.graphics.MdpWindow;
import mdp.graphics.input.CoordinateInputPane;
import mdp.graphics.input.CoordinateInputPane.CoordinateInputListener;
import mdp.graphics.input.MainInputPane;
import mdp.graphics.input.MainInputPane.ExecutionMode;
import mdp.graphics.input.MainInputPane.MapInteractionMode;
import mdp.graphics.map.MdpMap;
import mdp.graphics.map.MdpMap.CellState;

/**
 * MdpWindowController encapsulates logic required for handling user inputs from MainInputPane
 * and routing control to other controller classes
 * 
 * @author Ying Hao
 */
public class MdpWindowController implements CoordinateInputListener, MouseClickListener, ActionListener {
	private interface MapBaseInterface {
		/**
		 * Sets the map
		 * @param map
		 */
		public void setMap(MdpMap map);
	}
	
	/**
	 * MapLoader provides contract method needed for MdpWindowController to delegate
	 * map loading and saving task
	 * 
	 * @author Ying Hao
	 */
	public interface MapLoader extends MapBaseInterface {
		/**
		 * Perform loading of map from the specified file
		 */
		public void load(File file);
		
		/**
		 * Perform saving of map to the specified file
		 */
		public void save(File file);
	}
	
	/**
	 * ExecutionController provides contract method needed for MdpWindowController to delegate
	 * exploration and fastest path task
	 * 
	 * @author Ying Hao
	 */
	public interface ExecutionController extends MapBaseInterface {
		/**
		 * Explores the map
		 * @param map
		 * @param mode
		 */
		public void explore(ExecutionMode mode);
		
		/**
		 * Perform a fastest path route planning from the start point to the end point
		 */
		public void fastestpath();
		
		/**
		 * Cancels the previously executed action
		 * @param map
		 */
		public void cancel();
	}
	
	/**
	 * ExecutionState contains the enumeration of all possible mutually exclusive execution states
	 * @author Ying Hao
	 */
	private enum ExecutionState {
		EXPLORE, FASTEST_PATH;

		@Override
		public String toString() {
			if(this == EXPLORE)
				return "Explore";
			else
				return "Fastest Path";
		}
	}
	
	private JFrame frame;
	private MdpMap map;
	private MainInputPane inputpane;
	private MapLoader maploader;
	private ExecutionController executor;
	
	public MdpWindowController(MdpWindow window) {
		this.frame = window.getFrame();
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
		if(this.maploader != null)
			this.maploader.setMap(null);
		
		this.maploader = loader;
		this.maploader.setMap(map);
	}
	
	/**
	 * Gets the execution controller
	 * @return
	 */
	public ExecutionController getExecutionController() {
		return this.executor;
	}
	
	/**
	 * Sets the execution controller
	 * @param controller
	 */
	public void setExecutionController(ExecutionController controller) {
		if(this.executor != null)
			this.executor.setMap(null);
		
		this.executor = controller;
		this.executor.setMap(map);
	}

	@Override
	public void onCoordinateInput(CoordinateInputPane source, Point point) {
		if(source == inputpane.getStartCoordinateInput())
			map.setRobotLocation(point);
		else
			map.setEndLocation(point);
		
		map.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point p = map.convertScreenPointToMapPoint(e.getPoint());
		MapInteractionMode mode = inputpane.getMapInteractionModeInput().getSelectedValue();
		
		if(mode == MapInteractionMode.ADD_OBSTACLE)
			map.setCellState(p, CellState.OBSTACLE);
		else if(mode == MapInteractionMode.SET_WAYPOINT)
			map.setCellState(p, CellState.WAYPOINT);
		
		map.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == inputpane.getLoadMapButton()) {
			loadmap();
		}
		else if(e.getSource() == inputpane.getSaveMapButton()) {
			savemap();
		}
		else if(e.getSource() == inputpane.getExecutionButton()) {
			execute();
		}
		else if(e.getSource() == inputpane.getCancelButton()) {
			cancel();
		}
		else if(e.getSource() == inputpane.getResetButton()) {
			cancel();
			map.reset();
			inputpane.getStartCoordinateInput().setCoordinate(map.getRobotLocation());
			inputpane.getEndCoordinateInput().setCoordinate(map.getEndLocation());
		}
	}
	
	private void loadmap() {
		JFileChooser chooser = new JFileChooser();
		
		// Stops execution
		if(this.executor != null)
			this.executor.cancel();
		
		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION && this.maploader != null)
			this.maploader.load(chooser.getSelectedFile());
	}
	
	private void savemap() {
		JFileChooser chooser = new JFileChooser();
		
		// Stops execution
		if(this.executor != null)
			this.executor.cancel();
		
		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION && this.maploader != null)
			this.maploader.load(chooser.getSelectedFile());
	}
	
	private void execute() {
		JButton executionbtn = inputpane.getExecutionButton();
		boolean explore = executionbtn.getText().equals(ExecutionState.EXPLORE.toString());
		
		if(this.executor != null) {
			if(explore) {
				this.map.setCellState(CellState.UNEXPLORED);
				this.executor.explore(inputpane.getExecutionModeInput().getSelectedValue());
			}
			else {
				this.executor.fastestpath();
			}
		}
		
		executionbtn.setText(explore? ExecutionState.FASTEST_PATH.toString(): ExecutionState.EXPLORE.toString());
	}
	
	private void cancel() {
		if(this.executor != null)
			this.executor.cancel();
		
		inputpane.getExecutionButton().setText(ExecutionState.EXPLORE.toString());
	}

}
