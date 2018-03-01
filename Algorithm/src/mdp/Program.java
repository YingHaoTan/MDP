package mdp;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

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
import mdp.models.SensorConfiguration;
import mdp.robots.SimulatorRobot;
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

        Dimension rdim = new Dimension(3, 3);
        
        MdpWindow window = new MdpWindow("Mdp Algorithm Simulator", new Dimension(15, 20), new Dimension(3, 3));
        MdpWindowController wcontroller = new MdpWindowController(window);
        XController xcontroller = new XController(window.getMap().getMapState());
        
        MapFileHandler filehandler = new MapFileHandler();
        SimulatorRobot srobot = new SimulatorRobot(rdim, Direction.UP);
        ExplorationBase explorer = new HugRightExplorationController(new AStarFastestPath(new BasicPathSpecification()));
        FastestPathBase fastestpath = new AStarFastestPath(new WaypointPathSpecification());
        
        srobot.install(new SensorConfiguration(Direction.UP, -1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 0, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.LEFT, 1, 3, 0.5));
        srobot.install(new SensorConfiguration(Direction.RIGHT, 1, 2, 0.5));
        srobot.install(new SensorConfiguration(Direction.RIGHT, -1, 2, 0.5));
        
        xcontroller.setSimulatorRobot(srobot);
        xcontroller.setFastestPathPlanner(fastestpath);
        xcontroller.setExplorer(explorer);
        xcontroller.setWindowController(wcontroller);
        
        wcontroller.setMapLoader(filehandler);
        wcontroller.setMapSaver(filehandler);
        wcontroller.setSimulatorRobot(srobot);
        wcontroller.setFastestPathPlanner(fastestpath);
        wcontroller.setExplorer(explorer);
        wcontroller.setXController(xcontroller);
        
        SynchronousQueue<StatusMessage> incomingQueue = new SynchronousQueue<>();
        Queue<StatusMessage> outgoingArduinoQueue = new LinkedList<>();
        Queue<StatusMessage> outgoingAndroidQueue = new LinkedList<>();
        
        MDPTCPConnector mdpTCPConnector = new MDPTCPConnector("localhost", 5000, incomingQueue, outgoingArduinoQueue, outgoingAndroidQueue);
        mdpTCPConnector.start();
    }

}
