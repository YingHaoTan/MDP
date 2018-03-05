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
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import mdp.models.Direction;
import mdp.models.MapState;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;
import mdp.tcp.ArduinoInstruction;
import mdp.tcp.ArduinoUpdate;
import mdp.tcp.StatusMessage;
import java.util.Date;
import mdp.tcp.ArduinoMessage;
import mdp.tcp.ArduinoStream;

/**
 *
 * @author JINGYANG
 */
public class PhysicalRobot extends RobotBase {

    private Queue<ArduinoUpdate> incomingArduinoQueue;
    private Queue<ArduinoMessage> outgoingArduinoQueue;
    private Queue<StatusMessage> outgoingAndroidQueue;
    private Map<SensorConfiguration, Integer> readings = new HashMap<>();
    private Queue<PhysicalRobot.NotifyTask> taskqueue;
    private long timerDelay = (long) 10;
    // Just to store robot supposed position

    public PhysicalRobot(Dimension dimension, Direction orientation, Queue<ArduinoUpdate> incomingArduinoQueue, Queue<ArduinoMessage> outgoingArduinoQueue, Queue<StatusMessage> outgoingAndroidQueue) {
        super(dimension, orientation);
        this.taskqueue = new LinkedList<>();
        this.incomingArduinoQueue = incomingArduinoQueue;
        this.outgoingArduinoQueue = outgoingArduinoQueue;
        this.outgoingAndroidQueue = outgoingAndroidQueue;
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
    	
        // Tells TCP to send START command
        ArduinoInstruction initMessage = new ArduinoInstruction(RobotAction.START, false);

        outgoingArduinoQueue.add(initMessage);

        //Waits for TCP's reply   
        while (true) {
            //System.out.println("size from physical robot=" + incomingArduinoQueue.size());
            if (!incomingArduinoQueue.isEmpty()) {
                ArduinoUpdate incomingArduinoUpdate = incomingArduinoQueue.remove();
                setArduinoSensorReadings(incomingArduinoUpdate);

                break;
            }
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(PhysicalRobot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*System.out.println(incomingArduinoUpdate.getFront1());
            System.out.println(incomingArduinoUpdate.getFront2());
            System.out.println(incomingArduinoUpdate.getFront3());
            System.out.println(incomingArduinoUpdate.getRight1());
            System.out.println(incomingArduinoUpdate.getRight2());
            System.out.println(incomingArduinoUpdate.getLeft1());*/
    }

    @Override
    protected void dispatchMovement(Direction mapdirection, RobotAction... actions) {
        for (RobotAction action : actions) {
            // How do I know from here whether I have obstacleInFront or not.. I'm putting this as false from here
            // 1) The MapState containing the scanned obstacles is inside ExplorationBase.java
            System.out.println("==========================");
            System.out.println(action + "- Before sending message");
            Date date = new Date();
            System.out.println("Put-into-queue:" + date.toString());
            ArduinoInstruction arduinoInstruction = new ArduinoInstruction(action, false);
            outgoingArduinoQueue.add(arduinoInstruction);

            while (true) {
                if (!incomingArduinoQueue.isEmpty()) {
                    ArduinoUpdate incomingArduinoUpdate = incomingArduinoQueue.remove();
                    setArduinoSensorReadings(incomingArduinoUpdate);
                    date = new Date();
                    System.out.println("Take-from-queue:" + date.toString());

                    System.out.println(action + "- Action received");
                    break;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PhysicalRobot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

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
        
        NotifyTask task = new NotifyTask(mapdirection, actions);
        taskqueue.offer(task);
        if (taskqueue.size() == 1) {
            this.getScheduler().schedule(task, timerDelay);
        }
    }
    
    @Override
	protected void dispatchCalibration(RobotAction action) {
		ArduinoInstruction arduinoInstruction = new ArduinoInstruction(action, false);
		outgoingArduinoQueue.add(arduinoInstruction);
	}

    @Override
    protected void moveRobotStream(List<RobotAction> actions, List<Direction> orientations) {
        int orientationIndex = 0;

        // Crafts message
        ArduinoStream streamMessage = new ArduinoStream(actions);
        outgoingArduinoQueue.add(streamMessage);

        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == RobotAction.TURN_LEFT || actions.get(i) == RobotAction.TURN_RIGHT) {
                NotifyTask task = new NotifyTask(null, new RobotAction[]{actions.get(i)});
                taskqueue.offer(task);
                if (taskqueue.size() == 1) {
                    this.getScheduler().schedule(task, timerDelay);
                }
            } else {
                NotifyTask task = new NotifyTask(orientations.get(orientationIndex++), new RobotAction[]{actions.get(i)});
                taskqueue.offer(task);
                if (taskqueue.size() == 1) {
                    this.getScheduler().schedule(task, timerDelay);
                }
            }
        }
    }

    public void stop() {
        //send stop message
        ArduinoInstruction arduinoInstruction = new ArduinoInstruction(RobotAction.STOP, false);
        outgoingArduinoQueue.add(arduinoInstruction);

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

    /**
     * NotifyTask is a TimerTask that notifies registered RobotActionListener on
     * a specific robot action sequence completion
     *
     * @author Ying Hao
     */
    private class NotifyTask extends TimerTask {

        private Direction mapdirection;
        private RobotAction[] actions;

        public NotifyTask(Direction mapdirection, RobotAction[] actions) {
            this.mapdirection = mapdirection;
            this.actions = actions;
        }

        @Override
        public void run() {
            PhysicalRobot.this.notify(mapdirection, actions);
            PhysicalRobot.this.taskqueue.poll();

            if (PhysicalRobot.this.taskqueue.size() > 0) {
                PhysicalRobot.this.getScheduler().schedule(PhysicalRobot.this.taskqueue.peek(), timerDelay);
            }
        }

    }

}
