/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.auctions;

import es.csic.iiia.planes.AbstractPlane;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.NeighborTracking;
import es.csic.iiia.planes.messaging.MessagingAgent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AuctionPlane extends AbstractPlane 
    implements NeighborTracking.NeighborTrackingListener
{
    
    private ArrayList<Task> localTasks = new ArrayList<Task>();
    
    @Override
    public void initialize() {
        addBehavior(new NeighborTracking(this));
    }

    public AuctionPlane(Location location) {
        super(location);
    }

    @Override
    public List<Location> getPlannedLocations() {
        List<Location> locations = new ArrayList<Location>();
        for (Task t : localTasks) {
            locations.add(t.getLocation());
        }
        return locations;
    }

    @Override
    public void neighborDetected(MessagingAgent neighbor) {
        //System.err.println("Me " + this + " new neighbor: " + neighbor);
    }

    @Override
    public void neighborLost(MessagingAgent neighbor) {
        //System.err.println("Me " + this + " lost neighbor: " + neighbor);
    }

    @Override
    protected void taskCompleted(Task t) {
        localTasks.remove(t);
        if (!localTasks.isEmpty()) {
            setNextTask(localTasks.get(0));
        }
    }

    @Override
    protected void taskAdded(Task t) {
        smartAddTask(t);
        setNextTask(localTasks.get(0));
    }
    
//    @Override
//    protected void taskCompleted(Task t) {
//        localTasks.remove(t);
//        if (!localTasks.isEmpty()) {
//            setNextTask(findClosest(getLocation(), localTasks));
//        }
//    }
//
//    @Override
//    protected void taskAdded(Task t) {
//        localTasks.add(t);
//        setNextTask(findClosest(getLocation(), localTasks));
//    }
    
    private double smartAddTask(Task t) {
        double currentCost = getCost(localTasks);
        localTasks.add(t);
        recomputeTasks();
        return getCost(localTasks) - currentCost;
    }
    
    private void recomputeTasks() {
        List<Task> pending = new ArrayList<Task>(localTasks);
        localTasks.clear();
        Location current = getLocation();
        while (!pending.isEmpty()) {
            Task next = findClosest(current, pending);
            pending.remove(next);
            localTasks.add(next);
            current = next.getLocation();
        }
    }
    
    private double getCost(List<Task> ts) {
        if (ts.isEmpty()) {
            return 0;
        }
        
        double cost = getLocation().distance(ts.get(0).getLocation());
        for (int i=0, len=ts.size()-1; i<len; i++) {
            cost += ts.get(i).getLocation().distance(ts.get(i+1).getLocation());
        }
        return cost;
    }
    
    private Task findClosest(Location location, List<Task> candidates) {
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
