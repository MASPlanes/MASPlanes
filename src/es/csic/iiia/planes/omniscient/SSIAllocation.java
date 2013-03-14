/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.planes.omniscient;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SSIAllocation extends AbstractAllocationStrategy {
    private static final Logger LOG = Logger.getLogger(SSIAllocation.class.getName());

    Map<OmniscientPlane, PathCost> assignments = new TreeMap<OmniscientPlane, PathCost>();

    @Override
    public void allocate(World w,
        OmniscientPlane[] planes,
        TreeMap<MessagingAgent, Set<Task>> visibilityMap,
        TreeMap<OmniscientPlane, Task> assignmentMap,
        TreeMap<Task, OmniscientPlane> reverseMap)
    {
        List<Task> pendingTasks = new ArrayList<Task>(w.getTasks());
        LOG.finer("Tasks to allocate: " + pendingTasks);

        // Initialize the planes paths and first bids
        assignments.clear();
        PriorityQueue<Bid> bids = new PriorityQueue<Bid>(planes.length);
        for (OmniscientPlane p : planes) {
            assignments.put(p, new PathCost(new ArrayList<Task>(),0));
            Bid best = best(p, assignments.get(p), pendingTasks, visibilityMap.get(p));
            if (best != null) {
                LOG.finer("New bid: " + best);
                bids.add(best);
            } else {
                LOG.finer("Plane " + p + " has no bid to make.");
            }
        }

        while (!bids.isEmpty() && !pendingTasks.isEmpty()) {
            Bid best = bids.poll();

            // Older bids may be left in the queue. When we find one of these,
            // we compute the plane's new bid without changing anything else
            if (pendingTasks.contains(best.task)) {
                LOG.finer("Accepted bid: " + best);
                PathCost pcost = assignments.get(best.plane);
                pcost.path.add(best.bp.index, best.task);
                assignments.put(best.plane, new PathCost(pcost.path, best.bp.totalCost));
                pendingTasks.remove(best.task);
            }

            Bid newBid = best(best.plane, assignments.get(best.plane), pendingTasks, visibilityMap.get(best.plane));
            if (newBid != null) {
                LOG.finer("New bid: " + newBid);
                bids.add(newBid);
            }
        }

        // Now set the assignments
        reverseMap.clear();
        for (OmniscientPlane p : planes) {
            List<Task> path = assignments.get(p).path;
            if (!path.isEmpty()) {
                assignmentMap.put(p, path.get(0));
                reverseMap.put(path.get(0), p);
            } else {
                assignmentMap.remove(p);
            }
        }
    }

    private BestPosition bestPosition(OmniscientPlane p, PathCost path, Task t) {
        final Location tl = t.getLocation();

        // Go first?
        double minCost = p.getLocation().distance(tl);
        int best = 0;
        if (!path.path.isEmpty()) {
            minCost += tl.distance(path.path.get(0).getLocation());
            minCost -= p.getLocation().distance(path.path.get(0).getLocation());
        }

        // Go after the i'th (which starts at 0)
        for (int i=0; i<path.path.size()-1; i++) {
            Location prev = path.path.get(i).getLocation();
            Location next = path.path.get(i+1).getLocation();
            double cost = prev.distance(tl)
                        + tl.distance(next)
                        - prev.distance(next);

            if (cost < minCost) {
                minCost = cost;
                best = i+1;
            }
        }

        // Go at the end
        if (!path.path.isEmpty()) {
            double cost = path.path.get(path.path.size()-1).getLocation().distance(tl);
            if (cost < minCost) {
                minCost = cost;
                best = path.path.size();
            }
        }

        LOG.log(Level.FINEST, "Best position for {0} in {1}: {2} ({3})", new Object[]{t, p, best, minCost});
        LOG.log(Level.FINEST, "Current path: {0}", path);
        return new BestPosition(best, minCost, path.cost + minCost);
    }

    @Override
    public List<Location> getPlannedLocations(OmniscientPlane plane) {
        List<Location> locations = new ArrayList<Location>();
        for (Task t : assignments.get(plane).path) {
            locations.add(t.getLocation());
        }
        return locations;
    }



    private Bid best(OmniscientPlane p, PathCost currentPath, List<Task> remaining, Set<Task> visibles) {
        Task best = null;
        BestPosition bestBp = null;
        for (Task t : remaining) {

            if (!visibles.contains(t)) {
                continue;
            }

            BestPosition bp = bestPosition(p, currentPath, t);
            if (bestBp == null || bp.additionalCost < bestBp.additionalCost) {
                best = t;
                bestBp = bp;
            }

        }

        Bid result = null;
        if (best != null) {
            result = new Bid(p, best, bestBp);
        }
        return result;
    }

    class PathCost {
        public final List<Task> path;
        public final double cost;
        public PathCost(List<Task> path, double cost) {
            this.path = path;
            this.cost = cost;
        }
    }

    class BestPosition {
        public final int index;
        public final Double additionalCost;
        public final Double totalCost;
        public BestPosition(int index, double additionalCost, double totalCost) {
            this.index = index;
            this.additionalCost = additionalCost;
            this.totalCost = totalCost;
        }

        @Override
        public String toString() {
            return "BestPosition{" + "index=" + index + ", cost=" + additionalCost + '}';
        }
    }

    class Bid implements Comparable<Bid> {
        public final Task task;
        public final BestPosition bp;
        public final OmniscientPlane plane;

        public Bid(OmniscientPlane plane, Task task, BestPosition bp) {
            this.plane = plane;
            this.task = task;
            this.bp = bp;
        }

        @Override
        public int compareTo(Bid t) {
            return bp.totalCost.compareTo(t.bp.totalCost);
        }

        @Override
        public String toString() {
            return "Bid{" + "task=" + task + ", bp=" + bp + ", plane=" + plane + '}';
        }

    }

}