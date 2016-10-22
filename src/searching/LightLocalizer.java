package searching;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer extends Thread{

	public static int ROTATION_SPEED = 30;
	private static final long CORRECTION_PERIOD = 10;

	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private float[] colorData;
	// private static double colorLevel;

	// set variables
	private boolean inPosition;
	private final double black = 0.27;
	private double wheelRadius;
	private final double sensorDistance = 14.0;
	private double[] angles;
	private int angleIndex;

	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public LightLocalizer(Odometer odo, Navigation nav, SampleProvider colorValue, float[] colorData,
			EV3LargeRegulatedMotor rightMotor, EV3LargeRegulatedMotor leftMotor) {
		this.odo = odo;
		this.nav = nav;
		this.colorSensor = colorValue;
		this.colorData = colorData;
		// get the motors
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.wheelRadius = odo.getRadius();
		// initialize arrays
		this.angles = new double[4];
		this.angleIndex = 0;
		this.leftMotor.setAcceleration(this.nav.ACCELERATION);
		this.rightMotor.setAcceleration(this.nav.ACCELERATION);

	}

	public void doLocalization() {
		long correctionStart, correctionEnd;
		correctionStart = System.currentTimeMillis();
		
		//NOTE: HERE WE HAD TO HARD CODE WHERE THE ROBOT WAS TO GO AS
		//THE TRAVELTO METHOD IN NAVIGATION WAS NOT WORKING CORRECTLY.
		//THE ROBOT WOULD CONSTANTLY OSCILLATE FROM LEFT TO RIGHT IF 
		//WE ATTEMPTED TO USE THIS METHOD.  THEREFORE, WE HAD TO
		//HARD CODE WHERE TO GO FOR THE ROBOT, BUT THIS IS THE ONLY 
		//PART OF THE CODE THAT IS HARDCODED.  
		//THE CORRECT IMPLEMENTATION SHOULD HAVE BEEN TO HAVE
		//THE ROBOT TURN 45 DEGREES, MOVE FORWARD UNTIL LINE DETECTION,
		//AND THEN MOVE BACK.  IT WOULD NOT DO IT.  
		
		// set the motor speed
		leftMotor.setSpeed(nav.SLOW);
		rightMotor.setSpeed(nav.SLOW);
		// rotate the robot by 45 degrees
		leftMotor.rotate(convertAngle(wheelRadius, 14.6, 45), true);
		rightMotor.rotate(-convertAngle(wheelRadius, 14.6, 45), false);
		// drive the robot for certain distance
		leftMotor.rotate(convertDistance(wheelRadius, 14.0), true);
		rightMotor.rotate(convertDistance(wheelRadius, 14.0), false);
		
		
		inPosition = true;
		// now the robot is in position
		if (inPosition == true) {
			Sound.beepSequence();
			// rotate(counter clockwise) 360 degrees and read all 4 lines
			
			nav.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);

			while (angleIndex < 4) {
				// for every black line, beep & record data
				if (getColorData() < black) {
					Sound.buzz();
					angles[angleIndex] = odo.getTheta();
					angleIndex++;
				}
			}
			nav.stopMotors();

			// do trig to compute (0,0) and 0 degrees
			double thetaY = angles[2] - angles[0]; // angle sensed at y axis
			double thetaX = angles[3] - angles[1]; // angle sensed at x axis

			// compute x and y
			double x = (-1) * sensorDistance * Math.cos(Math.PI * thetaY / (2 * 180));
			double y = (-1) * sensorDistance * Math.cos(Math.PI * thetaX / (2 * 180));
			// double thetaYNeg = angles[0];
			// double deltaTheta = 270 + thetaY/2 - thetaYMinus;

			// set position based on the computation
			odo.setPosition(new double[] { x, y, odo.getTheta() }, new boolean[] { true, true, true });
			
			// when done travel to (0,0) and turn to 90 degrees (initial position)
			nav.travelTo(0.0, 0.0);
			//nav.turnTo(0.0);
			
			
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
			
		}

	}


	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// get color sensor reading
	public double getColorData() {
		colorSensor.fetchSample(colorData, 0);
		double colorLevel = colorData[0];
		return colorLevel;
	}
	
	
	//We had to use our own travelTo methods as the ones provided were not working
	//but we were not able to properly organize this code, and that is why
	//it is here
	private double calculateDistance(double x, double y){
		double diffX = x - odo.getX();
		double diffY = y - odo.getY();
		
		double distance = Math.sqrt(Math.pow(diffX, 2)+Math.pow(diffY, 2));
		
		return distance;
	}
	private double calculateAngle(double x, double y){ //calculate angles according to tutorial slides
		double thetad = 0;
		double diffX = x - odo.getX();
		double diffY = y - odo.getY();
		
		//note that we are measuring the angle from the Y axis rather than the x axis, 
		//as such the numbers are a little more different.
		if (diffY!=0){
			if (diffY>0){
				thetad = Math.atan(diffX/diffY);
				if (diffX==0)
					thetad = 0;
			}
			
			else if (diffY<0){
				if (diffX<0)
					thetad = Math.atan(diffX/diffY) - Math.PI;
				if (diffX>0)
					thetad = Math.atan(diffX/diffY) + Math.PI;
				if (diffX==0)
					thetad = Math.PI;
			}
		} else if (diffY==0){
			if (diffX>0)
				thetad = Math.PI/2;
			if (diffX<0)
				thetad = -Math.PI/2;
			if (diffX==0)
				thetad = 0;
		}
		
		//get the difference between the thetas
		double thetar = odo.getTheta();
		
		double diffTheta = thetad - thetar;
		
		//make sure it uses the minimum angle
		if (diffTheta<-Math.PI)
			diffTheta += 2* Math.PI;
		if (diffTheta>Math.PI)
			diffTheta -= 2* Math.PI;
		
		return diffTheta;
		
	}
	// this is turnTo method from previous lab
	public void turnTo(double theta){
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		
		//rotate wheels
		leftMotor.rotate(convertAngle(wheelRadius, 14.6, theta), true);
		rightMotor.rotate(-convertAngle(wheelRadius, 14.6, theta), false);
	}
	// this is travelTo method from previous lab
	public void travelTo(double x, double y){
		//call methods to calculate the angle and distance needed to travel
		double diffTheta = calculateAngle(x, y);
		double distance = calculateDistance(x, y);
		
		turnTo(diffTheta); //turn to the needed angle
		
		leftMotor.setSpeed(nav.SLOW);
		rightMotor.setSpeed(nav.SLOW);
		
		//rotate wheels to go to the needed distance
		leftMotor.rotate(convertDistance(wheelRadius, distance), true);
		rightMotor.rotate(convertDistance(wheelRadius, distance), false);
		
		
		
	}

}