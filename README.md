myo-truck
=========

Myo Armband controlling Lego Mindstorm EV3 RAC3 TRUCK by hand gestures.

## Video
[![Myo Armband controlling Lego Mindstorm EV3 RAC3 TRUCK by hand gestures](http://img.youtube.com/vi/LsQ_2EhIpoQ/0.jpg)](http://www.youtube.com/watch?v=LsQ_2EhIpoQ)

## How to build the robot
You can follow the official building instructions for EV3 RAC3 TRUCK on [lego site](http://www.lego.com/en-us/mindstorms/build-a-robot/rac3-truck). Then you need to install lejOS on your EV3. It's quite simple and you will find all necesarry information and files [here](http://www.lejos.org/ev3.php). I recommend you to download EV3 Eclipse plugin, which makes it very convenient to control your EV3 from Eclipse environment. Also, if you have the possibility to get NETGEAR Wifi Dongle for your EV3, it will allow you to connect to your EV3 wirelessy. Otherwise you can use Bluetooth or USB connection.

## Run the program
There are two classes in the project. You need to run the `MyoTruck`.
- `MyoTruck` - Main class that handles connection to Myo, EV3 and does the main execution.
- `MyoDataCollector` - This class collects orientation data and listens to gestures from your Myo.

I have also included [myo-java](https://github.com/NicholasAStuart/myo-java) library files within the source code, so you don't have to include it. 

## Control Gestures
- **Raise your hand** -> speed increase
- **Lower your hand** -> speed decrease/switch to reverse gear
- **Fist** pose -> put the truck into sleep (stop moving)
- **Fingers spread** pose -> wake the truck up from sleep
- **Thumb to pinky** pose -> close the program

## Used Hardware
- Myo Armband (Developer Kit) - [more info](http://www.havlena.net/en/innovations/introducing-myo-a-gesture-control-armband-that-analyzes-muscle-activity/)
- Lego Mindstorms EV3

## Software Dependencies
- [Myo Connect](https://developer.thalmic.com/downloads) (version 0.6.0)
- [myo-java](https://github.com/NicholasAStuart/myo-java) 
- [lejOS EV3 Runtime Library](http://sourceforge.net/projects/lejos/files/lejos-EV3/0.8.1-beta/) (version 0.8.1)

## Architecture Diagram
![Myo Armband controlling Lego Mindstorm EV3 RAC3 TRUCK by hand gestures](https://raw.githubusercontent.com/matoushavlena/myo-truck/master/myo-truck-architecture.png)

Following is one of the possible architectures I picked for this project. Although there are other options how to connect Myo to EV3:
- Through PC, Mac, Android or iOS
- Install Myo drivers directly on EV3 brick with leoJS
- more...

## Supported Platforms
- Mac, Windows, Linux (possibly Android and iOS, but some modifications would have to be made)

