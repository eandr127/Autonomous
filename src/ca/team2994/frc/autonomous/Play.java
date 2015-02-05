package ca.team2994.frc.autonomous;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ca.team2994.frc.utils.Utils;

/**
 * A list of waypoints that can be loaded into an automode
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 *
 */
public class Play extends ArrayList<Waypoint> {

	/**
	 * ID for serialization
	 */
	private static final long serialVersionUID = -1942032151926094078L;

	/**
	 * The human readable name of the Play
	 */
	private String name;

	/**
	 * The location of the file that contains the play
	 */
	private String fileLoc;

	/**
	 * Create a new play that contains a list of waypoints
	 * 
	 * @param fileLoc
	 *            The location of the file to load
	 */
	public Play(String fileLoc) {
		File f = new File(fileLoc);
		if (f.exists()) {
			fileLoc = f.getPath();
			getName(fileLoc);
		}
	}

	/**
	 * Get the human readable name of the file by reading first line that starts
	 * with the '#' character
	 * 
	 * @param fileLoc
	 *            The location of the file to load
	 */
	private void getName(String fileLoc) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(fileLoc)));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					line = line.replaceFirst("#", "");
					this.name = line;
				}
			}
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				Utils.logException(Utils.ROBOT_LOGGER, e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the human readable name of the file
	 * 
	 * @return The human readable name of the file
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the location of the file that was loaded from
	 * 
	 * @return The location of the file that was loaded from
	 */
	public String getFileLoc() {
		return fileLoc;
	}
}
