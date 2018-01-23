package mdp.files;

import javax.swing.JFileChooser;

import mdp.controllers.MapLoader;
import mdp.controllers.MapSaver;
import mdp.graphics.map.MdpMap;

/**
 * MapFileHandler implements MapSaver and MapLoader to save and load from the file system
 * 
 * @author Ying Hao
 */
public class MapFileHandler implements MapSaver, MapLoader {
	private JFileChooser chooser;
	
	public MapFileHandler() {
		chooser = new JFileChooser();
		chooser.setFileFilter(new MdfFileFilter());
	}

	@Override
	public void load(MdpMap map) {
		if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
		}
	}

	@Override
	public void save(MdpMap map) {
		if(chooser.showSaveDialog(null) == JFileChooser.CANCEL_OPTION) {
			
		}
	}

}
