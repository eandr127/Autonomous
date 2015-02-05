package ca.team2994.frc.autonomous;

/**
 * A type of waypoint action (rotate)
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 *
 */
public class TurnWaypoint implements Waypoint {

	/**
	 * The angle to turn
	 */
	private int angle;

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
	 * @param angle
	 *            The angle to turn
	 * @param time
	 *            The time of the waypoint
	 * @param manager
	 *            DriveManager to drive with
	 * @param usePID
	 *            Whether to use PID or to dead reckon
	 */
	public TurnWaypoint(int angle, long time, DriveManager manager,
			boolean usePID) {
		this.angle = angle;
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
		manager.driveTurn(angle, usePID);

	}
}
