package mdp.graphics.input;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * CoordinateInputPane encapsulates input controls necessary for user input of coordinate
 * information
 * 
 * @author Ying Hao
 */
public class CoordinateInputPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3265317092131315585L;
	
	/**
	 * CoordinateInputListener provides contract method for CoordinateInputPanel to notify
	 * whenever user inputs a new coordinate information
	 * 
	 * @author Ying Hao
	 */
	public interface CoordinateInputListener {
		public void onCoordinateInput(CoordinateInputPane source, Point point);
	}
	
	private JComboBox<Integer> xcombobox;
	private JComboBox<Integer> ycombobox;
	private List<CoordinateInputListener> listeners;
	
	public CoordinateInputPane(String label, Dimension dimension) {
		listeners = new ArrayList<CoordinateInputListener>();
		
		xcombobox = new JComboBox<Integer>();
		ycombobox = new JComboBox<Integer>();
		
		for(int x = 0; x <  dimension.width; x++)
			xcombobox.addItem(x);
		for(int y = 0; y < dimension.height; y++)
			ycombobox.addItem(y);
		
		xcombobox.addItemListener((ItemEvent ae) -> notifyCoordinateInput());
		ycombobox.addItemListener((ItemEvent ae) -> notifyCoordinateInput());
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
		this.setLayout(layout);
		
		this.add(new JLabel(label));
		this.add(xcombobox);
		this.add(new JLabel(", "));
		this.add(ycombobox);
	}
	
	/**
	 * Gets the coordinate
	 * @return
	 */
	public Point getCoordinate() {
		return new Point((Integer) xcombobox.getSelectedItem(), (Integer) ycombobox.getSelectedItem());
	}
	
	/**
	 * Sets the coordinate
	 * @param p
	 */
	public void setCoordinate(Point p) {
		this.xcombobox.setSelectedItem(p.x);
		this.ycombobox.setSelectedItem(p.y);
	}
	
	/**
	 * Adds a CoordinateInputListener
	 * @param listener
	 */
	public void addCoordinateInputListener(CoordinateInputListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Removes a CoordinateInputListener
	 * @param listener
	 */
	public void removeCoordinateInputListener(CoordinateInputListener listener) {
		this.listeners.remove(listener);
	}
	
	private void notifyCoordinateInput() {
		for(CoordinateInputListener listener: new ArrayList<>(this.listeners))
			listener.onCoordinateInput(this, new Point((Integer) xcombobox.getSelectedItem(), (Integer) ycombobox.getSelectedItem())); 
	}

}
