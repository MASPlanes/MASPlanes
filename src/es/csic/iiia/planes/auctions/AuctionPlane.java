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
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a plane that coordinates using auctions.
 * <p/>
 * Aside from the auction coordination, this plane use the "nearest task"
 * strategy.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AuctionPlane extends AbstractPlane {

    public static int AUCTION_EVERY = 100;

    private ArrayList<Task> localTasks = new ArrayList<Task>();

    public AuctionPlane(Location location) {
        super(location);
    }

    @Override
    public void initialize() {
        addBehavior(new NeighborTracking(this));
        addBehavior(new AuctionBehavior(this));
        super.initialize();
    }

    @Override
    public List<Location> getPlannedLocations() {
        replan(getLocation(), localTasks);
        List<Location> locations = new ArrayList<Location>();
        for (Task t : localTasks) {
            locations.add(t.getLocation());
        }
        return locations;
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
        if (!localTasks.isEmpty()) {
            setNextTask(findClosest(getLocation(), localTasks));
        }
    }

    private static double replan(Location origin, List<Task> tasks) {
        List<Task> pending = new ArrayList<Task>(tasks);
        tasks.clear();
        Location current = origin;
        double cost = 0;
        while (!pending.isEmpty()) {
            Task next = findClosest(current, pending);
            cost += current.distance(next.getLocation());
            pending.remove(next);
            tasks.add(next);
            current = next.getLocation();
        }
        return cost;
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

    /**
     * Get the offer of this plane for the given task.
     *
     * @param task to bid for.
     * @return offer.
     */
    protected double getOffer(Task task) {
        final Location pl = getLocation();
        final Location tl = task.getLocation();
        final Location sl = getWorld().getNearestStation(tl).getLocation();

        final double plane2task   = pl.distance(tl);
        final double task2station = tl.distance(sl);

        // Battery required to fulfill the task before recharging
        double reqBattery = (long)((plane2task + task2station) / getSpeed());
        if (this.getBattery() > reqBattery) {
            return plane2task;
        }

        // The plane can't fulfill the task without recharging, so the offer
        // considers charging first
        final Location nsl = getWorld().getNearestStation(pl).getLocation();
        return pl.distance(nsl) + nsl.distance(tl);
    }

}
