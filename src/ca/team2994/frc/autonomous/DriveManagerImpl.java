package ca.team2994.frc.autonomous;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.team2994.frc.utils.ButtonEntry;
import ca.team2994.frc.utils.ConfigFile;
import ca.team2994.frc.utils.EJoystick;
import ca.team2994.frc.utils.PlayList;
import ca.team2994.frc.utils.SimGyro;
import ca.team2994.frc.utils.SimLib;
import ca.team2994.frc.utils.SimPID;
import ca.team2994.frc.utils.Utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotDrive;

/**
 * Manages the driving of the robot
 * 
 * @author <a href="https://github.com/eandr127">eandr127</a>
 * @author <a href="https://github.com/JackMc">JackMc</a>
 * 
 */
public class DriveManagerImpl implements DriveManager {

	/**
	 * Robot - used to bail out if disable
	 */
	private RobotBase robot;

	/**
	 * SimPID to oscillate using encoders around target
	 */
	private SimPID encoderPID;

	/**
	 * SimPID to oscillate using gyro around target
	 */
	private SimPID gyroPID;

	/**
	 * A gryo to measure angles with
	 */
	private SimGyro gyro;

	/**
	 * The right encoder to measure wheel turn distance
	 */
	private Encoder rightEncoder;

	/**
	 * The left encoder to measure wheel turn distance
	 */
	private Encoder leftEncoder;

	/**
	 * The joystick to move the robot with
	 */
	private EJoystick stick;

	/**
	 * The robotDrive to move the robot with
	 */
	private RobotDrive drive;

	/**
	 * Manages the calibration of the encoders of the robot
	 */
	private CalibrationManager calibration;

	/**
	 * The properties file to read values from
	 */
	private ConfigFile conf;

	/**
	 * In tele-op, whether to log waypoints or use normal tele-op
	 */
	private boolean saveWaypoints = true;

	/**
	 * Whether in autonomous, to use PID or dead reckoning to move with specific
	 * distances
	 */
	private boolean usePID;

	/**
	 * Initializes a DriveManagerImpl. This class is an implementation of the
	 * interface DriveManager and can be used to drive the robot via turning or
	 * driving straight. If a certain piece of hardware is unavailable, the
	 * correct action is to pass in its associated object as null, and an
	 * exception will be thrown if the associated functionality is requested.
	 * 
	 * In the case of encoders, passing in one as null will cause the other to
	 * be used as the sole encoder. Passing in both as null will indicate that
	 * there is no encoder functionality on the robot (i.e. the robot cannot
	 * drive straight reliably).
	 * 
	 * @param drive
	 *            The RobotDrive object associated with the drive train.
	 * @param base
	 *            An object representing the running robot code and its state.
	 *            Used to check if movement loops should bail out.
	 * @param gyro
	 *            The gryoscope on the robot used for turning.
	 * @param leftEncoder
	 *            The encoder on the "left" side of the robot. Passing this in
	 *            as null will cause the other (right) encoder to be assumed as
	 *            the only one.
	 * @param rightEncoder
	 *            The encoder on the "right" side of the robot. Passing this in
	 *            as null will cause the other (left) encoder to be assumed as
	 *            the only one.
	 * 
	 * @param stick
	 *            The joystick to take user input with
	 */
	public DriveManagerImpl(RobotDrive drive, RobotBase base, SimGyro gyro,
			Encoder leftEncoder, Encoder rightEncoder, EJoystick stick) {
		this.drive = drive;
		this.robot = base;
		this.gyro = gyro;
		this.leftEncoder = leftEncoder;
		this.rightEncoder = rightEncoder;
		this.stick = stick;

		this.conf = new ConfigFile(ConfigFile.DEFAULT_CONFIGURATION_FILE);
		saveWaypoints = this.conf.getPropertyAsBoolean("doRobotLogging");

		// Read encoder values from a file.
		readEncoderValues();
		readPIDValues();
		// TODO: Read these in from the Constants file or the SmartDashboard
		// Rationale: The D stops it from thrasing, P is taken from Simbotics
		// Rationale: P is taken from Simbotics.
		double[] encoderPIDVals = conf.getPropertyAsDoubleArray("gyroPID");

		this.encoderPID = new SimPID(encoderPIDVals[0], encoderPIDVals[1],
				encoderPIDVals[2], encoderPIDVals[3]);

		// Initialize the Calibration instance
		this.calibration = new CalibrationManager(stick, drive, base);
	}

