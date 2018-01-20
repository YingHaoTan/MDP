package mdp.graphics.input;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import mdp.graphics.map.MdpMap;

/**
 * CoordinateInputPane encapsulates input controls necessary for all user inputs
 * 
 * @author Ying Hao
 */
public class MainInputPane extends JPanel implements CoordinateInputPane.CoordinateInputListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4895645877543278267L;
	
	private MdpMap map;
	private CoordinateInputPane startinput;
	private CoordinateInputPane endinput;
	private JRadioButton addobstaclemode;
	private JRadioButton setwaypointmode;
	private JRadioButton normalmode;
	private ButtonGroup state;
	
	public MainInputPane(MdpMap map) {
		this.map = map;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		this.startinput = new CoordinateInputPane("Start Coordinate(X, Y):", map.getRobotCoordinateBounds(), map.getRobotLocation());
		this.endinput = new CoordinateInputPane("End Coordinate(X, Y):", map.getRobotCoordinateBounds(), map.getEndLocation());
		this.addobstaclemode = new JRadioButton("Add Obstacle Mode");
		this.setwaypointmode = new JRadioButton("Edit Waypoint Mode");
		this.normalmode = new JRadioButton("Normal Mode");
		
		state = new ButtonGroup();
		state.add(addobstaclemode);
		state.add(setwaypointmode);
		state.add(normalmode);
		normalmode.setSelected(true);
		
		this.add(startinput);
		this.add(endinput);
		this.add(normalmode);
		this.add(addobstaclemode);
		this.add(setwaypointmode);
		
		this.startinput.addCoordinateInputListener(this);
		this.endinput.addCoordinateInputListener(this);
	}

	@Override
	public void onCoordinateInput(CoordinateInputPane source, Point point) {
		if(source == this.startinput)
			map.setRobotLocation(point);
		else
			map.setEndLocation(point);
		
		map.repaint();
	}

}
