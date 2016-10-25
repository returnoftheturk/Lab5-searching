//Code to run part 1 of the demo
//will detect if its a block or non block
//passes information to LCDINFO 

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
		//assume no blocks are detected at first
		this.blockInfo = new String[] { "No Object Detected", "No Object Detected" };
		this.colorData = colorData;
	}

	public void doDetection(){
		//have program run forever so it can keep detecting for as long
		//as the demo goes on
		while (true){
			//if distance is not less than 5, have display say no object detected.
			if (distance>5){
				resetBlockInfo();
			}
			
			//within range, detect whether or not block or non block
			if (distance<5){
				blockInfo[0] = "Object Detected";
				
				//block
				if (getColorData()<7 && getColorData()>5){
					blockInfo[1] = "Block";
				}
				
				//non block
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

	//constructor to display on screen
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
}
