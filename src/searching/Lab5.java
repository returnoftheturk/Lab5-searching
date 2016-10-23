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
	
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 14.6;

	
	public static void main(String[] args) {
		
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample providoer instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("ColorID");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		
		
		Navigation nav = new Navigation(odo);
		
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, nav, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE, rightMotor, leftMotor);
		
		LightLocalizer lsl = new LightLocalizer(odo, nav, colorValue, colorData, rightMotor, leftMotor);
		BlockDetection blockDetection = new BlockDetection(usSensor, colorSensor, colorData);
		
		int buttonChoice;

		// some objects that need to be instantiated
		
		final TextLCD t = LocalEV3.get().getTextLCD();
//		Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
//		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);
//		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer);

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
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			t.clear();
			UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, blockDetection);
			usPoller.start();
			LCDInfo lcd = new LCDInfo(odo, usl, lsl, LCDInfo.DemoType.OBJECT_DETECTION, blockDetection);
			blockDetection.doDetection();
//			odometer.start();
//			odometryDisplay.start();
		} else if (buttonChoice == Button.ID_RIGHT){
			t.clear();
			UltrasonicPoller usPoller = new UltrasonicPoller(usValue, usData, usl);
			LCDInfo lcd = new LCDInfo(odo, usl, lsl, LCDInfo.DemoType.OBJECT_SEARCH_FIND, blockDetection);
			usPoller.start();
			usl.doLocalization();
//			odometer.start();
//			odometryDisplay.start();
//			odometryCorrection.start();
//			(new Thread() {
//				public void run() {
//					SquareDriver.drive(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK);
//				}
//			}).start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
		
	}

}
