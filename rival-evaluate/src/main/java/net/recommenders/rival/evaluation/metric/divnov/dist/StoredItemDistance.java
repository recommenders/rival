/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.rival.evaluation.metric.divnov.dist;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author saul
 */
public class StoredItemDistance<I> implements ItemDistance<I> {

    private final Map<I, Map<I, Double>> dists;

    public StoredItemDistance(Iterable<I> items, ItemDistance<I> dist) {
        dists = new HashMap<I, Map<I, Double>>();
        
        for (I i : items) {
            Map<I, Double> iDists = new HashMap<I, Double>();
            for (I j : items) {
                iDists.put(j, dist.dist(i, j));
            }
            dists.put(i, iDists);
        }
    }

    @Override
    public double dist(I i, I j) {
        return dists.get(i).get(j);
    }

}
