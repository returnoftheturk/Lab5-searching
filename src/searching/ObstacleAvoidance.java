//Seperate thread to enter once the robot detects an object
//Gets close to object to tell whether or not it is a block or non block

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
		Sound.beep();
		//get closer to the newly found target
		while (nav.usSensor.getDistance() > 3 && nav.usSensor.getDistance()< 25) {

		}
		nav.stopMotors();
		
		//detect whether or not it is a block or non block
		if (getColorData() < 7 && getColorData() > 5) {
			//this means it is a block
			Sound.buzz();
			Sound.buzz();
			//grabs block
			armMotor.setSpeed(100);
			armMotor.rotate(100, false);
			nav.travelTo(63, 63, false);
			nav.turnTo(45, true);
			armMotor.rotate(-100, false);
			Sound.beep();
			Sound.beep();
			Sound.beep();
			while(nav.usSensor.getDistance()<10){
				nav.setSpeeds(-30, -30);
			}
			
		} else  {
			//non block or a wall
			Sound.buzz();
			obstruction = true;
			//boolean to go to the next point
			
//			if (Math.abs(destx - nav.odometer.getX())<15 || Math.abs(desty - nav.odometer.getY())<15){
//				obstruction = true;
//			}
			while(nav.usSensor.getDistance()<25){
				nav.setSpeeds(-30, -30);
			}
//			
			//this is code that we were using to hard code avoiding obstacles. 
			//however it also did not work with walls.  As it would enter a thread
			//and stop polling the ultrasonic until it exited the thread.
//			nav.turnBy(90);
//			if (nav.usSensor.getDistance()>50){
//				nav.goForward(30, false);
//			} else {
//				nav.turnBy(180);
//			}
//			if (nav.usSensor.getDistance()>50){
//				nav.goForward(30,false);
//			} else {
//				nav.turnBy(-90);
//			} 
//			if (nav.usSensor.getDistance()>50){
//				nav.goForward(30, false);
//			}
//			else obstruction = true;
				
		}
		safe = true;
	}

	//method to get colorData from the light sensor
	public double getColorData() {
		colorSensor.fetchSample(colorData, 0);
		double colorLevel = colorData[0];
		return colorLevel;
	}

	public boolean resolved() {
		return safe;
	}
	
	//method to go to the next point if there is an obstruction at the current point
	public boolean obstructionAtPoint(){
		return obstruction;
	}
}
