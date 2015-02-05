package ca.team2994.frc.autonomous;

/**
 * A type of waypoint action (drive straight)
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 * 
 */
public class DriveWaypoint implements Waypoint {

	/**
	 * The distance to drive
	 */
	private double distance;

	/**
	 * The time of the waypoint
	 */
	private long time;

	/**
	 * DriveManager to drive with
	 */
	private DriveManager manager;

	/**
	 * Whether to use PID or to dead reckon
	 */
	private boolean usePID;

	/**
	 * A waypoint recorded in a file, of type drive
	 * 
	 * @param distance
	 *            The distance to drive
	 * @param time
	 *            The time of the waypoint
	 * @param manager
	 *            DriveManager to drive with
	 * @param usePID
	 *            Whether to use PID or to dead reckon
	 */
	public DriveWaypoint(double distance, long time, DriveManager manager,
			boolean usePID) {
		this.distance = distance;
		this.time = time;
		this.manager = manager;
		this.usePID = usePID;
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public void run() {
		manager.driveStraight(distance, usePID);
	}
}
