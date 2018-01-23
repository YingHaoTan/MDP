package mdp.test;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import mdp.controllers.MdpWindowController;
import mdp.files.MapFileHandler;
import mdp.graphics.MdpWindow;

public class Program {
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
	    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	        if ("Nimbus".equals(info.getName())) {
	            UIManager.setLookAndFeel(info.getClassName());
	            break;
	        }
	    }
		
		MdpWindow window = new MdpWindow("Mdp Algorithm Simulator", 20, 15);
		MdpWindowController controller = new MdpWindowController(window);
		
		MapFileHandler filehandler = new MapFileHandler();
		controller.setMapLoader(filehandler);
		controller.setMapSaver(filehandler);
	}

}
