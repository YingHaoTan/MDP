package mdp.graphics.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * MdpMap class encapsulates the map graphics user interface<br />
 * MdpMap operates in 2 coordinate systems, map coordinate system and robot coordinate system<br /><br />
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
public class MdpMap extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5182200928126366649L;
	private static final Dimension DEFAULT_CELL_SIZE = new Dimension(40, 40);
	private static final Dimension DEFAULT_ROBOT_DIMENSION = new Dimension(3, 3);
	
	/**
	 * CellState contains the enumeration of all possible mutually exclusive cell states
	 * 
	 * @author Ying Hao
	 */
	public enum CellState {
		UNEXPLORED, OBSTACLE, NORMAL, WAYPOINT
	}
	
	private int row;
	private int column;
	private Dimension cellsize;
	private Dimension robotdim;
	private Point robotlocation;
	private Point endlocation;
	private String[][] celllabels;
	private CellState[][] cellstates;
	private Point waypoint;
	
	/**
	 * Creates an instance of MdpMap with the specified map size in rows and columns
	 * 
	 * @param row
	 * @param column
	 */
	public MdpMap(int row, int column) {
		this.row = row;
		this.column = column;
		this.cellsize = DEFAULT_CELL_SIZE;
		this.celllabels = new String[column][row];
		this.cellstates = new CellState[column][row];
		
		this.setBackground(Color.WHITE);
		this.setRobotDimension(DEFAULT_ROBOT_DIMENSION);
		reset();
	}

	/**
	 * Gets the size of each map cell
	 * @return
	 */
	public Dimension getMapCellSize() {
		return this.cellsize;
	}
	
	/**
	 * Sets the size of each map cell
	 * @param dimension
	 */
	public void setMapCellSize(Dimension dimension) {
		this.cellsize = dimension;
	}
	
	/**
	 * Gets the robot dimension in map coordinate
	 * @return
	 */
	public Dimension getRobotDimension() {
		return this.robotdim;
	}
	
	/**
	 * Sets the robot dimension in map coordinate
	 * @param dim
	 */
	public void setRobotDimension(Dimension dim) {
		this.robotdim = dim;
	}
	
	/**
	 * Gets the end location with reference to robot coordinates
	 * @return
	 */
	public Point getEndLocation() {
		return this.endlocation;
	}
	
	/**
	 * Gets the end location with reference to robot coordinates
	 * @param
	 */
	public void setEndLocation(Point location) {
		if(this.endlocation != null)
			setCellLabel(getEndMapPoint(), null);
		
		this.endlocation = location;
		setCellLabel(getEndMapPoint(), "Endpoint");
	}
	
	/**
	 * Gets the location of robot with reference to robot coordinates
	 * @return
	 */
	public Point getRobotLocation() {
		return this.robotlocation;
	}
	
	/**
	 * Sets the location of robot with reference to robot coordinates
	 * @param location
	 */
	public void setRobotLocation(Point location) {
		if(this.robotlocation != null)
			setCellLabel(getRobotMapPoint(), null);
		
		this.robotlocation = location;
		setCellLabel(getRobotMapPoint(), "Robot");
	}
	
	/**
	 * Gets the cell label at the specified location
	 * @param location
	 * @return
	 */
	public String getCellLabel(Point location) {
		return this.celllabels[location.x][location.y];
	}
	
	/**
	 * Sets the cell label at the specified location
	 * @param location
	 * @param label
	 */
	public void setCellLabel(Point location, String label) {
		this.celllabels[location.x][location.y] = label;
	}
	
	/**
	 * Gets the cell state at the specified location
	 * @param location
	 * @return
	 */
	public CellState getCellState(Point location) {
		return this.cellstates[location.x][location.y];
	}
	
	/**
	 * Sets the cell state at the specified location
	 * @param location
	 * @param value
	 */
	public void setCellState(Point location, CellState state) {
		this.cellstates[location.x][location.y] = state;
		
		if(state == CellState.WAYPOINT) {
			// Clears previous waypoint
			if(this.waypoint != null && this.waypoint != location)
				this.setCellState(waypoint, CellState.NORMAL);
			
			// Set waypoint value
			this.waypoint = location;
		}
	}
	
	/**
	 * Gets the robot center location in map coordinate
	 * @return
	 */
	public Point getRobotMapPoint() {
		return new Point(robotlocation.x + (robotdim.width / 2), robotlocation.y + (robotdim.height / 2));
	}
	
	/**
	 * Gets the end center location in map coordinate
	 * @return
	 */
	public Point getEndMapPoint() {
		return new Point(endlocation.x + (robotdim.width / 2), endlocation.y + (robotdim.height / 2));
	}
	
	/**
	 * Sets the cell state for all cells
	 * @param state
	 */
	public void setCellState(CellState state) {
		for(int x = 0; x < this.cellstates.length; x++) {
			for(int y = 0; y < this.cellstates[x].length; y++) {
				this.setCellState(new Point(x, y), state);
			}
		}
		
		this.repaint();
	}
	
	/**
	 * Converts a map coordinate to screen coordinate
	 * @param p
	 * @return
	 */
	public Point convertMapPointToScreenPoint(Point p) {
		return new Point(p.x * (1 + this.cellsize.width) + 1, 
				(this.row - p.y - 1) * (1 + this.cellsize.height) + 1);
	}
	
	/**
	 * Converts a screen coordinate to map coordinate
	 * @param p
	 * @return
	 */
	public Point convertScreenPointToMapPoint(Point p) {
		return new Point((p.x - 1) / (1 + this.cellsize.width), 
				this.row - (this.cellsize.height + p.y) / (1 + this.cellsize.height));
	}
	
	/**
	 * Converts a robot point in robot coordinate to a list of matching map point in map coordinate
	 * @param p
	 * @return
	 */
	public Set<Point> convertRobotPointToMapPoints(Point p) {
		Set<Point> points = new HashSet<>();
		
		for(int x = 0; x < robotdim.width; x++)
			for(int y = 0; y < robotdim.height; y++)
				points.add(new Point(p.x + x, p.y + y));
	
		return points;
	}
	
	/**
	 * Gets the bounding rectangle of the robot coordinates
	 * @return
	 */
	public Rectangle getRobotCoordinateBounds() {
		return new Rectangle(0, 0, this.column - this.robotdim.width + 1, this.row - this.robotdim.height + 1);
	}
	
	/**
	 * Resets this map by removing all cell labels, obstacles, way points, unexplored states
	 * @param Indicates if start point and end points must be reset
	 */
	public void reset() {
		Rectangle rbound = this.getRobotCoordinateBounds();
		
		for(String[] rowlabels: this.celllabels)
			Arrays.fill(rowlabels, null);
		for(CellState[] rowStates: this.cellstates)
			Arrays.fill(rowStates, CellState.NORMAL);
		
		this.setRobotLocation(new Point(0, 0));
		this.setEndLocation(new Point(rbound.width - 1, rbound.height - 1));
		this.repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		int totalcellW = this.column * this.cellsize.width;
		int totalcellH = this.row * this.cellsize.height;
		int totalmarginW = (this.column + 1) * 1;
		int totalmarginH = (this.row + 1) * 1;
		
		return new Dimension(totalcellW + totalmarginW, totalcellH + totalmarginH);
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getSize();
	}
	
	@Override
	public BaselineResizeBehavior getBaselineResizeBehavior() {
		return BaselineResizeBehavior.CONSTANT_ASCENT;
	}

	@Override
	public int getBaseline(int width, int height) {
		return 0;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int rowoffset = 1 + this.cellsize.height;
		int coloffset = 1 + this.cellsize.width;
		
		for(int i = 0; i < this.row + 1; i++)
			g.drawLine(0, i * rowoffset, this.getWidth(), i * rowoffset);
		
		for(int i = 0; i < this.column + 1; i++)
			g.drawLine(i * coloffset, 0, i * coloffset, this.getHeight());
	}
	
	@Override
	protected void paintChildren(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		
	    paintCellState(g);
		paintRobot(g);
		paintEndLocation(g);
		paintLabel(g);
	}
	
	private void paintCellState(Graphics g) {
		for(int x = 0; x < this.cellstates.length; x++) {
			for(int y = 0; y < this.cellstates[x].length; y++) {
				CellState state = this.cellstates[x][y];
				Point point = convertMapPointToScreenPoint(new Point(x, y));
				
				if(state != CellState.NORMAL) {
					if(state == CellState.WAYPOINT) {
						try {
							// Attempt to draw image
							Image image = ImageIO.read(ClassLoader.getSystemResource("map-marker.jpg"));
							g.drawImage(image, point.x, point.y, 
									point.x + this.cellsize.width, 
									point.y + this.cellsize.height,
									0, 0, image.getWidth(null), image.getHeight(null), null);
						} catch (IOException | IllegalArgumentException e) {
							// Fallback to draw yellow rectangle for waypoint if image loading failed
							g.setColor(Color.YELLOW);
							g.fillRect(point.x, point.y, this.cellsize.width, this.cellsize.height);
						}
					}
					else {
						if(state == CellState.UNEXPLORED)
							g.setColor(Color.LIGHT_GRAY);
						else
							g.setColor(Color.BLACK);
						
						g.fillRect(point.x, point.y, this.cellsize.width, this.cellsize.height);
					}
				}
			}
		}
	}
	
	private void paintRobot(Graphics g) {
		Point robotMapPoint = getRobotMapPoint();
		Point robotScreenPoint = convertMapPointToScreenPoint(new Point(robotMapPoint.x - (robotdim.width / 2), robotMapPoint.y + (robotdim.height / 2)));
		int robotWidth = robotdim.width * (this.cellsize.width + 1) - 2;
		int robotHeight = robotdim.height * (this.cellsize.height + 1) - 2;
		
		g.setColor(new Color(84, 189, 84));
		g.fillOval(robotScreenPoint.x, robotScreenPoint.y, robotWidth, robotHeight);
	}
	
	private void paintEndLocation(Graphics g) {
		Point endMapPoint = getEndMapPoint();
		Point endScreenPoint = convertMapPointToScreenPoint(new Point(endMapPoint.x - (robotdim.width / 2), endMapPoint.y + (robotdim.height / 2)));
		int robotWidth = robotdim.width * (this.cellsize.width + 1) - 1;
		int robotHeight = robotdim.height * (this.cellsize.height + 1) - 1;
		
		g.setColor(new Color(64, 224, 208));
		g.fillRect(endScreenPoint.x, endScreenPoint.y, robotWidth, robotHeight);
	}
	
	private void paintLabel(Graphics g) {
		g.setColor(Color.BLACK);
		
		for(int x = 0; x < this.celllabels.length; x++) {
			for(int y = 0; y < this.celllabels[x].length; y++) {
				String label = this.celllabels[x][y];
				
				Point labelPoint = convertMapPointToScreenPoint(new Point(x, y));
				FontMetrics metrics = g.getFontMetrics();
				
				if(label != null) {
					g.drawString(label, 
							(int)((this.cellsize.width - metrics.stringWidth(label)) / 2) + labelPoint.x, 
							this.cellsize.height / 2 + labelPoint.y);
				}
			}
		}
	}

}
