/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.models;

import java.awt.Point;

/**
 * Robot point system
 * If neighbour point is null, it means that you cannot move to that point from the current point
 * @author JINGYANG
 */
public class ExplorationNode {
    private Point up;
    private Point down;
    private Point left;
    private Point right;
    private boolean traversed;
    
    public ExplorationNode(){
        up = null;
        down = null;
        left = null;
        right = null;
        traversed = false;
    }

    public Point getUp() {
        return up;
    }

    public void setUp(Point up) {
        this.up = up;
    }

    public Point getDown() {
        return down;
    }

    public void setDown(Point down) {
        this.down = down;
    }

    public Point getLeft() {
        return left;
    }

    public void setLeft(Point left) {
        this.left = left;
    }

    public Point getRight() {
        return right;
    }

    public void setRight(Point right) {
        this.right = right;
    }
    
    public Point getNeighbour(Direction direction){
        switch(direction){
            case UP:
                return getUp();
            case DOWN:
                return getDown();
            case LEFT:
                return getLeft();
            case RIGHT:
                return getRight();
        }
        System.out.println("Shouldn't reach here at all");
        return null;
    }
    
    public void setNeighbour(Direction direction, Point p){
        switch(direction){
            case UP:
                setUp(p);
                break;
            case DOWN:
                setDown(p);
                break;
            case LEFT:
                setLeft(p);
                break;
            case RIGHT:
                setRight(p);
                break;
        }
    }
    
    public void traversed(){
        this.traversed = true;
    }
    
    public boolean isTraversed(){
        return traversed;
    }
}
