package searching;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class ObstacleAvoidance extends Thread {

	Navigator nav;
	boolean safe;
	boolean obstruction;
	private SampleProvider colorSensor;
	private float[] colorData;
	private double destx, desty;
	private static final EV3LargeRegulatedMotor armMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	public ObstacleAvoidance(Navigator nav, SampleProvider colorSensor, float[] colorData,
			double destx, double desty) {
		this.nav = nav;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.destx = destx;
		this.desty = desty;
		safe = false;
		obstruction = false;
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
		while (nav.usSensor.getDistance() > 3 && nav.usSensor.getDistance()< 25) {

		}
		nav.stopMotors();
		if (getColorData() < 7 && getColorData() > 5) {
			Sound.buzz();
			armMotor.setSpeed(200);
			armMotor.rotate(100, false);
			nav.travelTo(60, 60, false);
			nav.turnTo(45, true);
			armMotor.rotate(-100, false);
			while(nav.usSensor.getDistance()<10){
				nav.setSpeeds(-30, -30);
			}
			
		} else  {
			if (Math.abs(destx - nav.odometer.getX())<15 || Math.abs(desty - nav.odometer.getY())<15){
				obstruction = true;
			}
			while(nav.usSensor.getDistance()<15){
				nav.setSpeeds(-30, -30);
			}
			
			nav.turnBy(90);
			if (nav.usSensor.getDistance()>50){
				nav.goForward(30, false);
			} else {
				nav.turnBy(180);
			}
			if (nav.usSensor.getDistance()>50){
				nav.goForward(30,false);
			} else {
				nav.turnBy(-90);
			} 
			if (nav.usSensor.getDistance()>50){
				nav.goForward(30, false);
			}
			else obstruction = true;
				
		}
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
	public boolean obstructionAtPoint(){
		return obstruction;
	}
}
