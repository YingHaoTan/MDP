package mdp.controllers;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * MouseClickListener is a syntatic sugar class that provides a default implementation which does nothing
 * for all other MouseListener methods and requires classes that implement it to only implement the
 * MouseClick method
 * 
 * @author Ying Hao
 */
public interface MouseClickListener extends MouseListener {

	@Override
	default void mousePressed(MouseEvent e) {
		// Do nothing
	}

	@Override
	default void mouseReleased(MouseEvent e) {
		// Do nothing
	}

	@Override
	default void mouseEntered(MouseEvent e) {
		// Do nothing
	}

	@Override
	default void mouseExited(MouseEvent e) {
		// Do nothing
	}

}
