package net.havlena.myotruck;

import java.rmi.RemoteException;

import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.FirmwareVersion;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.enums.Arm;
import com.thalmic.myo.enums.PoseType;
import com.thalmic.myo.enums.VibrationType;
import com.thalmic.myo.enums.XDirection;

public class MyoDataCollector implements DeviceListener {
    private static final int SCALE = 20;
    private double rollW;
    private double pitchW;
    private double yawW;
    private Pose currentPose;
    private Arm whichArm;

    public MyoDataCollector() {
	rollW = 0;
	pitchW = 0;
	yawW = 0;
	currentPose = new Pose();
    }

    @Override
    public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
	Quaternion normalized = rotation.normalized();

	double roll = Math.atan2(2.0f * (normalized.getW() * normalized.getX() + normalized.getY() * normalized.getZ()), 1.0f - 2.0f * (normalized.getX() * normalized.getX() + normalized.getY() * normalized.getY()));
	double pitch = Math.asin(2.0f * (normalized.getW() * normalized.getY() - normalized.getZ() * normalized.getX()));
	double yaw = Math.atan2(2.0f * (normalized.getW() * normalized.getZ() + normalized.getX() * normalized.getY()), 1.0f - 2.0f * (normalized.getY() * normalized.getY() + normalized.getZ() * normalized.getZ()));

	rollW = ((roll + Math.PI) / (Math.PI * 2.0) * SCALE);
	pitchW = ((pitch + Math.PI / 2.0) / Math.PI * SCALE);
	yawW = ((yaw + Math.PI) / (Math.PI * 2.0) * SCALE);
    }

    @Override
    public void onPose(Myo myo, long timestamp, Pose pose) {
	currentPose = pose;
	if (currentPose.getType() == PoseType.FIST) {
	    myo.vibrate(VibrationType.VIBRATION_MEDIUM);
	    MyoTruck.running = false;
	}
	if (currentPose.getType() == PoseType.FINGERS_SPREAD) {
	    myo.vibrate(VibrationType.VIBRATION_SHORT);
	    MyoTruck.running = true;
	}
    }

    @Override
    public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
	whichArm = arm;
    }

    @Override
    public void onArmUnsync(Myo myo, long timestamp) {
	whichArm = null;
    }

    @Override
    public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
    }

    @Override
    public void onConnect(Myo myo, long timestamp, FirmwareVersion firmwareVersion) {
    }

    @Override
    public void onDisconnect(Myo myo, long timestamp) {
    	MyoTruck.running = true;
    }

    @Override
    public void onPair(Myo myo, long timestamp, FirmwareVersion firmwareVersion) {
    }

    @Override
    public void onUnpair(Myo myo, long timestamp) {
    	MyoTruck.running = true;
    }

    @Override
    public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
    }

    @Override
    public void onRssi(Myo myo, long timestamp, int rssi) {
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder("\r");

	String xDisplay = String.format("[%s%s]", repeatCharacter('*', (int) rollW), repeatCharacter(' ', (int) (SCALE - rollW)));
	String yDisplay = String.format("%1$,.2f", pitchW).concat(String.format("[%s%s]", repeatCharacter('*', (int) pitchW), repeatCharacter(' ', (int) (SCALE - pitchW))));
	String zDisplay = String.format("[%s%s]", repeatCharacter('*', (int) yawW), repeatCharacter(' ', (int) (SCALE - yawW)));

	String armString = null;
	if (whichArm != null) {
	    armString = String.format("[%s]", whichArm == Arm.ARM_LEFT ? "L" : "R");
	} else {
	    armString = String.format("[?]");
	}
	String poseString = null;
	if (currentPose != null) {
	    String poseTypeString = currentPose.getType()
		    .toString();
	    poseString = String.format("[%s%" + (SCALE - poseTypeString.length()) + "s]", poseTypeString, " ");
	} else {
	    poseString = String.format("[%14s]", " ");
	}
	builder.append(xDisplay);
	builder.append(yDisplay);
	builder.append(zDisplay);
	builder.append(armString);
	builder.append(poseString);
	return builder.toString();
    }

    private String repeatCharacter(char character, int numOfTimes) {
	StringBuilder builder = new StringBuilder();
	for (int i = 0; i < numOfTimes; i++) {
	    builder.append(character);
	}
	return builder.toString();
    }
    
    public void setSpeed() {
	    try {
	    	int speed = -400+(int)pitchW*50;
			MyoTruck.motorL.setSpeed(speed);
			if (speed>0) MyoTruck.motorL.backward();
			else MyoTruck.motorL.forward();
			MyoTruck.motorR.setSpeed(speed);
			if (speed>0) MyoTruck.motorR.backward();
			else MyoTruck.motorR.forward();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
