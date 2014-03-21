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
import es.csic.iiia.planes.Task;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SSI Allocation where a task can be inserted anywhere in the current plan.
 * <p/>
 * In this SSI variant, a task is introduced at the point in the plan that minimizes the path cost.
 * This mechanism is theoretically strong, since it is proven that using an insertion heuristic
 * such as this one is guaranteed to produce plans that are no worse than 2x the optimal one.
 * <p/>
 * However, in a dynamic system such as this one, the strategy may produce inconsistent plane
 * behavior. Because farther tasks can be inserted before nearest ones in the plan, the planes
 * sometimes apply large plan changes because of a minor diferrence in very future tasks. This
 * can produce back-and-forth behaviors where the plane is constantly changing between plans
 * without making any real progress.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SSIAllocation extends AbstractSSIAllocation {
    private static final Logger LOG = Logger.getLogger(SSIAllocation.class.getName());

    @Override
    public String getName() {
        return "ssi";
    }

    @Override
    public String getDescription() {
        return "Allocates tasks using Sequential Single-Item auctions";
    }

    @Override
    protected BestPosition bestPosition(OmniscientPlane p, PathCost path, Task t) {
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

}