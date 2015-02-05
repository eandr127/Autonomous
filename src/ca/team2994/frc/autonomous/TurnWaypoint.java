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
	 * 
	 */
	private int angle;
	
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
	 * @param angle
	 * @param time
	 * @param manager
	 */
	public TurnWaypoint(int angle, long time, DriveManager manager, boolean usePID) {
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
