package mdp.graphics.input;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import mdp.graphics.ExecutionMode;
import mdp.graphics.MapInteractionMode;
import mdp.graphics.map.MdpMap;
import mdp.models.MapDescriptorFormat;
import mdp.models.MapState;

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
	
	private CoordinateInputPane startinput;
	private CoordinateInputPane endinput;
	private ComboBoxInputPane<MapInteractionMode> minteractionmode;
	private ComboBoxInputPane<ExecutionMode> executionmode;
	private JTextArea mdf1;
	private JTextArea mdf2;
	private JButton loadmapbtn;
	private JButton savemapbtn;
	private JButton executionbtn;
	private JButton cancelbtn;
	private JButton resetbtn;
	
	public MainInputPane(MdpMap map) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		MapState mstate = map.getMapState();
		this.startinput = new CoordinateInputPane("Start Coordinate(X, Y):", mstate.getRobotSystemDimension());
		this.endinput = new CoordinateInputPane("End Coordinate(X, Y):", mstate.getRobotSystemDimension());
		this.minteractionmode = new ComboBoxInputPane<>("Map Interaction Mode:", MapInteractionMode.values());
		this.executionmode = new ComboBoxInputPane<>("Execution Mode:", ExecutionMode.values());
		
		JPanel mdf1panel = new JPanel();
		mdf1panel.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.mdf1 = new JTextArea();
		this.mdf1.setPreferredSize(new Dimension(300, 50));
		this.mdf1.setLineWrap(true);
		this.mdf1.setEditable(false);
		mdf1panel.add(new JLabel("MDF 1:"));
		mdf1panel.add(mdf1);
		
		JPanel mdf2panel = new JPanel();
		mdf2panel.setLayout(new FlowLayout(FlowLayout.LEADING));
		this.mdf2 = new JTextArea();
		this.mdf2.setPreferredSize(new Dimension(300, 50));
		this.mdf2.setLineWrap(true);
		this.mdf2.setEditable(false);
		mdf2panel.add(new JLabel("MDF 2:"));
		mdf2panel.add(mdf2);
		
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
		this.add(mdf1panel);
		this.add(mdf2panel);
		this.add(executionpane);
		
		this.sync(mstate);
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
	 * Gets the MDF 1 label
	 * @return
	 */
	public JTextArea getMDF1Label() {
		return this.mdf1;
	}
	
	/**
	 * Gets the MDF 2 label
	 * @return
	 */
	public JTextArea getMDF2Label() {
		return this.mdf2;
	}
	
	/**
	 * Gets the load map button
	 * @return
	 */
	public JButton getLoadMapButton() {
		return this.loadmapbtn;
	}
	
	/**
	 * Gets the save map button
	 * @return
	 */
	public JButton getSaveMapButton() {
		return this.savemapbtn;
	}
	
	/**
	 * Gets the execution button
	 * @return
	 */
	public JButton getExecutionButton() {
		return this.executionbtn;
	}
	
	/**
	 * Gets the cancel button
	 * @return
	 */
	public JButton getCancelButton() {
		return this.cancelbtn;
	}
	
	/**
	 * Gets the reset button
	 * @return
	 */
	public JButton getResetButton() {
		return this.resetbtn;
	}
	
	/**
	 * Syncrhonizes the current MainInputPane displays with the provided map state instance
	 * @param map
	 */
	public void sync(MapState mstate) {
		this.startinput.setCoordinate(mstate.getRobotPoint());
		this.endinput.setCoordinate(mstate.getEndPoint());
		this.mdf1.setText(mstate.toString(MapDescriptorFormat.MDF1));
		this.mdf2.setText(mstate.toString(MapDescriptorFormat.MDF2));
	}
	
	/**
	 * Enables input pane
	 */
	public void enable() {
		executionbtn.setEnabled(true);
		cancelbtn.setEnabled(true);
		resetbtn.setEnabled(true);
	}
	
	/**
	 * Disables input pane
	 */
	public void disable() {
		executionbtn.setEnabled(false);
		cancelbtn.setEnabled(false);
		resetbtn.setEnabled(false);
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