package ca.team2994.frc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.team2994.frc.autonomous.AutoMode;
import ca.team2994.frc.autonomous.DriveManager;
import ca.team2994.frc.autonomous.Play;

/**
 * A list of all Plays that can be run in an automode
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 *
 */
public class PlayList {

	/**
	 * The list of Plays to choose to run from
	 */
	private List<Play> plays;

	/**
	 * The manager of the drive
	 */
	private DriveManager driveManager;

	/**
	 * Whether to use PID or to use dead reckoning
	 */
	private boolean usePID;

	/**
	 * Initialize PlayList with a list of Plays
	 * 
	 * @param plays
	 *            The list of plays initialize with
	 * @param driver
	 *            The DriveManager of the robot
	 * @param usePID
	 *            Whether to use PID or to dead reckon
	 */
	public PlayList(List<File> plays, DriveManager driver, boolean usePID) {
		this.plays = new ArrayList<Play>();
		this.usePID = usePID;
		driveManager = driver;
		for (File play : plays) {
			try {
				this.plays.add(AutoMode.loadWaypoints(play));
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	/**
	 * Run a play from the play list
	 * 
	 * @param name
	 *            The name of the play to run
	 */
	public void play(String name) {
		for (Play play : plays) {
			if (play.getName() == name) {
				try {
					new AutoMode(name, play.getFileLoc(), driveManager, usePID);
				} catch (IOException e) {
					Utils.logException(Utils.ROBOT_LOGGER, e);
					e.printStackTrace();
				}
			}
		}
	}
}
