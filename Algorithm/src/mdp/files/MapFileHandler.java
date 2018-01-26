package mdp.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import mdp.controllers.MapLoader;
import mdp.controllers.MapSaver;
import mdp.graphics.map.MdpMap;
import mdp.models.MapDescriptorFormat;

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
			String filesubpath = chooser.getSelectedFile().getAbsolutePath();
			filesubpath = filesubpath.substring(0, filesubpath.lastIndexOf("."));
			
			String mdf1path = filesubpath + ".mdf1";
			String mdf2path = filesubpath + ".mdf2";
			
			String mdf1, mdf2;
			try {
				try(BufferedReader reader = new BufferedReader(new FileReader(new File(mdf1path)))) {
					mdf1 = reader.readLine();
				}
				
				try(BufferedReader reader = new BufferedReader(new FileReader(new File(mdf2path)))) {
					mdf2 = reader.readLine();
				}
				
				map.getMapState().parseString(mdf1, mdf2);
				map.repaint();
			} catch(IOException e) {
				// Handle case where an error occurs during file reading
			}
		}
	}

	@Override
	public void save(MdpMap map) {
		if(chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			String filesubpath = chooser.getSelectedFile().getAbsolutePath();
			
			try {
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filesubpath + ".mdf1")))) {
					writer.write(map.getMapState().toString(MapDescriptorFormat.MDF1));
				}
				
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filesubpath + ".mdf2")))) {
					writer.write(map.getMapState().toString(MapDescriptorFormat.MDF2));
				}
			} catch(IOException e) {
				// Handle case where an error occurs during file writing
			}
		}
	}

}
