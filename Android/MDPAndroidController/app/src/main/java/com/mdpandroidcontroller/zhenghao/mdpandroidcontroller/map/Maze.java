package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.map;

import android.text.TextUtils;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.CellState;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.Direction;

import java.math.BigInteger;
import java.util.Collections;

/**
 *
 * Class used to manage the arena.
 * data is stored as a 1d array instead of 2d and managed mathematically
 *
 * More methods are to be completed for integration with other groups
 *
 *
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
    private Direction robot_d = Direction.UP;
    private int waypoint_x;
    private int waypoint_y ;

    public Maze(){
        maze = new int[MAZE_COLS*MAZE_ROWS];
        waypoint_x = waypoint_y = -1;
        robot_x = robot_y = -3;
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
        robot_x = -3;
        robot_y = -3;
    }

    // copied from algo team
    public void updateMaze(String mdf1, String mdf2){
        String mdf1bin = new BigInteger(mdf1, 16).toString(2);
        String mdf2bin = new BigInteger(mdf2, 16).toString(2);

        mdf2bin = TextUtils.join("", Collections.nCopies(mdf2.length() * 4 - mdf2bin.length(), "0")) + mdf2bin;

        int mdf1counter = 0;
        int mdf2counter = 0;
        mdf1bin = mdf1bin.substring(2, mdf1bin.length() - 2);

        for (int y = 0; y < MAZE_ROWS; y++) {
            for (int x = 0; x < MAZE_COLS; x++) {
                if (mdf1bin.substring(mdf1counter, mdf1counter + 1).equals("0")) {
                    updateGrid(x, y, CellState.UNEXPLORED);
                } else {
                    if (mdf2bin.substring(mdf2counter, mdf2counter + 1).equals("1")) {
                        updateGrid(x, y, CellState.OBSTACLE);
                    }
                    else {
                        updateGrid(x, y, CellState.NORMAL);
                    }

                    mdf2counter++;
                }

                mdf1counter++;
            }
        }
    }

    /**
     * This method is for easy conversion to the actual arena GUI
     * Priority on robot first, then waypoint, then arena
     *
     * @param x
     * @param y
     * @return
     */
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
        if(x == waypoint_x && y ==waypoint_y){
            return CellState.WAYPOINT;
        }

        if(maze[x+y*MAZE_COLS] == NORMAL_GRID){
            return CellState.NORMAL;
        }else if(maze[x+y*MAZE_COLS] == OBSTACLE_GRID){
            return CellState.OBSTACLE;
        }else{
            return CellState.UNEXPLORED;
        }
    }
}