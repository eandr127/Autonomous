package ca.team2994.frc.autonomous;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ca.team2994.frc.utils.Utils;

public class Play extends ArrayList<Waypoint> {

	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = -1942032151926094078L;

	private String name;
	
	private String fileLoc;
	
	public Play(String fileLoc) {
		File f = new File(fileLoc);
		if(f.exists()) {
			fileLoc = f.getPath();
			getName(fileLoc);
		}
	}
	
	private void getName(String fileLoc) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(fileLoc)));
			String line;
			while((line = br.readLine()) != null) {
				if(line.startsWith("#")) {
					line = line.replaceFirst("#", "");
					this.name = line;
				}
			}
		} 
		catch (IOException e) {
			
			e.printStackTrace();
		}
		finally {
			try {
				br.close();
			} catch (IOException e) {
				Utils.logException(Utils.ROBOT_LOGGER, e);
				e.printStackTrace();
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getFileLoc() {
		return fileLoc;
	}
}
