package mdp.graphics.input;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
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
	
	public CoordinateInputPane(String label, Rectangle bounds) {
		this(label, bounds, new Point(0, 0));
	}
	
	public CoordinateInputPane(String label, Rectangle bounds, Point initial) {
		listeners = new ArrayList<CoordinateInputListener>();
		
		xcombobox = new JComboBox<Integer>();
		ycombobox = new JComboBox<Integer>();
		
		for(int x = bounds.x; x < bounds.x + bounds.width; x++)
			xcombobox.addItem(x);
		for(int y = bounds.y; y < bounds.y + bounds.height; y++)
			ycombobox.addItem(y);
		
		xcombobox.setSelectedItem(initial.x);
		ycombobox.setSelectedIndex(initial.y);
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
		for(CoordinateInputListener listener: this.listeners)
			listener.onCoordinateInput(this, new Point((Integer) xcombobox.getSelectedItem(), (Integer) ycombobox.getSelectedItem())); 
	}

}
