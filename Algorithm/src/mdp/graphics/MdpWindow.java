package mdp.graphics;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mdp.graphics.input.MainInputPane;
import mdp.graphics.map.MdpMap;

/**
 * MdpWindow class encapsulates the main graphics user interface and application wide context
 * 
 * @author Ying Hao
 */
public class MdpWindow {
	private JFrame frame;
	private JPanel maincontent;
	private MainInputPane inputpane;
	private MdpMap map;
	
	/**
	 * Creates an instance of a MdpWindow with the specified window title and map size
	 * 
	 * @param windowTitle
	 * @param noOfRows
	 * @param noOfColumns
	 */
	public MdpWindow(String windowTitle, int noOfRows, int noOfColumns) {
		frame = new JFrame();
		maincontent = new JPanel();
		map = new MdpMap(noOfRows, noOfColumns);
		inputpane = new MainInputPane(map);
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
		layout.setAlignOnBaseline(true);
		
		maincontent.setLayout(layout);
		maincontent.add(map);
		maincontent.add(inputpane);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(windowTitle);
		frame.setContentPane(maincontent);
		frame.setResizable(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
	
	/**
	 * Gets the map user interface
	 * @return
	 */
	public MdpMap getMap() {
		return this.map;
	}
	
	/**
	 * Gets the input user interface
	 * @return
	 */
	public MainInputPane getMainInputPane() {
		return this.inputpane;
	}

}
