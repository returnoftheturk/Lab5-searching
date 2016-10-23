package searching;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class Lab5 {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static final Port colorPort = LocalEV3.get().getPort("S4");


	private static Navigator nav;
	private static Navigation navigation;

	public static void main(String[] args) {

		// Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize
		// operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource") // Because we don't bother to close this
										// resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance"); // colorValue
																// provides
																// samples from
																// this instance
		float[] usData = new float[usValue.sampleSize()]; // colorData is the
															// buffer in which
															// data are returned

		// Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample providoer instance for the above and initialize
		// operating mode
		// 4. Create a buffer for the sensor data

		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("ColorID"); // colorValue
																	// provides
																	// samples
																	// from this
																	// instance
		float[] colorData = new float[colorValue.sampleSize()]; // colorData is
																// the buffer in
																// which data
																// are returned

		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		navigation = new Navigation(odo);

		int buttonChoice;

		// some objects that need to be instantiated

		final TextLCD t = LocalEV3.get().getTextLCD();

		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString("Detect | Search ", 0, 2);
			t.drawString("Blocks |   and  ", 0, 3);
			t.drawString("       |  Find  ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			t.clear();
			BlockDetection blockDetection = new BlockDetection(usSensor, colorSensor, colorData);
			UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, blockDetection);
			usPoller.start();
			LCDInfo lcd = new LCDInfo(odo, LCDInfo.DemoType.OBJECT_DETECTION, blockDetection);
			blockDetection.doDetection();
			// odometer.start();
			// odometryDisplay.start();
		} else if (buttonChoice == Button.ID_RIGHT) {
			t.clear();
			USLocalizer usl = new USLocalizer(odo, navigation, usValue, usData,
					USLocalizer.LocalizationType.FALLING_EDGE, rightMotor, leftMotor);
			UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, usl);
			nav = new Navigator(odo, usPoller);
			
			LCDInfo lcd = new LCDInfo(odo, LCDInfo.DemoType.OBJECT_SEARCH_FIND, usl);
			usPoller.start();
			nav.start();
//			nav.turnBy(360);
//			usl.doLocalization();
			completeCourse();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);

	}

	private static void completeCourse() {
		int[][] waypoints = { { 60, 30 }, { 30, 30 }, { 30, 60 }, { 60, 0 } };

		for (int[] point : waypoints) {
			Sound.buzz();
			nav.travelTo(point[0], point[1], true);
			while (nav.isTravelling()) {
				Sound.beep();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
