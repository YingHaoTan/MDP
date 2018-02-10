package com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.R;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.map.Maze;
import com.mdpandroidcontroller.zhenghao.mdpandroidcontroller.models.CellState;

/**
 * Created by ernes on 10/2/2018.
 */

public class MazeGridAdapter extends BaseAdapter {
    private Context context;
    private Maze maze;

    public MazeGridAdapter(Context context, Maze maze){
        this.context = context;
        this.maze = maze;
    }

    @Override
    public int getCount() {
        return (Maze.MAZE_COLS*Maze.MAZE_ROWS);
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View temp = new View(context);
        temp.setLayoutParams(new ViewGroup.LayoutParams((viewGroup.getWidth()/Maze.MAZE_COLS),(viewGroup.getWidth()/Maze.MAZE_COLS)));
        CellState state = maze.getState(i % Maze.MAZE_COLS, Maze.MAZE_ROWS-1-(i / Maze.MAZE_COLS) );

        if(state == CellState.NORMAL){
            temp.setBackgroundResource(R.drawable.cell_normal);
        }else if(state == CellState.OBSTACLE){
            temp.setBackgroundResource(R.drawable.cell_obstacle);
        }else if(state == CellState.WAYPOINT){
            temp.setBackgroundResource(R.drawable.cell_waypoint);
        }else if(state == CellState.ROBOT){
            temp.setBackgroundResource(R.drawable.cell_robot);
        }else if(state == CellState.ROBOT_HEAD){
            temp.setBackgroundResource(R.drawable.cell_robot_head);
        }else{
            temp.setBackgroundResource(R.drawable.cell_unexplored);
        }

        return temp;
    }

    public void updateMaze(Maze newMaze){
        this.maze = newMaze;
    }
}
