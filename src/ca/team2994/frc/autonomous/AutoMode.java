package ca.team2994.frc.autonomous;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import ca.team2994.frc.utils.Utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

/**
 * Manages autonomous mode
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 *
 */
public class AutoMode {

	/**
	 * The DriveManager to drive the robot with
	 */
	private DriveManager drive;

	/**
	 * The file to load waypoints from
	 */
	private String filename;
	
	/**
	 * The name of the autonomous routine
	 */
	@SuppressWarnings("unused")
	private String name;

	/**
	 * A list of all waypoints loaded
	 */
	private List<Waypoint> waypoints;

	/**
	 * The start time of this object
	 */
	public final long startTime = System.currentTimeMillis();

	/**
	 * Initialize a AutoMode object and read from the autonomous waypoints file
	 * specified by filename. After this call if an exception is not thrown the
	 * autonomous mode is ready to be executed.
	 * 
	 * @param name
	 *            The human-readable name of this autonomous mode.
	 * @param filename
	 *            The filename containing waypoints.
	 * @param drive
	 *            The drive manager for the robot.
	 * @throws IOException
	 *             If the waypoints file doesn't exist or cannot be read.
	 */
	public AutoMode(String name, String filename, DriveManager drive)
			throws IOException {
		this.name = name;
		this.filename = filename;
		this.drive = drive;
		this.waypoints = new ArrayList<>();
		this.loadWaypoints();
	}

	/**
	 * Load the waypoints specified in the waypoints file into the ArrayList.
	 * 
	 * @throws IOException
	 *             If the file cannot be read or doesn't exist.
	 */
	private void loadWaypoints() throws IOException {
		// TODO: Read from the autonomous waypoints file (filename)
		// Magic Guava splitting/reading voodoo
		List<String> guavaResult = Files.readLines(new File(filename),
				Charsets.UTF_8);
		Iterable<String> guavaResultFiltered = Iterables.filter(guavaResult,
				Utils.skipComments);
		guavaResultFiltered.forEach(new Consumer<String>() {
			@Override
			public void accept(String line) {
				String[] s = Iterables.toArray(Utils.SPLITTER.split(line),
						String.class);
				/*
				 * s should now contain: 1) The time since the start of
				 * autonomous 2) The type of action (turn or drive) 3) The
				 * parameters for the action
				 */
				String type = s[1];

				if (type.equalsIgnoreCase("turn")) {
					try {
						int angle = (int) Double.parseDouble(s[2]);
						waypoints.add(new TurnWaypoint(angle, Integer
								.parseInt(s[0]), drive));
					} catch (NumberFormatException nef) {
						nef.printStackTrace();
						Utils.logException(Utils.ROBOT_LOGGER, nef);
					}
				} else if (type.equalsIgnoreCase("drive")) {
					try {
						Double distance = Double.parseDouble(s[2]);
						// TODO: Add a class for a normal waypoint (drive
						// straight)
						waypoints.add(new DriveWaypoint(distance, Integer
								.parseInt(s[0]), drive));
					} catch (NumberFormatException nef) {
						nef.printStackTrace();
						Utils.logException(Utils.ROBOT_LOGGER, nef);
					}

				}

			}

		});
		// Contains the split up ones for this line

		Collections.sort(waypoints, WAYPOINT_COMPARER);

		runScheduler();
	}

	/**
	 * A comparator for sorting the list of autonomous commands from oldest to newest
	 */
	private static final Comparator<Waypoint> WAYPOINT_COMPARER = new Comparator<Waypoint>() {

		@Override
		public int compare(Waypoint o1, Waypoint o2) {
			return (int) (o1.getTime() - o2.getTime());
		}

	};

	/**
	 * A scheduler for running the waypoints loaded from a file
	 */
	public void runScheduler() {
		for (Waypoint w : waypoints) {
			long diff = w.getTime() - (System.currentTimeMillis() - startTime);
			try {
				if (!(diff < 0)) {
					Thread.sleep(diff);
				}
			} catch (InterruptedException e) {
				Utils.logException(Utils.ROBOT_LOGGER, e);
				e.printStackTrace();
			}
			w.run();
		}

	}
}
