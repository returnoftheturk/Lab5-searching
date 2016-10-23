package searching;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObstacleAvoidance extends Thread {

	Navigator nav;
	boolean safe;
	private SampleProvider colorSensor;
	private float[] colorData;
	private static final EV3LargeRegulatedMotor armMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	public ObstacleAvoidance(Navigator nav, SampleProvider colorSensor, float[] colorData) {
		this.nav = nav;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		safe = false;
	}

	public void run() {

		/*
		 * The "avoidance" just stops and turns to heading 0 to make sure that
		 * the threads are working properly.
		 * 
		 * If you want to call travelTo from this class you MUST call
		 * travelTo(x,y,false) to go around the state machine
		 * 
		 * This means that you can't detect a new obstacle while avoiding the
		 * first one. That's probably not something you were going to do anyway.
		 * 
		 * Otherwise things get complicated and a lot of new states will be
		 * necessary.
		 * 
		 */

		// Log.log(Log.Sender.avoidance,"avoiding obstacle!");
		nav.setSpeeds(30, 30);
		while (nav.usSensor.getDistance() > 3) {

		}
		nav.stopMotors();
		if (getColorData() < 7 && getColorData() > 5) {
			Sound.buzz();
			armMotor.setSpeed(200);
			armMotor.rotate(100, false);
			nav.travelTo(75, 75, false);
			
		} else  {
			Sound.beep();
			nav.travelTo(nav.odometer.getX()-5, nav.odometer.getY()-5, false);
			nav.turnTo(nav.odometer.getTheta() + 90, false);
			nav.travelTo(nav.odometer.getX() + 15, nav.odometer.getY()+15, false);
			nav.turnTo(nav.odometer.getTheta() - 90, false);

		}

		// nav.turnTo(0,true);
		// nav.goForward(5, false); //using false means the Navigation method is
		// used
		// Log.log(Log.Sender.avoidance,"obstacle avoided!");
		safe = true;
	}

	public double getColorData() {
		colorSensor.fetchSample(colorData, 0);
		double colorLevel = colorData[0];
		return colorLevel;
	}

	public boolean resolved() {
		return safe;
	}
}
