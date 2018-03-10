package mdp;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import mdp.controllers.MdpWindowController;
import mdp.controllers.XController;
import mdp.controllers.explorer.ExplorationBase;
import mdp.controllers.explorer.HugRightExplorationController;
import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.astar.AStarFastestPath;
import mdp.controllers.fp.astar.BasicPathSpecification;
import mdp.controllers.fp.astar.WaypointPathSpecification;
import mdp.files.MapFileHandler;
import mdp.graphics.MdpWindow;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;
import mdp.robots.CalibrationSpecification;
import mdp.robots.PhysicalRobot;
import mdp.robots.SimulatorRobot;
import mdp.tcp.AndroidUpdate;
import mdp.tcp.ArduinoMessage;
import mdp.tcp.MDPTCPConnector;

public class Program {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        Dimension rdim = new Dimension(3, 3);
        
        MdpWindow window = new MdpWindow("Mdp Algorithm Simulator", new Dimension(15, 20), new Dimension(3, 3));
        MdpWindowController wcontroller = new MdpWindowController(window);
        XController xcontroller = new XController(window.getMap().getMapState());
        MapFileHandler filehandler = new MapFileHandler();
        ExplorationBase explorer = new HugRightExplorationController(new AStarFastestPath(new BasicPathSpecification()));
        FastestPathBase fastestpath = new AStarFastestPath(new WaypointPathSpecification());
        
        wcontroller.setMapLoader(filehandler);
        wcontroller.setMapSaver(filehandler);
        
        wcontroller.setFastestPathPlanner(fastestpath);
        wcontroller.setExplorer(explorer);
        
        wcontroller.setXController(xcontroller);
        
        xcontroller.setFastestPathPlanner(fastestpath);
        xcontroller.setExplorer(explorer);
        xcontroller.setWindowController(wcontroller);
        
        SensorConfiguration front1 = new SensorConfiguration(Direction.UP, -1, 0, 3, 0.8);
        SensorConfiguration front2 = new SensorConfiguration(Direction.UP, 0, 0, 3, 0.8);
        SensorConfiguration front3 = new SensorConfiguration(Direction.UP, 1, 0, 3, 0.8);
        SensorConfiguration right1 = new SensorConfiguration(Direction.RIGHT, -1, 0, 3, 0.8);
        SensorConfiguration right2 = new SensorConfiguration(Direction.RIGHT, 1, 0, 3, 0.8);
        SensorConfiguration left1 = new SensorConfiguration(Direction.LEFT, 1, 0, 5, 0.8);
        
        // SimulatorRobot
        SimulatorRobot srobot = new SimulatorRobot(rdim, Direction.RIGHT);
        srobot.install(front1);
        srobot.install(front2);
        srobot.install(front3);
        srobot.install(right1);
        srobot.install(right2);
        srobot.install(left1);      
        
        srobot.addCalibrationSpecification(new CalibrationSpecification(RobotAction.CAL_CORNER, front1, front2, front3, right1, right2));
        srobot.addCalibrationSpecification(new CalibrationSpecification(RobotAction.CAL_SIDE, right1, right2));
        
        wcontroller.setSimulatorRobot(srobot);
        xcontroller.setSimulatorRobot(srobot);
        
        Queue<ArduinoMessage> outgoingArduinoQueue = new ConcurrentLinkedQueue<>();
        Queue<AndroidUpdate> outgoingAndroidQueue = new ConcurrentLinkedQueue<>();
        try {
	        MDPTCPConnector mdpTCPConnector = new MDPTCPConnector(outgoingArduinoQueue, outgoingAndroidQueue);
	        mdpTCPConnector.startThreads();
	        
	        // PhysicalRobot
	        PhysicalRobot probot = new PhysicalRobot(rdim, Direction.RIGHT, mdpTCPConnector.getArduinoUpdateListenerList(), outgoingArduinoQueue, outgoingAndroidQueue, mdpTCPConnector.getOutgoingSemaphore());
	        probot.install(front1);
	        probot.install(front2);
	        probot.install(front3);
	        probot.install(right1);
	        probot.install(right2);
	        probot.install(left1);    
	        
	        probot.addCalibrationSpecification(new CalibrationSpecification(RobotAction.CAL_CORNER, front1, front2, front3, right1, right2));
	        probot.addCalibrationSpecification(new CalibrationSpecification(RobotAction.CAL_SIDE, right1, right2));
	        
	        wcontroller.setPhysicalRobot(probot);
	        xcontroller.setPhysicalRobot(probot);
	        
	        xcontroller.initializeCommunication(mdpTCPConnector.getAndroidInstructionListenerList(), outgoingAndroidQueue, mdpTCPConnector.getOutgoingSemaphore());
        } catch(IOException e) {
        	Logger.getAnonymousLogger().log(Level.SEVERE, null, e);
        }
    }

}
