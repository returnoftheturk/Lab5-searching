package searching;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDInfo implements TimerListener{
	public enum DemoType {
		OBJECT_DETECTION, OBJECT_SEARCH_FIND
	};
	
	private DemoType demoType;
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private USLocalizer usl;
	private BlockDetection blk;
	private Timer lcdTimer;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
//	private LightLocalizer lsl;
	
	// arrays for displaying data
	private double [] pos;
	private String[] info;
	private UltrasonicController usc; 
	
	public LCDInfo(Odometer odo, DemoType demoType, UltrasonicController usc) {
		this.odo = odo;
		if (demoType == DemoType.OBJECT_DETECTION)
			this.blk = (BlockDetection)usc;
		if (demoType == DemoType.OBJECT_SEARCH_FIND)
			this.usl = (USLocalizer)usc;
		this.demoType = demoType;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		// initialise the arrays for displaying data
		info = new String[2];
		pos = new double[3];
		
		// start the timer
		lcdTimer.start();
	}
		
	public void timedOut() {
		
		if (demoType == DemoType.OBJECT_DETECTION){
			LCD.clear();
			blk.getBlockInfo(info);
			LCD.drawString(info[0], 0, 0);
			LCD.drawString(info[1], 0, 1);
			
		} else if (demoType == DemoType.OBJECT_SEARCH_FIND){
			odo.getPosition(pos);
			LCD.clear();
			LCD.drawString("X: ", 0, 0);
			LCD.drawString("Y: ", 0, 1);
			LCD.drawString("H: ", 0, 2);
			LCD.drawString("US:          ", 0, 3);
			LCD.drawString("C: ", 0, 4);
			LCD.drawString(formattedDoubleToString(pos[0], 2), 3, 0);
			LCD.drawString(formattedDoubleToString(pos[1], 2), 3, 1);
			LCD.drawString(formattedDoubleToString((pos[2]),4), 3, 2);
			LCD.drawInt((int)usl.readUSDistance(), 4, 3);
//			LCD.drawString(formattedDoubleToString(lsl.getColorData(), 2), 3, 4);	
		}
	}
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}
}
