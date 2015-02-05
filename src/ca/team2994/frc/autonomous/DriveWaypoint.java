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
	 * 
	 */
	private double distance;
	
	/**
	 * 
	 */
	private long time;
	
	/**
	 * 
	 */
	private DriveManager manager;
	
	private boolean usePID;
	
	/**
	 * 
	 * @param distance
	 * @param time
	 * @param manager
	 */
	public DriveWaypoint(double distance, long time, DriveManager manager, boolean usePID) {
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
