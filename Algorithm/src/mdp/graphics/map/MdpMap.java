package mdp.graphics.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import mdp.models.CellState;
import mdp.models.MapState;

/**
 * MdpMap class encapsulates the map graphics user interface<br />
 * 
 * @author Ying Hao
 */
public class MdpMap extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5182200928126366649L;
	private static final Dimension DEFAULT_CELL_SIZE = new Dimension(40, 40);
	
	private MapState mstate;
	private Dimension cellsize;
	private String[][] celllabels;
	
	/**
	 * Creates an instance of MdpMap with the specified map size in rows and columns
	 * 
	 * @param row
	 * @param column
	 */
	public MdpMap(Dimension mapdim, Dimension robotdim) {
		this.cellsize = DEFAULT_CELL_SIZE;
		this.celllabels = new String[mapdim.width][mapdim.height];
		
		this.setBackground(Color.WHITE);
		this.mstate = new MapState(mapdim, robotdim);
	}
	
	/**
	 * Gets the map state
	 * @return
	 */
	public MapState getMapState() {
		return this.mstate;
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
	 * Converts a map coordinate to screen coordinate
	 * @param p
	 * @return
	 */
	public Point convertMapPointToScreenPoint(Point p) {
		return new Point(p.x * (1 + this.cellsize.width) + 1, 
				(this.mstate.getMapSystemDimension().height - p.y - 1) * (1 + this.cellsize.height) + 1);
	}

	/**
	 * Converts a screen coordinate to map coordinate
	 * @param p
	 * @return
	 */
	public Point convertScreenPointToMapPoint(Point p) {
		return new Point((p.x - 1) / (1 + this.cellsize.width), 
				this.mstate.getMapSystemDimension().height - (this.cellsize.height + p.y) / (1 + this.cellsize.height));
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension msystemdim = this.mstate.getMapSystemDimension();
		
		int totalcellW = msystemdim.width * this.cellsize.width;
		int totalcellH = msystemdim.height * this.cellsize.height;
		int totalmarginW = (msystemdim.width + 1) * 1;
		int totalmarginH = (msystemdim.height + 1) * 1;
		
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
		
		Dimension msystemdim = this.mstate.getMapSystemDimension();
		int rowoffset = 1 + this.cellsize.height;
		int coloffset = 1 + this.cellsize.width;
		
		for(int i = 0; i < msystemdim.height + 1; i++)
			g.drawLine(0, i * rowoffset, this.getWidth(), i * rowoffset);
		
		for(int i = 0; i < msystemdim.width + 1; i++)
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
		Dimension msystemdim = this.mstate.getMapSystemDimension();
		
		for(int x = 0; x < msystemdim.width; x++) {
			for(int y = 0; y < msystemdim.height; y++) {
				CellState state = this.mstate.getMapCellState(new Point(x, y));
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
		Dimension robotdim = this.mstate.getRobotDimension();
		
		List<Point> points = this.mstate.convertRobotPointToMapPoints(this.mstate.getRobotPoint());
		Point robotMapPoint = points.get(points.size() / 2);
		Point robotScreenPoint = convertMapPointToScreenPoint(new Point(robotMapPoint.x - (robotdim.width / 2), robotMapPoint.y + (robotdim.height / 2)));
		
		int robotWidth = robotdim.width * (this.cellsize.width + 1) - 2;
		int robotHeight = robotdim.height * (this.cellsize.height + 1) - 2;
		
		g.setColor(new Color(84, 189, 84));
		g.fillOval(robotScreenPoint.x, robotScreenPoint.y, robotWidth, robotHeight);
		
		g.setColor(Color.BLACK);
		FontMetrics metrics = g.getFontMetrics();
		String label = "Robot";
		
		Point labelScreenPoint = convertMapPointToScreenPoint(robotMapPoint);
		g.drawString(label, 
				(int)((this.cellsize.width - metrics.stringWidth(label)) / 2) + labelScreenPoint.x, 
				this.cellsize.height / 2 + labelScreenPoint.y);
	}
	
	private void paintEndLocation(Graphics g) {
		Dimension robotdim = this.mstate.getRobotDimension();
		
		List<Point> points = this.mstate.convertRobotPointToMapPoints(this.mstate.getEndPoint());
		Point endMapPoint = points.get(points.size() / 2);
		Point endScreenPoint = convertMapPointToScreenPoint(new Point(endMapPoint.x - (robotdim.width / 2), endMapPoint.y + (robotdim.height / 2)));
		int robotWidth = robotdim.width * (this.cellsize.width + 1) - 1;
		int robotHeight = robotdim.height * (this.cellsize.height + 1) - 1;
		
		g.setColor(new Color(64, 224, 208));
		g.fillRect(endScreenPoint.x, endScreenPoint.y, robotWidth, robotHeight);
		
		g.setColor(Color.BLACK);
		FontMetrics metrics = g.getFontMetrics();
		String label = "Endpoint";
		
		Point labelScreenPoint = convertMapPointToScreenPoint(endMapPoint);
		g.drawString(label, 
			(int)((this.cellsize.width - metrics.stringWidth(label)) / 2) + labelScreenPoint.x, 
			this.cellsize.height / 2 + labelScreenPoint.y);
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
