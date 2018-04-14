package mdp.files;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

/**
 * MdfFileFilter is a FileFilter that accepts MDF 1 and MDF 2 file formats
 * 
 * @author Ying Hao
 */
public class MdfFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		String extension = FilenameUtils.getExtension(f.getAbsolutePath());
		return f.isDirectory() || extension.equalsIgnoreCase("mdf1") || extension.equalsIgnoreCase("mdf2");
	}

	@Override
	public String getDescription() {
		return "Map Descriptor Format";
	}

}
