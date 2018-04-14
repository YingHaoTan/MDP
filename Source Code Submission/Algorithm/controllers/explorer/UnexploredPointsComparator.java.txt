/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mdp.controllers.explorer;

import java.util.Comparator;

/**
 *
 * @author JINGYANG
 */
public class UnexploredPointsComparator implements Comparator<Integer>{
    private Integer[] distances;
    
    public UnexploredPointsComparator(Integer[] distances)
    {
        this.distances = distances;
    }
    
    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[distances.length];
        for (int i = 0; i < distances.length; i++)
        {
            indexes[i] = i; 
        }
        return indexes;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return distances[o1].compareTo(distances[o2]);
    }
}
