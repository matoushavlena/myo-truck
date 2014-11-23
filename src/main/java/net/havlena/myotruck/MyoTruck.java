package net.havlena.myotruck;

import java.rmi.RemoteException;

import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;

import lejos.hardware.BrickFinder;
import lejos.hardware.BrickInfo;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

public class MyoTruck {
	// Steering motor
	private RMIRegulatedMotor motorS;
	// Left motor
	private RMIRegulatedMotor motorL;
	// Right motor
	private RMIRegulatedMotor motorR;
	private RemoteEV3 ev3;
	private GraphicsLCD lcd;
	private boolean running = true;
	private boolean sleeping = false;
	//private int totalSteps = 0;
	// In case your maximum steering angles are shifted to either left or right, you can correct it here.
	private static int ANGLE_STEERING_CORRECTION = 0;
	// We don't want to allow steering to maximum values
	private static int STEERING_RESERVE_FROM_MAX = 20;
	// The higher number, the more sensitive, 1 is the lowest
	private static int STEERING_SENSITIVITY = 3;
	// The higher number, the more sensitive, 1 is the lowest
	private static int SPEED_SENSITIVITY = 3;
	private int steeringScale;  
	
	public MyoTruck(String host) throws RemoteException {
		try {
			BrickInfo[] bricks = BrickFinder.discover();
			if (bricks.length==0) throw new RuntimeException("Unable to find an EV3!");
			this.ev3 = new RemoteEV3(bricks[0].getIPAddress());
			ev3.setDefault();
			Sound.beep();
			Button.LEDPattern(4);
			lcd = ev3.getGraphicsLCD();
			lcd.clear();
			lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
			lcd.drawString("Running...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
			motorS = ev3.createRegulatedMotor("A", 'M');
			motorL = ev3.createRegulatedMotor("B", 'L');
			motorR = ev3.createRegulatedMotor("C", 'L');
			
			motorS.setSpeed((int)(motorS.getMaxSpeed()*0.8));
			motorS.setAcceleration(3000);
			// Reset front wheels to center position
			motorS.rotateTo(-500);
			int angleLeft = motorS.getTachoCount();
			motorS.rotateTo(500);
			int angleRight = motorS.getTachoCount();
			steeringScale = (Math.abs(angleLeft)+Math.abs(angleRight))/2;
			motorS.rotateTo(angleLeft+steeringScale+ANGLE_STEERING_CORRECTION);
			steeringScale -= STEERING_RESERVE_FROM_MAX;
			motorS.resetTachoCount();
			motorS.rotateTo(-steeringScale);
			motorS.rotateTo(steeringScale);
			motorS.rotateTo(0);

			motorL.setAcceleration(3000);
			motorR.setAcceleration(3000);
			motorL.setSpeed(0);
			motorR.setSpeed(0);
			motorL.stop(true);
			motorR.stop(true);
		} catch (Exception e) {
			this.close();
			e.printStackTrace();
		}
	}
	
	private void close() {
		Button.LEDPattern(5);
		try {
			if (motorS!=null) motorS.close();
			if (motorL!=null) motorL.close();
			if (motorR!=null) motorR.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		lcd.clear();
		lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		lcd.drawString("Closing...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		Delay.msDelay(3000);
		lcd.clear();
		lcd.refresh();
		Button.LEDPattern(0);
	}
	
	private void run(Hub hub, MyoDataCollector dataCollector) throws RemoteException {
    	while (running) {
    		hub.run(1000/20);
    		System.out.print(dataCollector);
    		if (sleeping==true) {
    			try {
        			Thread.sleep(200);
        			continue;
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
    		}
    		/*totalSteps++;
    		if (totalSteps>100) {
    			running = false;
    			break;
    		}*/
	    	System.out.print(dataCollector);
	    	this.setSpeed(dataCollector.getPitch());
	    	this.setSteering(dataCollector.getRoll(), dataCollector.isLeftArm());
    	}
    	motorL.stop(true);
		motorR.stop(true);
	}
	
	private void setSpeed(double pitch) {
		try {
			int speed = 0;
			int scalePitch = MyoDataCollector.SCALE/2/SPEED_SENSITIVITY;
			int scaleMotor = (int)motorL.getMaxSpeed();
			if (pitch>=0) speed = (int)((-scalePitch*SPEED_SENSITIVITY+pitch)*(scaleMotor/scalePitch));
			motorL.setSpeed(speed);
			motorR.setSpeed(speed);
			if (speed>=0) {
				motorL.backward();
				motorR.backward();
			} else {
				motorL.forward();
				motorR.forward();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void setSteering(double roll, boolean isLeftArm) {
		try {
			int steering = 0;
			int scale = MyoDataCollector.SCALE/2/STEERING_SENSITIVITY;
			if (roll>=0) steering = (int)((-scale*STEERING_SENSITIVITY+roll)*(steeringScale/scale));
			if (isLeftArm) steering = steering*-1;
			motorS.rotateTo(steering);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		this.running = false;
	}
	
	public void sleep() {
		lcd.clear();
		lcd.drawString("Myo Truck", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		lcd.drawString("Sleeping...", LCD.SCREEN_WIDTH/2, LCD.SCREEN_HEIGHT/2+30, GraphicsLCD.BASELINE | GraphicsLCD.HCENTER);
		Button.LEDPattern(6);
		try {
			motorL.setSpeed(0);
			motorR.setSpeed(0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
		this.running = true;
		this.sleeping = true;
	}
	
	public void start() {
		Button.LEDPattern(4);
		this.running = true;
		this.sleeping = false;
	}
	
	protected void finalize() throws Throwable {
		try {
			this.close();
		} finally {
			super.finalize();
		}
	}
	
	public static void main(String[] args) {
		try {
			MyoTruck myotruck = new MyoTruck("192.168.192.5");
			Hub hub = new Hub("net.havlena.myo");
		    Myo myo = hub.waitForMyo(10000);
		    if (myo == null) {
				throw new RuntimeException("Unable to find a Myo!");
			}
		    MyoDataCollector dataCollector = new MyoDataCollector(myotruck);
		    hub.addListener(dataCollector);
		    myotruck.run(hub, dataCollector);
		    myotruck.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}