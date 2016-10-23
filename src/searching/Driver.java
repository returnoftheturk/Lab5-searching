package searching;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Driver extends Navigation{

	enum State {
		INIT, TURNING, TRAVELLING, EMERGENCY, SEARCHING
	};

	State state;

	private EV3LargeRegulatedMotor armMotor;
	private boolean isNavigating = false;

	private double destx, desty;

	final static int SLEEP_TIME = 50;
	final static int armLength = 10;
	
	UltrasonicPoller usSensor;
	BlockDetection detection;
	
	
	public Driver(Odometer odo, UltrasonicPoller usSensor, BlockDetection detection, EV3LargeRegulatedMotor armMotor) {
		super(odo);
		this.usSensor = usSensor;
		this.detection = detection;
		this.armMotor = armMotor;
		// TODO Auto-generated constructor stub
	}

	public void travelTo(double x, double y, boolean avoid) {
		if (avoid) {
			destx = x;
			desty = y;
			isNavigating = true;
		} else {
			super.travelTo(x, y);
		}
	}
	
	private void updateTravel() {
		double minAng;

		minAng = calculateAngle(destx, desty);
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
				 * However, this implementation would be necessary if you would like
				 * to stop a turn in the middle (e.g. if you were traveling but also
				 * scanning with a sensor for something...)
				 * 
				 */
				double destAngle = calculateAngle(destx, desty);
				turnTo(destAngle);
				if(facingDest(destAngle)){
					setSpeeds(0,0);
					state = State.TRAVELLING;
				}
				break;
			case TRAVELLING:
				if (checkEmergency()) { // order matters!
					state = State.EMERGENCY;
					avoidance = new ObstacleAvoidance(this);
					avoidance.start();
				} else if (!checkIfDone(destx, desty)) {
					updateTravel();
				} else { // Arrived!
					setSpeeds(0, 0);
					isNavigating = false;
					state = State.INIT;
				}
				break;
			case EMERGENCY:
				if (avoidance.resolved()) {
					state = State.TURNING;
				}
				break;
			// search for the block, catch the block if detected
			case SEARCHING:
				if (!detection.detectObject()) {
					turnBy(90);
					if (usSensor.getDistance() - Math.hypot(odometer.getX(),odometer.getY()) < 120) {
						setSpeeds(0,0);
						goForward(usSensor.getDistance() - armLength);
					}
				}
				if (detection.detectBlock()) {
					armMotor.rotate(90);
					state = State.TRAVELLING;
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
			this.setSpeeds(-SLOW, SLOW);
		} else if (error < 0.0) {
			this.setSpeeds(SLOW, -SLOW);
		} else if (error > 180.0) {
			this.setSpeeds(SLOW, -SLOW);
		} else {
			this.setSpeeds(-SLOW, SLOW);
		}

	}

	/*
	 * Go foward a set distance in cm with or without avoidance
	 */
	public void goForward(double distance, boolean avoid) {
		double x = odometer.getX()
				+ Math.cos(Math.toRadians(this.odometer.getTheta())) * distance;
		double y = odometer.getY()
				+ Math.sin(Math.toRadians(this.odometer.getTheta())) * distance;

		this.travelTo(x, y, avoid);

	}

	public boolean isTravelling() {
		return isNavigating;
	}

}