package ca.team2994.frc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.team2994.frc.autonomous.AutoMode;
import ca.team2994.frc.autonomous.DriveManager;
import ca.team2994.frc.autonomous.Play;

public class PlayList {
	private List<Play> plays;
	private DriveManager driveManager;
	private boolean usePID;
	
	public PlayList(List<File> plays, DriveManager driver, boolean usePID) {
		this.plays = new ArrayList<Play>();
		this.usePID = usePID;
		driveManager = driver;
		for(File play : plays) {
			try {
				this.plays.add(AutoMode.loadWaypoints(play));
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public void play(String name) {
		for(Play play : plays) {
			if(play.getName() == name) {
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
