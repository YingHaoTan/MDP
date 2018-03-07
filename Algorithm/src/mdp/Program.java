package mdp;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Queue;

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
import mdp.robots.CalibrationSpecificationBuilder;
import mdp.robots.PhysicalRobot;
import mdp.robots.SimulatorRobot;
import mdp.tcp.AndroidUpdate;
import mdp.tcp.ArduinoMessage;
import mdp.tcp.MDPTCPConnector;
import mdp.tcp.StatusMessage;

public class Program {
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        
        Queue<ArduinoMessage> outgoingArduinoQueue = new LinkedList<>();
        Queue<AndroidUpdate> outgoingAndroidQueue = new LinkedList<>();
        MDPTCPConnector mdpTCPConnector = new MDPTCPConnector(outgoingArduinoQueue, outgoingAndroidQueue);
        mdpTCPConnector.startThreads();

        Dimension rdim = new Dimension(3, 3);
        
        MdpWindow window = new MdpWindow("Mdp Algorithm Simulator", new Dimension(15, 20), new Dimension(3, 3));
        MdpWindowController wcontroller = new MdpWindowController(window);
        XController xcontroller = new XController(window.getMap().getMapState(), mdpTCPConnector.getAndroidInstructionListenerList(), outgoingAndroidQueue, mdpTCPConnector.getOutgoingSemaphore());
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
        
        // SimulatorRobot
        SimulatorRobot srobot = new SimulatorRobot(rdim, Direction.RIGHT);
        srobot.install(new SensorConfiguration(Direction.UP, -1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 0, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.RIGHT, -1, 2, 0.5));
        srobot.install(new SensorConfiguration(Direction.RIGHT, 1, 2, 0.5));
        srobot.install(new SensorConfiguration(Direction.LEFT, 0, 4, 0.5));      
        
        srobot.addCalibrationSpecification(new CalibrationSpecificationBuilder()
        		.add(Direction.RIGHT)
        		.add(Direction.UP)
        		.setCalibrationType(RobotAction.CAL_CORNER)
        		.build());
        
        wcontroller.setSimulatorRobot(srobot);
        xcontroller.setSimulatorRobot(srobot);
        
        // PhysicalRobot
        PhysicalRobot probot = new PhysicalRobot(rdim, Direction.RIGHT, mdpTCPConnector.getArduinoUpdateListenerList(), outgoingArduinoQueue, mdpTCPConnector.getOutgoingSemaphore());
        probot.install(new SensorConfiguration(Direction.UP, -1, 2, 0.75));
        probot.install(new SensorConfiguration(Direction.UP, 0, 2, 0.75));
        probot.install(new SensorConfiguration(Direction.UP, 1, 2, 0.75));
        probot.install(new SensorConfiguration(Direction.RIGHT, -1, 2, 0.5));
        probot.install(new SensorConfiguration(Direction.RIGHT, 1, 2, 0.5));
        probot.install(new SensorConfiguration(Direction.LEFT, 0, 4, 0.5));
        
        probot.addCalibrationSpecification(new CalibrationSpecificationBuilder()
        		.add(Direction.RIGHT)
        		.add(Direction.UP)
        		.setCalibrationType(RobotAction.CAL_CORNER)
        		.build());
        
        wcontroller.setPhysicalRobot(probot);
        xcontroller.setPhysicalRobot(probot);
    }

}
