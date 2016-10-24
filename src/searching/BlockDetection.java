package searching;

import lejos.hardware.Button;
import lejos.robotics.SampleProvider;

public class BlockDetection implements UltrasonicController {
	private SampleProvider usSensor, colorSensor;
	private int distance;
	private String[] blockInfo;
	private float[] colorData;
	

	public BlockDetection(SampleProvider usSensor, SampleProvider colorSensor, float[] colorData) {
		this.usSensor = usSensor;
		this.colorSensor = colorSensor;
		this.blockInfo = new String[] { "No Object Detected", "No Object Detected" };
		this.colorData = colorData;
	}

	public void doDetection(){
		int count = 0;
		while (true){
			if (distance>5){
				resetBlockInfo();
			}
			
			if (distance<5){
				blockInfo[0] = "Object Detected";
				if (getColorData()<7 && getColorData()>5){
					blockInfo[1] = "Block";
				}
				else if (getColorData()>10 && getColorData()<15){
					blockInfo[1] = "Not Block";
				}
			}
			
		}
		
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO Auto-generated method stub

	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return distance;
	}

	public void getBlockInfo(String[] info) {
		info[0] = blockInfo[0];
		info[1] = blockInfo[1];
	}

	private void resetBlockInfo() {
		blockInfo[0] = "No Object Detected";
		blockInfo[1] = "No Object Detected";
	}

	// get color sensor reading
	public double getColorData() {
		colorSensor.fetchSample(colorData, 0);
		double colorLevel = colorData[0];
		return colorLevel;
	}

	public boolean detectBlock() {
		if (blockInfo[1] == "Block") {
			return true;
		}
		return false;
	}

	public boolean detectObject() {
		if (blockInfo[0] == "Object Detected") {
			return true;
		}
		return false;
	}
}
