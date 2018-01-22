package mdp.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import mdp.graphics.MdpWindow;
import mdp.graphics.input.CoordinateInputPane;
import mdp.graphics.input.CoordinateInputPane.CoordinateInputListener;
import mdp.graphics.input.MainInputPane;
import mdp.graphics.input.MainInputPane.MapInteractionMode;
import mdp.graphics.map.MdpMap;
import mdp.graphics.map.MdpMap.CellState;
import mdp.graphics.map.MdpMap.MapDescriptorFormat;

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
		 * Perform saving of map to the specified file using the specified mdf1 and mdf2 values
		 */
		public void save(File file, String mdf1, String mdf2);
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

	@Override
	public void onCoordinateInput(CoordinateInputPane source, Point point) {
		CoordinateInputPane sinput = inputpane.getStartCoordinateInput();
		CoordinateInputPane einput = inputpane.getEndCoordinateInput();
		
		if(map.getCellState(point, false) == CellState.NORMAL) {
			if(source == sinput)
				map.setRobotLocation(point);
			else
				map.setEndLocation(point);
			
			map.repaint();
		}
		else {
			if(source == sinput)
				sinput.setCoordinate(map.getRobotLocation());
			else
				einput.setCoordinate(map.getEndLocation());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point p = map.convertScreenPointToMapPoint(e.getPoint());
		MapInteractionMode mode = inputpane.getMapInteractionModeInput().getSelectedValue();
		
		Set<Point> rpoints = map.convertRobotPointToMapPoints(map.getRobotLocation());
		rpoints.addAll(map.convertRobotPointToMapPoints(map.getEndLocation()));
		
		CellState pstate = map.getCellState(p);
		// Only add obstacle/set waypoint when CellState is normal and
		// is not intersecting current robot or endpoint
		if(pstate == CellState.NORMAL && !rpoints.contains(p)) {
			if(mode == MapInteractionMode.ADD_OBSTACLE)
				map.setCellState(p, CellState.OBSTACLE);
			else if(mode == MapInteractionMode.SET_WAYPOINT)
				map.setCellState(p, CellState.WAYPOINT);
		}
		else if(mode == MapInteractionMode.ADD_OBSTACLE && pstate == CellState.OBSTACLE) {
			map.setCellState(p, CellState.NORMAL);
		}
		
		// Updates MDF 2 label
		if(mode == MapInteractionMode.ADD_OBSTACLE)
			inputpane.getMDF2Label().setText(map.toString(MapDescriptorFormat.MDF2));
		
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
		else {
			if(e.getSource() == inputpane.getExecutionButton()) {
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
			
			inputpane.getMDF1Label().setText(map.toString(MapDescriptorFormat.MDF1));
			inputpane.getMDF2Label().setText(map.toString(MapDescriptorFormat.MDF2));
		}
	}
	
	private void loadmap() {
		JFileChooser chooser = new JFileChooser();
		
		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION && this.maploader != null)
			this.maploader.load(chooser.getSelectedFile());
	}
	
	private void savemap() {
		JFileChooser chooser = new JFileChooser();
		
		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION && this.maploader != null)
			this.maploader.save(chooser.getSelectedFile(), null, null);
	}
	
	private void execute() {
		JButton executionbtn = inputpane.getExecutionButton();
		boolean explore = executionbtn.getText().equals(ExecutionState.EXPLORE.toString());
		
		if(explore) {
			this.map.setCellState(CellState.UNEXPLORED);
			
			Set<Point> exploredpoints = this.map.convertRobotPointToMapPoints(map.getRobotLocation());
			exploredpoints.addAll(this.map.convertRobotPointToMapPoints(map.getEndLocation()));
			
			for(Point p: exploredpoints)
				this.map.setCellState(p, CellState.NORMAL);
			
			executionbtn.setText(ExecutionState.FASTEST_PATH.toString());
		}
		else {
			executionbtn.setText(ExecutionState.EXPLORE.toString());
		}
	}
	
	private void cancel() {
		this.map.setCellState(CellState.NORMAL);
		inputpane.getExecutionButton().setText(ExecutionState.EXPLORE.toString());
	}

}
