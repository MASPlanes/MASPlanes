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
package es.csic.iiia.planes.maxsum.distributed;

import es.csic.iiia.bms.factors.SelectorFactor;
import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.cli.Configuration;
import es.csic.iiia.planes.maxsum.centralized.CostFactor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Behavior that re-builds the Max-Sum graph to represent the current planes,
 * tasks and their connections.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSUpdateGraphBehavior extends AbstractBehavior<MSPlane> {
    private static final Logger LOG = Logger.getLogger(MSUpdateGraphBehavior.class.getName());

    private NeighborTracking tracker;

    final private MSPlane plane;

    /**
     * Build a new max-sum graph updating behavior.
     *
     * @param plane plane that will exhibit this behavior.
     */
    public MSUpdateGraphBehavior(MSPlane plane) {
        super(plane);
        this.plane = plane;
    }

    /**
     * Get the dependencies of this behavior.
     *
     * @return an array with {@link NeighborTracking} as its only entry.
     */
    @Override
    public Class[] getDependencies() {
        return new Class[]{NeighborTracking.class};
    }

    @Override
    public void initialize() {
        tracker = plane.getBehavior(NeighborTracking.class);
    }

    @Override
    public void beforeMessages() {

    }

    /**
     * Updates the structure over which the max-sum algorithm is running.
     * <p/>
     * The structure is updated according to the neighboring planes (that are
     * guaranteed to still be neighbors after {@link Configuration#msIterations}
     * iterations) and their tasks.
     *
     * @TODO: This function is cheating a bit. We should *not* be able to
     * directly fetch the tasks from other agents. Instead, we should be
     * obtaining that information through the NeighborTracking behavior.
     */
    @Override
    public void afterMessages() {
        final long remainder = getAgent().getWorld().getTime() % getConfiguration().getMsStartEvery();
        if (remainder != 1) {
            return;
        }

        // Cleanup the previous graph
        final CostFactor<FactorID> pf = plane.getPlaneFactor();
        pf.clearNeighbors();
        for (Task t : plane.getTasks()) {
            SelectorFactor<FactorID> f = plane.getTaskFactor(t);
            f.clearNeighbors();
        }

        // We are a neighbor of ourselves
        int nPendingTasks = 0;
        List<MSPlane> neighbors = plane.getNeighbors();
        neighbors.clear();
        for (MessagingAgent a : tracker.getNeighbors(getConfiguration().getMsIterations())) {
            MSPlane p = (MSPlane)a;
            neighbors.add(p);

            for (Task t : p.getTasks()) {
                pf.addNeighbor(new FactorID(p, t));
                nPendingTasks++;
            }

            for (Task t : plane.getTasks()) {
                plane.getTaskFactor(t).addNeighbor(new FactorID(p));
            }

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0} has neighbor {1}", new Object[]{getAgent(), a});
            }
        }

        // Disable the plane if it has no neighbors (the plane itself is always in the
        // neighbors list)
        plane.setInactive(neighbors.size()<2 || nPendingTasks == 0);

        if (LOG.isLoggable(Level.FINEST)) {
            for (Task t : plane.getTasks()) {
                LOG.log(Level.FINEST, "Task factor: {0}", plane.getTaskFactor(t));
            }
            LOG.log(Level.FINEST, "Plane factor: {0}", pf);
        }
    }

}