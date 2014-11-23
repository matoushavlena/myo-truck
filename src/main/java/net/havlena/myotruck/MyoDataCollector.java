package net.havlena.myotruck;

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
    public static final int SCALE = 20;
    private double rollW;
    private double pitchW;
    private double yawW;
    private Pose currentPose;
    private Arm whichArm;
    private MyoTruck myotruck;
    private boolean connected = false;

    public MyoDataCollector(MyoTruck myotruck) {
		rollW = 0;
		pitchW = 0;
		yawW = 0;
		currentPose = new Pose();
		this.myotruck = myotruck;
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
		    myotruck.sleep();
		}
		if (currentPose.getType() == PoseType.FINGERS_SPREAD) {
		    myo.vibrate(VibrationType.VIBRATION_SHORT);
		    myotruck.start();
		}
		if (currentPose.getType() == PoseType.THUMB_TO_PINKY) {
		    myo.vibrate(VibrationType.VIBRATION_SHORT);
		    myotruck.stop();
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
    	connected = true;
    }

    @Override
    public void onDisconnect(Myo myo, long timestamp) {
    	connected = false;
    	myotruck.stop();
    }

    @Override
    public void onPair(Myo myo, long timestamp, FirmwareVersion firmwareVersion) {
    }

    @Override
    public void onUnpair(Myo myo, long timestamp) {
    	myotruck.stop();
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
	
		String xDisplay = "STEERING: ".concat(String.format("[%s%s]", repeatCharacter('*', (int) rollW), repeatCharacter(' ', (int) (SCALE - rollW))));
		String yDisplay = " SPEED: ".concat(String.format("[%s%s]", repeatCharacter('*', (int) pitchW), repeatCharacter(' ', (int) (SCALE - pitchW))));
	
		String armString = null;
		if (whichArm != null) {
		    armString = " ARM: ".concat(String.format("[%s]", whichArm == Arm.ARM_LEFT ? "L" : "R"));
		} else {
		    armString = " ARM: ".concat(String.format("[?]"));
		}
		
		String poseString = null;
		if (currentPose != null) {
		    String poseTypeString = currentPose.getType().toString();
		    poseString = " POSE: ".concat(String.format("[%s%" + (SCALE - poseTypeString.length()) + "s]", poseTypeString, " "));
		} else {
		    poseString = " POSE: ".concat(String.format("[%14s]", " "));
		}
		builder.append(xDisplay);
		builder.append(yDisplay);
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
    
    public double getPitch() {
    	if (!connected) return -1;
    	return pitchW;
	}
    
    public double getRoll() {
    	if (!connected) return -1;
    	return rollW;
	}
    
    public boolean isLeftArm() {
    	if (whichArm != null && whichArm == Arm.ARM_RIGHT) return false;
    	return true;
	}
}
