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
 * SSI Allocation where planes' plans can only be extended by adding tasks at the end.
 * <p/>
 * This allocation strategy should be more stable than other SSI variants, because the planes will
 * always build a plan where each next task is the one nearest to the preceding one, starting
 * at the current plane location.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class IncrementalSSIAllocation extends AbstractSSIAllocation {
    private static final Logger LOG = Logger.getLogger(IncrementalSSIAllocation.class.getName());

    @Override
    public String getName() {
        return "hungarian";
    }

    @Override
    public String getDescription() {
        return "Allocates tasks using the Hungarian method";
    }

    @Override
    protected BestPosition bestPosition(OmniscientPlane p, PathCost path, Task t) {
        final Location tl = t.getLocation();
        Location last = p.getLocation();
        if (!path.path.isEmpty()) {
            last = path.path.get(path.path.size()-1).getLocation();
        }

        int best    = path.path.size();
        double cost = last.distance(tl);

        LOG.log(Level.FINEST, "Task {0} extra cost: {0}", new Object[]{t, cost});
        return new BestPosition(best, cost, path.cost + cost);
    }

}