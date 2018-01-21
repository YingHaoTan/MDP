package mdp.graphics.input;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import mdp.graphics.map.MdpMap;

/**
 * CoordinateInputPane encapsulates input controls necessary for all user inputs
 * 
 * @author Ying Hao
 */
public class MainInputPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4895645877543278267L;
	
	/**
	 * CellState contains the enumeration of all possible mutually exclusive map interaction modes
	 * 
	 * @author Ying Hao
	 */
	private enum MapInteractionMode {
		NONE, ADD_OBSTACLE, SET_WAYPOINT
	}
	
	/**
	 * CellState contains the enumeration of all possible mutually exclusive execution modes
	 * 
	 * @author Ying Hao
	 */
	private enum ExecutionMode {
		SIMULATION, PHYSICAL
	}
	
	private CoordinateInputPane startinput;
	private CoordinateInputPane endinput;
	private ComboBoxInputPane<MapInteractionMode> minteractionmode;
	private ComboBoxInputPane<ExecutionMode> executionmode;
	private JButton loadmapbtn;
	private JButton savemapbtn;
	private JButton executionbtn;
	private JButton cancelbtn;
	private JButton resetbtn;
	
	public MainInputPane(MdpMap map) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		this.startinput = new CoordinateInputPane("Start Coordinate(X, Y):", map.getRobotCoordinateBounds(), map.getRobotLocation());
		this.endinput = new CoordinateInputPane("End Coordinate(X, Y):", map.getRobotCoordinateBounds(), map.getEndLocation());
		this.minteractionmode = new ComboBoxInputPane<>("Map Interaction Mode:", MapInteractionMode.values());
		this.executionmode = new ComboBoxInputPane<>("Execution Mode:", ExecutionMode.values());
		
		JPanel loadsavepane = new JPanel();
		loadsavepane.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.loadmapbtn = new JButton("Load Map");
		this.savemapbtn = new JButton("Save Map");
		loadsavepane.add(loadmapbtn);
		loadsavepane.add(savemapbtn);
		
		JPanel executionpane = new JPanel();
		executionpane.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.executionbtn = new JButton("Explore");
		this.cancelbtn = new JButton("Cancel");
		this.resetbtn = new JButton("Reset");
		executionpane.add(executionbtn);
		executionpane.add(cancelbtn);
		executionpane.add(resetbtn);
		
		this.add(loadsavepane);
		this.add(startinput);
		this.add(endinput);
		this.add(minteractionmode);
		this.add(executionmode);
		this.add(executionpane);
	}
	
	/**
	 * Gets the user interface for start coordinate input
	 * @return
	 */
	public CoordinateInputPane getStartCoordinateInput() {
		return this.startinput;
	}
	
	/**
	 * Gets the user interface for end coordinate input
	 * @return
	 */
	public CoordinateInputPane getEndCoordinateInput() {
		return this.endinput;
	}
	
	/**
	 * Gets the user interface for map interaction mode input
	 * @return
	 */
	public ComboBoxInputPane<MapInteractionMode> getMapInteractionModeInput() {
		return this.minteractionmode;
	}
	
	/**
	 * Gets the user interface for execution mode input
	 * @return
	 */
	public ComboBoxInputPane<ExecutionMode> getExecutionModeInput() {
		return this.executionmode;
	}
	
	/**
	 * Gets the execution button
	 * @return
	 */
	public JButton getExecutionButton() {
		return this.executionbtn;
	}

	@Override
	public BaselineResizeBehavior getBaselineResizeBehavior() {
		return BaselineResizeBehavior.CONSTANT_ASCENT;
	}

	@Override
	public int getBaseline(int width, int height) {
		return 0;
	}

}