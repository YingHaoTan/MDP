package mdp;

import java.awt.Dimension;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import mdp.controllers.MdpWindowController;
import mdp.controllers.explorer.ExplorationBase;
import mdp.controllers.explorer.HugRightExplorationController;
import mdp.controllers.fp.FastestPathBase;
import mdp.controllers.fp.FastestPathController;
import mdp.files.MapFileHandler;
import mdp.graphics.MdpWindow;
import mdp.models.Direction;
import mdp.models.SensorConfiguration;
import mdp.robots.SimulatorRobot;

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
        MdpWindowController controller = new MdpWindowController(window);
        MapFileHandler filehandler = new MapFileHandler();
        SimulatorRobot srobot = new SimulatorRobot(rdim, Direction.UP);
        FastestPathBase fastestpath = new FastestPathController();
        ExplorationBase explorer = new HugRightExplorationController(fastestpath);
        //ExplorationBase explorer = new SnakeExplorationController();
        srobot.install(new SensorConfiguration(Direction.UP, -1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 0, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.UP, 1, 2, 0.75));
        srobot.install(new SensorConfiguration(Direction.LEFT, 1, 3, 0.5));
        srobot.install(new SensorConfiguration(Direction.LEFT, -1, 3, 0.5));
        srobot.install(new SensorConfiguration(Direction.RIGHT, 1, 4, 0.5));
        srobot.install(new SensorConfiguration(Direction.RIGHT, -1, 4, 0.5));

        controller.setMapLoader(filehandler);
        controller.setMapSaver(filehandler);
        controller.setSimulatorRobot(srobot);
        controller.setFastestPathPlanner(new FastestPathController());
        /*
        FastestPath fp = new FastestPath();
        FastestPath fp2 = new FastestPath();
        
        controller.setFastestPath(fp);
        explorer.setFastestPath(fp2);
        */
        controller.setExplorer(explorer);
    }

}
