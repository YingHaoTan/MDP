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
        sendArduinoMessage(new ArduinoInstruction(RobotAction.START, null));

        // Block until initialization completes
        while (initializing);
    }

    /**
     * Sets auto update flag
     *
     * @param autoupdate
     */
    public void setAutoUpdate(boolean autoupdate) {
        this.autoupdate = autoupdate;
    }

    /**
     * Checks if this physical robot is auto updating
     *
     * @return
     */
    public boolean isAutoUpdate() {
        return autoupdate;
    }

    @Override
    protected void dispatchMovement(Direction mapdirection, RobotAction calibration, RobotAction... actions) {
        synchronized (commandqueue) {
            commandqueue.add(new Command(Arrays.asList(mapdirection), Arrays.asList(actions), false));
        }

        if (autoupdate) {
            if (mapdirection == null) {
                sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotTurning()));
            } else {
                sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotMoving()));
            }
        }

        for (RobotAction action : actions) {
            //System.out.println("In Physical Robot: " + action);
        	
            sendArduinoMessage(new ArduinoInstruction(action, calibration));
        }

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
    public void dispatchCalibration(RobotAction action) {
        synchronized (commandqueue) {
            commandqueue.add(new Command(null, Arrays.asList(action), false));
        }
        System.out.println("Calibration Data: " + action);
        sendArduinoMessage(new ArduinoInstruction(action, null));
    }

    @Override
    protected void moveRobotStream(List<RobotAction> actions, List<Direction> orientations, RobotAction calibration, boolean discardLastMove) {

        System.out.println("Streaming");
        for (int i = 0; i < actions.size(); i++) {
            System.out.println(actions.get(i));
        }
        
        

        synchronized (commandqueue) {
            commandqueue.add(new Command(orientations, actions, true));
        }

        MapState mstate = getMapState();

        for (Direction mapdirection : orientations) {
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
        
        if(discardLastMove){
            actions.remove(actions.size()-1);
        }
        
        printStreamIntoTestString(actions);

        sendArduinoMessage(new ArduinoStream(actions, calibration));
        Point rpoint = mstate.getRobotPoint();
        sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotPosition(rpoint.x, rpoint.y, getCurrentOrientation())));
    }

    public void stop() {
        // Send stop message
        System.out.println("Exploration completed");
        sendArduinoMessage(new ArduinoInstruction(RobotAction.STOP, null));
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
        boolean calibrated = (arduinoUpdate.getCalibrated() == 0x00) ? false : true;
        /*
        System.out.println(front1);
        System.out.println(front2);
        System.out.println(front3);
        System.out.println(right1);
        System.out.println(right2);
        System.out.println(left1);
        */
        // Can this be optimized, or less hardcoded?
        List<SensorConfiguration> sensors = this.getSensors();
        for (SensorConfiguration sensor : sensors) {
            if(calibrated){
                sensor.setWeight(SensorConfiguration.Weight.CALIBRATED);
            }
            else{
                sensor.setWeight(SensorConfiguration.Weight.NORMAL);
            }
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
                    break;
                case LEFT:

                    readings.put(sensor, left1);

                    break;
                default:
                    break;
            }
        }
    }

    private void handleArduinoUpdate(ArduinoUpdate update) {
        /*System.out.println("front1:" + (int) update.getFront1());
=======
        
        System.out.println("front1:" + (int) update.getFront1());
>>>>>>> stairs-skipping
        System.out.println("front2:" + (int) update.getFront2());
        System.out.println("front3:" + (int) update.getFront3());
        System.out.println("left1:" + (int) update.getLeft1());
        System.out.println("right1:" + (int) update.getRight1());
        System.out.println("right2:" + (int) update.getRight2());
<<<<<<< HEAD
        */
        setArduinoSensorReadings(update);

        if (initializing) {
            initializing = false;
        } else {
            Command command = commandqueue.peek();

            if (command != null) {

                if (command.stream) {
                	synchronized (commandqueue) {
                        commandqueue.poll();
                    }
                	
                    int orientationIndex = 0;

                    for (int i = 0; i < command.actions.size(); i++) {
                        RobotAction action = command.actions.get(i);

                        if (action == RobotAction.TURN_LEFT || action == RobotAction.TURN_RIGHT || action == RobotAction.ABOUT_TURN) {
                            this.notify(null, action);
                        } else {
                            this.notify(command.mapdirections.get(orientationIndex++), action);
                        }
                    }
                } else {
                    command.completedactions++;

                    if (command.isComplete()) {
                        synchronized (commandqueue) {
                            commandqueue.poll();
                        }

                        /*if (command.actions.size() != 1  || (command.actions.get(0) != RobotAction.CAL_CORNER && command.actions.get(0) != RobotAction.CAL_SIDE)){
                            this.notify(command.mapdirections.get(0), command.actions.toArray(new RobotAction[0]));
                        }*/
                        if(command.actions.get(0) != RobotAction.CAL_CORNER && command.actions.get(0) != RobotAction.CAL_SIDE && command.actions.get(0) != RobotAction.CAL_JIEMING)
                            this.notify(command.mapdirections.get(0), command.actions.toArray(new RobotAction[0]));
                        else{
                            this.notify(null, command.actions.toArray(new RobotAction[0]));
                        }
                        if (autoupdate) {
                            MapState mstate = getMapState();
                            Point rpoint = mstate.getRobotPoint();

                            sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotStopped()));
                            sendAndroidUpdate(new AndroidUpdate(androidTranslator.robotPosition(rpoint.x, rpoint.y, getCurrentOrientation())));
                        }
                    }
                }
            }
        }
    }

    private class Command {

        private final List<Direction> mapdirections;
        private final List<RobotAction> actions;
        private final boolean stream;
        private int completedactions;

        public Command(List<Direction> mapdirections, List<RobotAction> actions, boolean stream) {
            this.mapdirections = mapdirections;
            this.actions = actions;
            this.completedactions = 0;
            this.stream = stream;
        }

        public boolean isComplete() {
            return this.completedactions == actions.size();
        }
    }

}
