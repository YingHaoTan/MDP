package mdp.graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mdp.graphics.map.MdpMap;
import mdp.graphics.map.MdpMap.CellState;

/**
 * MdpWindow class encapsulates the main graphics user interface and application wide context
 * 
 * @author Ying Hao
 */
public class MdpWindow {
	private JFrame frame;
	private JPanel maincontent;
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
		map.setCellState(new java.awt.Point(5, 5), CellState.WAYPOINT);
		
		maincontent.add(map);
		
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

}
