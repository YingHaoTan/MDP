package mdp.test;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import mdp.graphics.MdpWindow;

public class Test {
	
	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		
		MdpWindow window = new MdpWindow("Mdp Algorithm Simulator", 20, 15);
	}

}
