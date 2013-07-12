/*
 * Copyright (c) 2013, Marc Pujol <mpujol@iiia.csic.es>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.util;

import es.csic.iiia.planes.Positioned;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Holds a path plan (sequence of tasks to service) and its associated cost.
 */
public final class PathPlan {

    private final Positioned start;
    private final List<Positioned> path;
    private double cost;

    /**
     * Build a new path plan starting at the specified position.
     *
     * @param start starting location of this plan.
     */
    public PathPlan(Positioned start) {
        this.start = start;
        this.path = new ArrayList<Positioned>();
        this.cost = 0;
    }

    /**
     * Build a new path plan starting at the specified position and that goes
     * through all the given positions.
     */
    public PathPlan(Positioned start, Collection<Positioned> positions) {
        this(start);
        for (Positioned position : positions) {
            add(position);
        }
    }
    
    /**
     * Build a copy of the given path plan.
     * @param other
     */
    public PathPlan(PathPlan other) {
        start = other.start;
        path = new ArrayList<Positioned>(other.path);
        cost = other.cost;
    }

    /**
     * Adds a task to visit during this plan.
     *
     * @param t task to add to the plan.
     */
    public void add(Positioned position) {
        final int n = path.size();

        // Evaluate insertion at the beginning
        int index = 0;
        double bestCost = cost + start.distance(position);
        if (n > 0) {
            final Positioned p = path.get(0);
            bestCost -= start.distance(p);
            bestCost += position.distance(p);
        }

        // Evaluate insertion after the i'th (0-indexed) task in the path
        for (int i = 0; i < n - 1; i++) {
            final Positioned pl = path.get(i);
            final Positioned nl = path.get(i + 1);
            double newCost = cost
                    - pl.distance(nl) // No longer going from pl to nl
                    + pl.distance(position) // Now going from pl to the task
                    + position.distance(nl); // And from the task to nl
            if (newCost < bestCost) {
                index = i + 1;
                bestCost = newCost;
            }
        }

        // Evaluate insertion at the end
        if (n > 0) {
            double newCost = cost + path.get(n - 1).distance(position);
            if (newCost < bestCost) {
                index = n;
                bestCost = newCost;
            }
        }

        // Finally insert the task
        path.add(index, position);
        cost = bestCost;
    }

    /**
     * Get the total cost of this plan.
     * @return cost of this plan.
     */
    public double getCost() {
        return cost;
    }

    /**
     * Get the cost of reaching the given position while following this plan.
     * @param position position to reach
     * @return cost of getting to that position
     */
    public double getCostTo(Positioned position) {
        final int n = path.size();
        if (n == 0) {
            throw new NotInPathException(position, path);
        }

        double costTo = start.distance(path.get(0));
        for (int i=0; i<n; i++) {
            final Positioned e = path.get(i);

            if (position.equals(e)) {
                break;
            }

            if (i+1 == n) { // No elements remaining!
                throw new NotInPathException(position, path);
            }
            
            costTo += e.distance(path.get(i+1));
        }

        return costTo;
    }

    /**
     * Get the plan computed to visit all the given locations.
     * @return computed plan.
     */
    public List<Positioned> getPlan() {
        return path;
    }

    /** Exception thrown when a requested position is not found in the plan. */
    private class NotInPathException extends RuntimeException{
        public NotInPathException(Positioned position, List<Positioned> path) {
            super("Element " + position.toString() + " not in path " +
                    Arrays.deepToString(path.toArray()));
        }
    };
}
