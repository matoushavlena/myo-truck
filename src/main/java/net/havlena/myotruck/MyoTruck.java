package net.havlena.myotruck;

import java.rmi.RemoteException;

import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.example.DataCollector;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;

public class MyoTruck {
	
	public static RMIRegulatedMotor motorW;
	public static RMIRegulatedMotor motorL;
	public static RMIRegulatedMotor motorR;
	private RemoteEV3 ev3;
	public static boolean running = true;
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

			//motorL.setSpeed((int)motorL.getMaxSpeed());
			//motorR.setSpeed((int)motorR.getMaxSpeed());
			motorL.setSpeed(10);
			motorR.setSpeed(10);
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
	
	public void run(Hub hub, MyoDataCollector dataCollector) throws RemoteException {
    	while (running) {
    		/*totalSteps++;
    		if (totalSteps>200) {
    			running = false;
    			break;
    		}*/
	    	hub.run(1000/10);
	    	System.out.print(dataCollector);
	    	dataCollector.setSpeed();
    		//try {
    		//	Thread.sleep(50);
    		//} catch (InterruptedException e) {
    		//	e.printStackTrace();
    		//}
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
			
			Hub hub = new Hub("net.havlena.myo");
		    System.out.println("Attempting to find a Myo...");
		    Myo myo = hub.waitForMyo(10000);
		    if (myo == null) {
				throw new RuntimeException("Unable to find a Myo!");
			}
		    
		    System.out.println("Connected to a Myo armband!");
		    MyoDataCollector dataCollector = new MyoDataCollector();
		    hub.addListener(dataCollector);
		    myotruck.run(hub, dataCollector);
		    //while (true) {

		    //}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
  
}