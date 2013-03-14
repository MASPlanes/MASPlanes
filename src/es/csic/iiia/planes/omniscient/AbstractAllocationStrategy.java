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
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractAllocationStrategy implements AllocationStrategy {

    protected Task getNearest(OmniscientPlane p, ArrayList<Task> candidates) {
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

    protected void pick(OmniscientPlane p, Task best,
            TreeMap<OmniscientPlane, Task> assignmentMap,
            TreeMap<Task, OmniscientPlane> reverseMap)
    {
        if (assignmentMap.containsKey(p)) {
            reverseMap.remove(assignmentMap.get(p));
        }
        assignmentMap.put(p, best);
        reverseMap.put(best, p);
    }

    protected double distance(Plane p, Task t) {
        return p.getLocation().distance(t.getLocation());
    }
    protected double distance(Task t, Plane p) {
        return p.getLocation().distance(t.getLocation());
    }

    @Override
    public List<Location> getPlannedLocations(OmniscientPlane plane) {
        return null;
    }



}
