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
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.MessagingAgent;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class IndependentAuctionAllocation extends AbstractAllocationStrategy {

    @Override
    public String getName() {
        return "auction";
    }

    @Override
    public String getDescription() {
        return "Allocates tasks using parallel single-item auctions.";
    }

    @Override
    public void allocate(
        World world,
        OmniscientPlane[] planes,
        TreeMap<MessagingAgent, Set<Task>> visibilityMap,
        TreeMap<OmniscientPlane, Task> assignmentMap,
        TreeMap<Task, OmniscientPlane> reverseMap)
    {
        for (Task t : world.getTasks()) {
            final Location tl = t.getLocation();
            OmniscientPlane best = null;
            double mind = Double.MAX_VALUE;

            for (OmniscientPlane p : planes) {
                if (!visibilityMap.get(p).contains(t)) {
                    continue;
                }

                final Location pl = p.getLocation();
                double d = tl.distance(pl);
                if (d < mind) {
                    best = p;
                    mind = d;
                }
            }

            if (best != null) {
                if (  assignmentMap.containsKey(best)
                   && distance(best, assignmentMap.get(best)) <= mind )
                {
                    continue;
                }
                if (reverseMap.containsKey(t)) {
                    OmniscientPlane other = reverseMap.get(t);
                    if (assignmentMap.get(other) == t) {
                        assignmentMap.remove(other);
                    }
                }
                assignmentMap.put(best, t);
                reverseMap.put(t, best);
            }
        }
    }

}
