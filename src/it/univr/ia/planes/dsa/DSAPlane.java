/*
 * Copyright (c) 2013, Andrea Jeradi, Francesco Donato
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
package it.univr.ia.planes.dsa;

import es.csic.iiia.planes.AbstractPlane;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a plane that coordinates using DSA
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class DSAPlane extends AbstractPlane {

    private ArrayList<Task> localTasks = new ArrayList<Task>();

    public DSAPlane(Location location) {
        super(location);
    }

    @Override
    public void initialize() {
        addBehavior(new NeighborTracking(this));
        addBehavior(new DSABehavior(this));
        super.initialize();
    }

    @Override
    protected void taskCompleted(Task t) {}

    @Override
    protected void taskAdded(Task t) {
        localTasks.add(t);

        final Task current = getNextTask();
        final double newdist = getLocation().distance(t.getLocation());
        if (current == null || newdist < getLocation().distance(current.getLocation())) {
            setNextTask(t);
        }
    }

    @Override
    protected void taskRemoved(Task t) {
        localTasks.remove(t);
        setNextTask(findClosest(getLocation(), localTasks));
    }

    private static Task findClosest(Location location, List<Task> candidates) {
        double mind = Double.MAX_VALUE;
        Task result = null;
        for (Task t : candidates) {
            final double d = location.distance(t.getLocation());
            if (d < mind) {
                result = t;
                mind = d;
            }
        }
        return result;
    }
}