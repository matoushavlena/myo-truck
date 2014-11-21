package net.havlena.myotruck;

import java.rmi.RemoteException;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;


public class MyoTruck {
	
	private RMIRegulatedMotor motorW;
	private RMIRegulatedMotor motorL;
	private RMIRegulatedMotor motorR;
	private RemoteEV3 ev3;
	private boolean running = true;
	private int totalSteps = 0;
	
	public MyoTruck(String host) throws RemoteException {
		try {
			this.ev3 = new RemoteEV3(host);
			ev3.setDefault();
			Sound.beep();
			motorW = ev3.createRegulatedMotor("A", 'M');
			motorL = ev3.createRegulatedMotor("B", 'L');
			motorR = ev3.createRegulatedMotor("C", 'L');
			
			motorW.setSpeed((int)motorW.getMaxSpeed());
			// RESET FRONT WHEELS TO CENTER POSITION
			motorW.rotateTo(500);
			motorW.resetTachoCount();
			motorW.rotateTo(-130);
			

			motorL.setSpeed((int)motorL.getMaxSpeed());
			motorR.setSpeed((int)motorR.getMaxSpeed());
			motorL.setAcceleration(5000);
			motorR.setAcceleration(5000);
			motorL.stop(true);
			motorR.stop(true);
			motorL.backward();
			motorR.backward();
		} catch (Exception e) {
			if (motorW!=null) motorW.close();
			if (motorL!=null) motorL.close();
			if (motorR!=null) motorR.close();  
			e.printStackTrace();
		}
	}
	
	public void run() throws RemoteException {
    	while (running) {
    		totalSteps++;
    		if (totalSteps>20) {
    			running = false;
    			break;
    		}
    		try {
    			Thread.sleep(50);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	motorL.stop(true);
		motorR.stop(true);
		if (motorW!=null) motorW.close();
		if (motorL!=null) motorL.close();
		if (motorR!=null) motorR.close(); 
	}
	
	public static void main(String[] args) {
		try {
			MyoTruck myotruck = new MyoTruck("192.168.192.5");
			myotruck.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
}