package mdp.controllers;

import mdp.graphics.MdpWindow;

/**
 * MdpWindowController encapsulates logic required for handling user inputs from MainInputPane
 * and routing control to other controller classes
 * 
 * @author Ying Hao
 */
public class MdpWindowController {
	private MdpWindow window;
	
	public MdpWindowController(MdpWindow window) {
		this.window = window;
	}

}
