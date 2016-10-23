package searching;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class USLocalizer implements UltrasonicController {
	public enum LocalizationType {
		FALLING_EDGE, RISING_EDGE
	};

	public static int ROTATION_SPEED = 30;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	private Odometer odo;
	private Navigation nav;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private final int d = 35;
	private final int k = 8;
	private int wallDistance;

	public USLocalizer(Odometer odo, Navigation nav, SampleProvider usSensor, float[] usData, LocalizationType locType,
			EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor leftMotor) {
		this.odo = odo;
		this.nav = nav;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.rightMotor = rightMotor;
		this.leftMotor = leftMotor;

		this.leftMotor.setAcceleration(this.nav.ACCELERATION);
		this.rightMotor.setAcceleration(this.nav.ACCELERATION);
	}

	public void doLocalization() {
		double angleA, angleA1, angleA2, angleB, angleB1, angleB2;

		if (locType == LocalizationType.FALLING_EDGE) {

			// if distance>50
			//rotate until robot sees a wall
			if (getFilteredData() > d + k) {
				while (getFilteredData() > d) {
					nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
				}
				nav.stopMotors();
			}
			
			// rotate the robot until it sees no wall

			// start rotating towards the right (positive angle)
			// while distance < 50
			
			while (getFilteredData() < d + k) {
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}
			Sound.buzz();
			nav.stopMotors();
			angleA1 = odo.getTheta();

			// keep rotating until robot is under the falling edge
			// while distance > 30
			// KEEP ROTATING UNTIL THE ROBOT SEES A WALL, THEN LATCH THE
			// ANGLE
			while (getFilteredData() > d - k) { // - tolerance
				nav.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);

			}
			Sound.buzz();
			nav.stopMotors();
			angleB1 = odo.getTheta();

			// while distance < 40
			// rotate back the other way
			while (getFilteredData() < d + k) {
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			Sound.buzz();
			nav.stopMotors();
			angleB2 = odo.getTheta();

			// while distance>30
			// keep rotating until the robot sees a wall, then latch the
			// angle
			while (getFilteredData() > d - k) {
				nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}
			Sound.buzz();
			nav.stopMotors();
			angleA2 = odo.getTheta();

			angleB = (angleB1 + angleB2) / 2;
			angleA = (angleA1 + angleA2) / 2;

			if (angleA > angleB) {
				angleB += 360;
			}

			nav.turnBy((angleB - angleA) / 2 - 38 + (angleA1 - angleA2) / 2);
			nav.stopMotors();
			Sound.buzz();

			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			// update the odometer position (example to follow:)

			odo.setPosition(new double[] { 0.0, 0.0, 0.0 }, new boolean[] { true, true, true });
			Sound.beep();
			
			nav.turnto1(-90);
			nav.stopMotors();
			double loc1 = readUSDistance();
			Sound.beep();
			nav.turnto1(-180);
			double loc2 = readUSDistance();
			Sound.beep();
			nav.travelTo(30.48-(loc1+8), 30.48- (loc2+8));
			Sound.beep();
			nav.turnto1(0);
			odo.setPosition(new double[] { 0.0, 0.0, 0.0 }, new boolean[] { true, true, true });
			
		} 
	}

	private float getFilteredData() {
		//method to filter data greater than 255
		usSensor.fetchSample(usData, 0);
		float distance = usData[0] * 100;
		if (distance > 255)
			distance = 255;

		return distance;
	}

	@Override
	public void processUSData(int distance) {
		this.wallDistance = distance;

	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return wallDistance;
	}


}