	/**
	 * Read in the encoder values from the autonomous config file. TODO:
	 * Integrate this with Georges' Constants class.
	 */
	private void readEncoderValues() {
		try {

			List<String> guavaResult = Files.readLines(new File(
					Utils.CALIBRATION_OUTPUT_FILE_LOC), Charsets.UTF_8);
			Iterable<String> guavaResultFiltered = Iterables.filter(
					guavaResult, Utils.skipComments);

			String[] s = Iterables
					.toArray(Utils.SPLITTER.split(guavaResultFiltered
							.iterator().next()), String.class);

			double encoderAConst = Double.parseDouble(s[0]);
			double encoderBConst = Double.parseDouble(s[1]);

			leftEncoder.setDistancePerPulse(encoderAConst);
			rightEncoder.setDistancePerPulse(encoderBConst);

		} catch (IOException e) {
			Utils.logException(Utils.ROBOT_LOGGER, e);
			leftEncoder.setDistancePerPulse(1);
			rightEncoder.setDistancePerPulse(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#readPIDValues()
	 */
	public void readPIDValues() {
		double[] gyroPIDVals = conf.getPropertyAsDoubleArray("gyroPID");

		this.gyroPID = new SimPID(gyroPIDVals[0], gyroPIDVals[1],
				gyroPIDVals[2], gyroPIDVals[3]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#driveStraight(int)
	 */
	@Override
	public void driveStraight(double units, boolean usePID) {
		if (!usePID) {
			deadReckonDrive(units);
			return;
		}

		// Reset the encoders (encoder.get(Distance|)() == 0)
		leftEncoder.reset();
		rightEncoder.reset();
		// Set up the desired number of units.
		encoderPID.setDesiredValue(units);
		// Reset the encoder PID to a reasonable state.
		encoderPID.resetErrorSum();
		encoderPID.resetPreviousVal();
		// Used to make sure that the PID doesn't bail out as done
		// right away (we know both the distances are zero from the
		// above reset).
		encoderPID.calcPID(0);

		// The first conditional here checks if the PID is done, pretty simple.
		// The second conditional is there to make sure that we bail if the
		// robot isn't enabled.
		while (!encoderPID.isDone() && robot.isEnabled()
				&& robot.isAutonomous()) {
			double driveVal = encoderPID
					.calcPID((leftEncoder.getDistance() + rightEncoder
							.getDistance()) / 2.0);
			// TODO: Read this from the constants file as "encoderPIDMax"
			double limitVal = SimLib.limitValue(driveVal,
					conf.getPropertyAsDouble("driveSpeed", 0.25));

			drive.setLeftRightMotorOutputs(limitVal + 0.0038, limitVal);
		}

		// Reset the motors (safety and sanity for if we bail out on a
		// none-isDone()
		// condition).
		drive.setLeftRightMotorOutputs(0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#driveTurn(int)
	 */
	@Override
	public void driveTurn(int degrees, boolean usePID) {
		if (!usePID) {
			deadReckonTurn(degrees);
			return;
		}

		gyroPID.setDesiredValue(degrees);
		gyro.reset(0);
		// Reset the gyro PID to a reasonable state.
		encoderPID.resetErrorSum();
		encoderPID.resetPreviousVal();
		// Used to make sure that the PID doesn't bail out as done
		// right away (we know the gyro angle is zero from the above
		// reset).
		gyroPID.calcPID(0);

		while (!gyroPID.isDone() && robot.isEnabled() && robot.isAutonomous()) {
			System.out.println("gyro.getAngle() = " + gyro.getAngle());
			double driveVal = gyroPID.calcPID(-gyro.getAngle());
			// TODO: Read this from the constants file as "gyroPIDMax"
			double limitVal = SimLib.limitValue(driveVal,
					conf.getPropertyAsDouble("turnSpeed", 0.25));
			System.out.println("limitVal = " + limitVal);
			drive.setLeftRightMotorOutputs(limitVal, -limitVal);
		}

		// Reset the motors (safety and sanity for if we bail out on a
		// none-isDone()
		// condition).
		drive.drive(0.0, 0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#arcadeDrive()
	 */
	public void arcadeDrive() {
		// TODO: Change this for competition robot??
		drive.arcadeDrive(-stick.getY(), -stick.getX()); // drive with arcade
															// style (use right
															// stick) (inverted)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#runCalibration()
	 */
	public void runCalibration() {
		calibration.calibrateEncoders(leftEncoder, rightEncoder, stick);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#runAutonomous
	 */
	public void runAutonomous() {
		File f = new File(conf.getProperty("playDir"));
		File[] files = f.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".auto");
			}
		});

		List<File> listOfFiles = new ArrayList<File>();

		for (File file : files) {
			listOfFiles.add(file);
		}

		PlayList plays = new PlayList(listOfFiles, this, usePID);

		plays.play(conf.getProperty("playToUse"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#runTeleOpLogging
	 */
	public void runTeleOPLogging() {
		File log = new File(conf.getProperty("outputPlayFile"));
		if (log.exists()) {
			log.delete();
		}

		long time = System.currentTimeMillis();
		boolean isFirst = true;
		while (robot.isOperatorControl()) {
			long temp = 0;
			if ((temp = doLogging(time, isFirst, log)) != 0) {
				time = temp;
				isFirst = false;
			}
		}
		drive.drive(0, 0);
	}

	/**
	 * Save waypoints for re-running in autonomous
	 * 
	 * @param startTime
	 *            The start of the tele-op period
	 * @param isFirst
	 *            Whether it is the first time the method has been called
	 * @param f
	 *            The file to output to
	 * @return The new start time (so the start time will be the first waypoint
	 *         log time)
	 */
	private long doLogging(long startTime, boolean isFirst, File f) {
		resetMeasurements();

		stick.enableButton(1);
		stick.enableButton(2);
		// boolean wasLastTurn = false;
		int i = 0;
		int j = 0;
		while (i != ButtonEntry.EVENT_CLOSED && j != ButtonEntry.EVENT_CLOSED) {
			stick.update();
			if (!robot.isOperatorControl()) {
				return 0;
			}
			drive.arcadeDrive(-stick.getY(), -stick.getX()); // drive with
																// arcade style
																// (use right
																// stick)
																// (inverted) //
																// drive with
																// arcade style
																// (use right
																// stick)
			i = stick.getEvent(1);
			j = stick.getEvent(2);
		}

		if (isFirst) {
			startTime = System.currentTimeMillis();
		}

		if (i == ButtonEntry.EVENT_CLOSED) {
			String time = "" + (System.currentTimeMillis() - startTime);
			String actionType = "drive";
			String encoderVal = ""
					+ (leftEncoder.getDistance() + rightEncoder.getDistance())
					/ 2;

			Utils.addLine(new String[] { time, actionType, encoderVal }, f);
		} else {
			String time = "" + (System.currentTimeMillis() - startTime);
			String actionType = "turn";
			String gyroAngle = "" + gyro.getAngle();

			Utils.addLine(new String[] { time, actionType, gyroAngle }, f);

		}

		resetMeasurements();
		return startTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#resetMeasurments()
	 */
	public void resetMeasurements() {
		leftEncoder.reset();
		rightEncoder.reset();
		gyro.reset(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#doRobotLogging()
	 */
	public boolean doRobotLogging() {
		return saveWaypoints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.team2994.frc.autonomous.DriveManager#usePID()
	 */
	public boolean usePID() {
		return usePID();
	}

	/**
	 * Turn without using PID
	 * 
	 * @param degrees
	 *            The amount to turn
	 */
	private void deadReckonTurn(int degrees) {
		gyro.reset();

		while ((gyro.getAngle() < degrees) && robot.isEnabled()) {
			drive.setLeftRightMotorOutputs(
					conf.getPropertyAsDouble("turnSpeed", 0.25),
					-conf.getPropertyAsDouble("turnSpeed", 0.25));
		}

		drive.drive(0, 0);
	}

	/**
	 * Drive without using PID
	 * 
	 * @param distance
	 *            The distance to drive
	 */
	private void deadReckonDrive(double distance) {
		leftEncoder.reset();
		rightEncoder.reset();

		double distanceTravelled = 0.0;

		while ((distanceTravelled < distance) && robot.isEnabled()) {
			distanceTravelled = (leftEncoder.getDistance() + rightEncoder
					.getDistance()) / 2;
			drive.drive(conf.getPropertyAsDouble("driveSpeed", 0.25), 0.0);
		}

		drive.drive(0, 0);
	}
}
