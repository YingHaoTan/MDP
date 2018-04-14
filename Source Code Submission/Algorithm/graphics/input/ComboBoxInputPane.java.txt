package mdp.graphics.input;

import java.awt.FlowLayout;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ComboBoxInputPane encapsulates input controls necessary for user input for a selection
 * of an item amongst a finite predefined set of items
 * 
 * @author Ying Hao
 */
public class ComboBoxInputPane<T> extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2521453088191527189L;
	private JComboBox<T> combobox;
	
	public ComboBoxInputPane(String label, T[] items) {
		combobox = new JComboBox<T>(items);
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
		this.setLayout(layout);
		
		this.add(new JLabel(label));
		this.add(combobox);
	}
	
	/**
	 * Gets the selected value
	 * @return
	 */
	public T getSelectedValue() {
		return (T) combobox.getSelectedItem();
	}
	
	/**
	 * Adds an ItemListener
	 *
	 * @param listener
	 */
	public void addItemListener(ItemListener listener) {
		combobox.addItemListener(listener);
	}
	
	/**
	 * Removes an ItemListener
	 * @param listener
	 */
	public void removeItemListener(ItemListener listener) {
		combobox.removeItemListener(listener);
	}

}
