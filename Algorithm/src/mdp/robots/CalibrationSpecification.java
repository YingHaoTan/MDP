package mdp.robots;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Map;

import mdp.models.CellState;
import mdp.models.Direction;
import mdp.models.RobotAction;
import mdp.models.SensorConfiguration;

/**
 * CalibrationSpecification provides methods to communicate path information
 *
 * @author Ying Hao
 */
public class CalibrationSpecification {

    private RobotAction calibrationType;
    private Map<SensorConfiguration, Integer> sensorDistances;

    public CalibrationSpecification(RobotAction calibrationType, Map<SensorConfiguration, Integer> sensorDistances) {
        this.calibrationType = calibrationType;
        this.sensorDistances = sensorDistances;
    }

    /**
     * Validates if the vicinity of the robot with respect to offsetX and
     * offsetY fulfils the condition for calibration
     *
     * @param robot
     * @param offsetX
     * @param offsetY
     * @param offsetActions
     * @return
     */
    public boolean isInPosition(RobotBase robot, RobotAction... offsetActions) {
        boolean inPosition = true;

        for (SensorConfiguration sensor : sensorDistances.keySet()) {
            Direction direction = robot.getSensorDirection(sensor);
            Dimension rdim = robot.getDimension();
            Point location = robot.getSensorCoordinate(sensor);

            if (offsetActions != null) {
                Point robotloc = robot.getMapState().getRobotPoint();

                for (RobotAction offsetA : offsetActions) {
                    if (offsetA == RobotAction.TURN_RIGHT) {
                        switch (direction) {
                            case UP:
                                direction = Direction.RIGHT;
                                break;
                            case DOWN:
                                direction = Direction.LEFT;
                                break;
                            case LEFT:
                                direction = Direction.UP;
                                break;
                            case RIGHT:
                                direction = Direction.DOWN;
                                break;

                        }
                    } else if (offsetA == RobotAction.TURN_LEFT) {
                        switch (direction) {
                            case UP:
                                direction = Direction.LEFT;
                                break;
                            case DOWN:
                                direction = Direction.RIGHT;
                                break;
                            case LEFT:
                                direction = Direction.DOWN;
                                break;
                            case RIGHT:
                                direction = Direction.UP;
                                break;
                        }
                    } else if (offsetA == RobotAction.ABOUT_TURN) {
                        switch (direction) {
                            case UP:
                                direction = Direction.DOWN;
                                break;
                            case DOWN:
                                direction = Direction.UP;
                                break;
                            case LEFT:
                                direction = Direction.RIGHT;
                                break;
                            case RIGHT:
                                direction = Direction.LEFT;
                                break;
                        }
                    } else if (offsetA == RobotAction.FORWARD) {
                        switch (direction) {
                            case UP:
                                robotloc = new Point(robotloc.x, robotloc.y + 1);
                                break;
                            case DOWN:
                                robotloc = new Point(robotloc.x, robotloc.y - 1);
                                break;
                            case LEFT:
                                robotloc = new Point(robotloc.x - 1, robotloc.y);
                                break;
                            case RIGHT:
                                robotloc = new Point(robotloc.x + 1, robotloc.y);
                                break;
                            default:
                                break;
                        }
                    }

                }

                robotloc = new Point(robotloc.x + (rdim.width / 2), robotloc.y + (rdim.height / 2));
                if (direction == Direction.UP) {
                    location = new Point(robotloc.x + sensor.getCoordinate(), robotloc.y + rdim.height / 2);
                } else if (direction == Direction.DOWN) {
                    location = new Point(robotloc.x - sensor.getCoordinate(), robotloc.y - rdim.height / 2);
                } else if (direction == Direction.LEFT) {
                    location = new Point(robotloc.x - rdim.width / 2, robotloc.y + sensor.getCoordinate());
                } else {
                    location = new Point(robotloc.x + rdim.width / 2, robotloc.y - sensor.getCoordinate());
                }
            }

            switch (direction) {
                case UP:
                    location = new Point(location.x, location.y + sensorDistances.get(sensor));
                    break;
                case DOWN:
                    location = new Point(location.x, location.y - sensorDistances.get(sensor));
                    break;
                case LEFT:
                    location = new Point(location.x - sensorDistances.get(sensor), location.y);
                    break;
                case RIGHT:
                    location = new Point(location.x + sensorDistances.get(sensor), location.y);
                    break;
                default:
                    break;
            }

            CellState state = robot.getMapState().getMapCellState(location);
            inPosition &= state == null || state == CellState.OBSTACLE;
        }

        return inPosition;
    }

    /**
     * Gets the calibration type for this specification instance
     *
     * @return
     */
    public RobotAction getCalibrationType() {
        return calibrationType;
    }

}
