package searching;

import lejos.hardware.Sound;
import lejos.hardware.sensor.SensorModes;

/*
 * 
 * The Navigator class extends the functionality of the Navigation class.
 * It offers an alternative travelTo() method which uses a state machine
 * to implement obstacle avoidance.
 * 
 * The Navigator class does not override any of the methods in Navigation.
 * All methods with the same name are overloaded i.e. the Navigator version
 * takes different parameters than the Navigation version.
 * 
 * This is useful if, for instance, you want to force travel without obstacle
 * detection over small distances. One place where you might want to do this
 * is in the ObstacleAvoidance class. Another place is methods that implement 
 * specific features for future milestones such as retrieving an object.
 * 
 * 
 */


public class Navigator extends Navigation {

	enum State {
		INIT, TURNING, TRAVELLING, EMERGENCY
	};

	State state;

	private boolean isNavigating = false;
	private SensorModes colorSensor;
	private float[] colorData;

	private double destx, desty;

	final static int SLEEP_TIME = 50;

	UltrasonicPoller usSensor;

	public Navigator(Odometer odo, UltrasonicPoller usSensor, SensorModes colorSensor, float [] colorData) {
		super(odo);
		this.usSensor = usSensor;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm
	 * Will travel to designated position, while constantly updating it's
	 * heading
	 * 
	 * When avoid=true, the nav thread will handle traveling. If you want to
	 * travel without avoidance, this is also possible. In this case,
	 * the method in the Navigation class is used.
	 * 
	 */
	public void travelTo(double x, double y, boolean avoid) {
		if (avoid) {
			destx = x;
			desty = y;
			isNavigating = true;
		} else {
			super.travelTo(x, y);
		}
	}

	
	/*
	 * Updates the h
//	 */
	private void updateTravel() {
		double minAng;

		minAng = getDestAngle(destx, desty);
		/*
		 * Use the BasicNavigator turnTo here because 
		 * minAng is going to be very small so just complete
		 * the turn.
		 */
		super.turnTo(minAng,false);
		this.setSpeeds(FAST, FAST);
	}

	public void run() {
		ObstacleAvoidance avoidance = null;
		state = State.INIT;
		while (true) {
			switch (state) {
			case INIT:
//				Sound.buzz();
				if (isNavigating) {
					state = State.TURNING;
				}
				break;
			case TURNING:
				/*
				 * Note: you could probably use the original turnTo()
				 * from BasicNavigator here without doing any damage.
				 * It's cheating the idea of "regular and periodic" a bit
				 * but if you're sure you never need to interrupt a turn there's
				 * no harm.
				 * 
				 * However, this implementation would be necessary if you would like
				 * to stop a turn in the middle (e.g. if you were travelling but also
				 * scanning with a sensor for something...)
				 * 
				 */
//				Sound.beep();
				double destAngle = getDestAngle(destx, desty);
				turnTo(destAngle);
				if(facingDest(destAngle)){
					stopMotors();
					state = State.TRAVELLING;
				}
				break;
			case TRAVELLING:
//				Sound.buzz();
				if (checkEmergency()) { // order matters!
					state = State.EMERGENCY;
					avoidance = new ObstacleAvoidance(this, colorSensor, colorData);
					avoidance.start();
				} else if (!checkIfDone(destx, desty)) {
					updateTravel();
				} else { // Arrived!
					stopMotors();
					isNavigating = false;
					state = State.INIT;
				}
				break;
			case EMERGENCY:
				if (avoidance.resolved()) {
					state = State.TURNING;
				}
				break;
			}
//			Log.log(Log.Sender.Navigator, "state: " + state);
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean checkEmergency() {
		return usSensor.getDistance() < 10;
	}


	private void turnTo(double angle) {
		double error;
		error = angle - this.odometer.getTheta();

		if (error < -180.0) {
			this.setSpeeds(SLOW, -SLOW);
		} else if (error < 0.0) {
			this.setSpeeds(-SLOW, SLOW);
		} else if (error > 180.0) {
			this.setSpeeds(-SLOW, SLOW);
		} else {
			this.setSpeeds(SLOW, -SLOW);
		}

	}

	/*
	 * Go foward a set distance in cm with or without avoidance
	 */
	public void goForward(double distance, boolean avoid) {
		double x = odometer.getX()
				+ Math.sin(Math.toRadians(this.odometer.getTheta())) * distance;
		double y = odometer.getY()
				+ Math.cos(Math.toRadians(this.odometer.getTheta())) * distance;

		this.travelTo(x, y, avoid);

	}

	public boolean isTravelling() {
		return isNavigating;
	}



}
