package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.map;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.CellState;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.Direction;

/**
 * Created by ernes on 10/2/2018.
 */

public class Maze {

    // maze dimension
    public final static int MAZE_COLS = 15;
    public final static int MAZE_ROWS = 20;

    // grid types
    public final static int UNEXPLORED_GRID = 0;
    public final static int NORMAL_GRID = 1;
    public final static int OBSTACLE_GRID = 2;
    public final static int WAYPOINT_GRID = 3;
    public final static int ROBOT_GRID = 4;
    public final static int ROBOT_DIRECTION_GRID = 5;

    //maze info
    private int[] maze;
    private int robot_x;
    private int robot_y;
    private Direction robot_d;
    private int waypoint_x;
    private int waypoint_y ;

    public Maze(){
        maze = new int[MAZE_COLS*MAZE_ROWS];
        waypoint_x = waypoint_y = robot_x = robot_y = -1;
    }

    public void updateWaypoint(int x , int y){
        waypoint_x = x;
        waypoint_y = y;

    }

    public void removeWaypoint(){
        maze[waypoint_x+waypoint_y*MAZE_COLS] = UNEXPLORED_GRID;
        waypoint_x = -1;
        waypoint_y = -1;
    }

    public int[] getWaypoint(){
        return new int[]{waypoint_x,waypoint_y};
    }

    public void updateGrid(int x, int y, CellState cs){
        if(waypoint_x == x && waypoint_y == y)
            return;
        if(cs == CellState.NORMAL){
            maze[x+y*MAZE_COLS] = NORMAL_GRID;
        }else if(cs == CellState.OBSTACLE){
            maze[x+y*MAZE_COLS] = OBSTACLE_GRID;
        }else{
            maze[x+y*MAZE_COLS] = UNEXPLORED_GRID;
        }
    }

    public void updateRobot(int x, int y, Direction direction){
        robot_x = x;
        robot_y = y;
        robot_d = direction;
    }

    public void removeRobot(){
        robot_x = -1;
        robot_y = -1;
    }

    // to take in MDF files from algo in the future
    public void updateMaze(){

    }

    public CellState getState(int x , int y){
        if(x >= robot_x && x <=(robot_x+2) && y >= robot_y && y <= (robot_y+2)){
            if(robot_d == Direction.UP && x == robot_x+1 && y == robot_y+2){
                return CellState.ROBOT_HEAD;
            }else if(robot_d == Direction.DOWN && x == robot_x+1 && y == robot_y){
                return CellState.ROBOT_HEAD;
            }else if (robot_d == Direction.LEFT && x == robot_x && y == robot_y+1){
                return CellState.ROBOT_HEAD;
            }else if (robot_d == Direction.RIGHT && x == robot_x+2 && y == robot_y+1){
                return CellState.ROBOT_HEAD;
            }
            return CellState.ROBOT;
        }
        if(maze[x+y*MAZE_COLS] == NORMAL_GRID){
            return CellState.NORMAL;
        }else if(maze[x+y*MAZE_COLS] == OBSTACLE_GRID){
            return CellState.OBSTACLE;
        }else if(maze[x+y*MAZE_COLS] == WAYPOINT_GRID){
            return CellState.WAYPOINT;
        }else{
            return CellState.UNEXPLORED;
        }
    }
}