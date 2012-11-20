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
package es.csic.iiia.planes.maxsum;

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.messaging.MessagingAgent;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class MSUpdateGraphBehavior extends AbstractBehavior {

    private NeighborTracking tracker;

    /**
     * This list of neighbors is necessary because we only consider changes
     * every some iterations. Therefore, we can not use the most up-to-date
     * information from the NeighborTracking module, having to maintain
     * a point-in-time set of neighbors instead.
     */
    final private Set<MSPlane> neighbors;

    final private MSPlane plane;

    public MSUpdateGraphBehavior(MSPlane plane) {
        super(plane);
        this.plane = plane;
        this.neighbors = plane.getNeighbors();
    }

    @Override
    public boolean isPromiscuous() {
        return false;
    }
    
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

    @Override
    public void afterMessages() {
        Map<Task, MSPlane> domain = new TreeMap<Task, MSPlane>();

        // Track our tasks
        for (Task t : plane.getTasks()) {
            domain.put(t, plane);
        }

        // And the tasks from our neighbors
        neighbors.clear();
        for (MessagingAgent a : tracker.getNeighbors(2)) {
            MSPlane p = (MSPlane)a;
            neighbors.add(p);

            for (Task t : p.getTasks()) {
                domain.put(t, p);
            }
        }

        plane.getVariable().update(domain);
    }

}