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
import es.csic.iiia.planes.Operator;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Omniscient god that sees everything and commands omniscient planes.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class OmniscientGod {

    private World world = null;
    private TreeMap<OmniscientPlane, Set<Task>> visibilityMap = new TreeMap<OmniscientPlane, Set<Task>>();
    private TreeMap<OmniscientPlane, Task> assignmentMap = new TreeMap<OmniscientPlane, Task>();
    private TreeMap<Task, OmniscientPlane> reverseMap = new TreeMap<Task, OmniscientPlane>();
    private boolean[][] planeVisibility;
    private int nplanes;
    private boolean changes = true;

    public OmniscientGod() {

    }

    public void initialize(World w) {
        if (world != null) {
            return;
        }

        this.world = w;
    }

    private boolean checkPlaneVisibility() {
        boolean changed = false;
        int i=0;
        for (Plane p1 : visibilityMap.keySet()) {
            double r = p1.getCommunicationRange();
            int j=0;
            for (Plane p2 : visibilityMap.keySet()) {
                double d = p1.getLocation().distance(p2.getLocation());
                boolean expected = false;
                if (d <= r) {
                    expected = true;
                }
                if (planeVisibility[i][j] != expected) {
                    changed = true;
                }
                planeVisibility[i][j] = expected;
                j++;
            }
            i++;
        }
        return changed;
    }

    private long lastIter = -1;
    public void iter(long i) {
        if (lastIter == i) return;
        if (lastIter == -1) {
            nplanes = world.getPlanes().size();
            for (Plane p : world.getPlanes()) {
                if (p instanceof OmniscientPlane) {
                    visibilityMap.put((OmniscientPlane) p, new TreeSet<Task>());
                }
            }
            planeVisibility = new boolean[nplanes][nplanes];
        }

        if (checkPlaneVisibility() || changes) {

            updateVisibility();
            //assignmentMap.clear();
            //reverseMap.clear();
            for (OmniscientPlane p : visibilityMap.keySet()) {
                assign(p);
            }

            Iterator<OmniscientPlane> it = assignmentMap.keySet().iterator();
            while (it.hasNext()) {
                final OmniscientPlane p = it.next();
                Task t = assignmentMap.get(p);
                if (t == null) {
                    it.remove();
                } else {
                    //System.err.println(p + " : " + t + " D: " + p.getLocation().distance(t.getLocation()));
                }
            }

        }

        lastIter = i;
        changes = false;
    }

    private void pick(OmniscientPlane p, Task best) {
        if (assignmentMap.containsKey(p)) {
            reverseMap.remove(assignmentMap.get(p));
        }
        assignmentMap.put(p, best);
        reverseMap.put(best, p);
    }

    private void assign(OmniscientPlane p) {
        ArrayList<Task> candidates = getCandidateTasks(p);
        while (!candidates.isEmpty()) {
            Task best = getNearest(p, candidates);
            if (reverseMap.containsKey(best) && reverseMap.get(best) != p) {
                OmniscientPlane o = reverseMap.get(best);
                double myd = p.getLocation().distance(best.getLocation());
                double otd = o.getLocation().distance(best.getLocation());
                if (myd < otd) {
                    assignmentMap.remove(o);
                    pick(p, best);
                    assign(o);
                    break;
                } else {
                    candidates.remove(best);
                    continue;
                }
            } else {
                pick(p, best);
                break;
            }
        }
    }

    private Task getNearest(OmniscientPlane p, ArrayList<Task> candidates) {
        double mind = Double.MAX_VALUE;
        Task best = null;
        for (Task t : candidates) {
            double d = p.getLocation().distance(t.getLocation());
            if (d < mind) {
                best = t;
                mind = d;
            }
        }
        return best;
    }

    private ArrayList<Task> getCandidateTasks(OmniscientPlane p) {
        return new ArrayList<Task>(visibilityMap.get(p));
    }

    private ArrayList<OmniscientPlane> getNeighbors(Location from, double range) {
        ArrayList<OmniscientPlane> neighs = new ArrayList<OmniscientPlane>();

        for (OmniscientPlane p : visibilityMap.keySet()) {
            final double d = from.distance(p.getLocation());
            if (d <= range) {
                neighs.add(p);
            }
        }

        return neighs;
    }

    public boolean addTask(Operator o, Task t) {
        boolean added = false;
        for (OmniscientPlane p : getNeighbors(o.getLocation(), o.getCommunicationRange())) {
            visibilityMap.get(p).add(t);
            added = true;
        }
        changes = true;
        return added;
    }

    public void updateVisibility() {
        for (OmniscientPlane p : visibilityMap.keySet()) {
            for (OmniscientPlane p2 : getNeighbors(p.getLocation(), p.getCommunicationRange())) {
                visibilityMap.get(p).addAll(visibilityMap.get(p2));
            }
        }
    }

    public Task getNextTask(OmniscientPlane plane) {
        return assignmentMap.get(plane);
    }

    void taskCompleted(Task t) {
        for (OmniscientPlane p : visibilityMap.keySet()) {
            visibilityMap.get(p).remove(t);
        }
        assignmentMap.remove(reverseMap.get(t));
        reverseMap.remove(t);
        changes = true;
    }

}
