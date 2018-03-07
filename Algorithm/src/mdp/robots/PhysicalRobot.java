/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;
import mdp.tcp.AndroidCommandsTranslator;
import mdp.tcp.AndroidUpdate;
import mdp.tcp.ArduinoInstruction;
import mdp.tcp.ArduinoUpdate;

import java.util.Arrays;
import mdp.tcp.ArduinoMessage;
import mdp.tcp.ArduinoStream;

/**
 *
 * @author JINGYANG
 */
public class PhysicalRobot extends RobotBase {

    private Queue<ArduinoMessage> outgoingArduinoQueue;
    private Queue<AndroidUpdate> outgoingAndroidQueue;
    private Map<SensorConfiguration, Integer> readings = new HashMap<>();
    private Queue<Command> commandqueue;
    private Semaphore outgoingSemaphore;
    private AndroidCommandsTranslator androidTranslator;
    private volatile boolean initializing;
    private volatile boolean autoupdate;

    public PhysicalRobot(Dimension dimension, Direction orientation, List<Consumer<ArduinoUpdate>> arduinoUpdateListenerList, Queue<ArduinoMessage> outgoingArduinoQueue, Queue<AndroidUpdate> outgoingAndroidQueue, Semaphore outgoingSemaphore) {
        super(dimension, orientation);
        this.commandqueue = new LinkedList<>();
        this.outgoingArduinoQueue = outgoingArduinoQueue;
        this.outgoingAndroidQueue = outgoingAndroidQueue;
        this.outgoingSemaphore = outgoingSemaphore;
        this.androidTranslator = new AndroidCommandsTranslator();
        this.autoupdate = true;
        
        arduinoUpdateListenerList.add(this::handleArduinoUpdate);
    }

    @Override
    public Map<SensorConfiguration, Integer> getSensorReading() {
        // If readings are null, tells TCP to send SCAN command
        // Waits for it then return
        // Else
        // Returns most recently updated readings, MUST ONLY CALL THIS AFTER SENSOR READING IS UPDATED WHICH IS AFTER AFTER MOVING
        return readings;
    }

    // This mstate should match Android's input. Tt's using the simulator's grid for now until integration with Android
    @Override
    public void init(MapState mstate) {
    	super.init(mstate);
    	
    	// Set initialized to false
    	initializing = true;
    	
        // Tells TCP to send START command
        sendArduinoMessage(new ArduinoInstruction(RobotAction.START, false));
        
        // Block until initialization completes
        while(initializing);
    }
    
    /**
     * Sets auto update flag
     * @param autoupdate
     */
    public void setAutoUpdate(boolean autoupdate) {
    	this.autoupdate = autoupdate;
    }
    
    /**
     * Checks if this physical robot is auto updating
     * @return
     */
    public boolean isAutoUpdate() {
    	return autoupdate;
    }

    @Override
    protected void dispatchMovement(Direction mapdirection, RobotAction... actions) {
    	synchronized(commandqueue) {
    		commandqueue.add(new Command(mapdirection, Arrays.asList(actions)));
    	}
    	
    	if(autoupdate) {
        	if(mapdirection == null)
        		sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotTurning()));
        	else
        		sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotMoving()));
        }
    	
        for (RobotAction action : actions)
        	sendArduinoMessage(new ArduinoInstruction(action, false));

        // Update supposed robot location
        MapState mstate = this.getMapState();
        Point location = mstate.getRobotPoint();
        if (mapdirection == Direction.UP) {
            mstate.setRobotPoint(new Point(location.x, location.y + 1));
        } else if (mapdirection == Direction.DOWN) {
            mstate.setRobotPoint(new Point(location.x, location.y - 1));
        } else if (mapdirection == Direction.LEFT) {
            mstate.setRobotPoint(new Point(location.x - 1, location.y));
        } else if (mapdirection == Direction.RIGHT) {
            mstate.setRobotPoint(new Point(location.x + 1, location.y));
        }
    }
    
    @Override
	protected void dispatchCalibration(RobotAction action) {
            sendArduinoMessage(new ArduinoInstruction(action, false));
	}

    @Override
    protected void moveRobotStream(List<RobotAction> actions, List<Direction> orientations) {
        // Crafts message
    	sendArduinoMessage(new ArduinoStream(actions));
    }

    public void stop() {
        // Send stop message
    	sendArduinoMessage(new ArduinoInstruction(RobotAction.STOP, false));
    }
    
    private synchronized void sendArduinoMessage(ArduinoMessage message) {
    	outgoingArduinoQueue.offer(message);
    	outgoingSemaphore.release();
    }
    
    private void sendAndroidUpdate(AndroidUpdate message) {
        outgoingAndroidQueue.offer(message);
        outgoingSemaphore.release();
    }

    private void setArduinoSensorReadings(ArduinoUpdate arduinoUpdate) {
        int front1 = (int) (arduinoUpdate.getFront1());
        int front2 = (int) (arduinoUpdate.getFront2());
        int front3 = (int) (arduinoUpdate.getFront3());
        int right1 = (int) (arduinoUpdate.getRight1());
        int right2 = (int) (arduinoUpdate.getRight2());
        int left1 = (int) (arduinoUpdate.getLeft1());

        // Can this be optimized, or less hardcoded?
        List<SensorConfiguration> sensors = this.getSensors();
        for (SensorConfiguration sensor : sensors) {
            switch (sensor.getDirection()) {
                case UP:
                    switch (sensor.getCoordinate()) {
                        case -1:
                            readings.put(sensor, front1);
                            break;
                        case 0:
                            readings.put(sensor, front2);
                            break;
                        case 1:
                            readings.put(sensor, front3);
                            break;

                    }
                    break;
                case RIGHT:
                    switch (sensor.getCoordinate()) {
                        case -1:
                            readings.put(sensor, right1);
                            break;
                        case 1:
                            readings.put(sensor, right2);
                            break;
                    }
                case LEFT:
                    if (sensor.getCoordinate() == 0) {
                        readings.put(sensor, left1);
                    }
				default:
					break;
            }
        }
    }

    private void handleArduinoUpdate(ArduinoUpdate update) {
        System.out.println("front1:" + (int)update.getFront1());
        System.out.println("front2:" + (int)update.getFront2());
        System.out.println("front3:" + (int)update.getFront3());
        System.out.println("left1:" + (int)update.getLeft1());
        System.out.println("right1:" + (int)update.getRight1());
        System.out.println("right2:" + (int)update.getRight2());
        
    	setArduinoSensorReadings(update);
    	
    	if(initializing) {
    		initializing = false;
    	}
    	else {
    		Command command = commandqueue.peek();
    		
    		if(command != null) {
	    		command.completedactions++;
	    		
	    		if(command.isComplete()) {
	    			synchronized(commandqueue) {
	    				commandqueue.poll();
	    			}
	    			
	    			this.notify(command.mapdirection, command.actions.toArray(new RobotAction[0]));
	    			
	    			if(autoupdate) {
	    				MapState mstate = getMapState();
	    				Point rpoint = mstate.getRobotPoint();
	    				
	    				sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotStopped()));
	    				sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotPosition(rpoint.x, rpoint.y, getCurrentOrientation())));
	    			}
	    		}
    		}
    	}
    }
    
    private class Command {
    	private final Direction mapdirection;
    	private final List<RobotAction> actions;
    	private int completedactions;
    	
    	public Command(Direction mapdirection, List<RobotAction> actions) {
    		this.mapdirection = mapdirection;
    		this.actions = actions;
    		this.completedactions = 0;
    	}
    	
    	public boolean isComplete() {
    		return this.completedactions == actions.size();
    	}
    }

}
