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
package es.csic.iiia.planes.behaviors;

import es.csic.iiia.planes.messaging.AbstractMessage;
import es.csic.iiia.planes.messaging.MessagingAgent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a neighbor tracking behavior.
 * <p/>
 * This behavior guarantees that all planes considered as neighbors in the
 * current iteration will be in range in the next one.
 *
 * @see #isNeighbor(es.csic.iiia.planes.messaging.MessagingAgent)
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class NeighborTracking extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(NeighborTracking.class.getName());

    private Set<MessagingAgent> oldNeighbors = new LinkedHashSet<MessagingAgent>();
    private Set<MessagingAgent> neighbors = new LinkedHashSet<MessagingAgent>();

    public NeighborTracking(MessagingAgent agent) {
        super(agent);
    }

    /**
     * Check if the given agent is a neighbor.
     *
     * @param agent to check for.
     * @return True if the given agent is a neighbor, or False otherwise.
     */
    public boolean isNeighbor(MessagingAgent agent) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{2} checking if {0} is within my neighbor list: {1}",
                    new Object[]{agent, neighbors, getAgent()});
        }

        return neighbors.contains(agent);
    }

    /**
     * Return True because this behaviour is promiscuous (we want to receive
     * all messages that we can hear, even if they are not intended for us).
     *
     * @return True.
     */
    @Override
    public boolean isPromiscuous() {
        return true;
    }

    @Override
    public void beforeMessages() {
        Set<MessagingAgent> aux = oldNeighbors;
        oldNeighbors = neighbors;
        neighbors = aux;
        neighbors.clear();
    }

    /**
     * Update our knowledge about this plane.
     *
     * @param m beacon message of the detected possible neighbor.
     */
    public void on(TrackingMessage m) {
        final MessagingAgent neighbor = m.getSender();

        // The maximum distance on the next iteration is the current distance
        // plus the maximum travel distance of each agent if they travelled on
        // completely opposite directions.
        final double d = getAgent().getLocation().distance(neighbor.getLocation());
        final double maxd = d + getAgent().getSpeed() + neighbor.getSpeed();

        if (maxd < getAgent().getCommunicationRange()) {
            neighbors.add(neighbor);
        }
    }

    /**
     * {@inheritDoc}
     *
     * In this case, this behavior notifies the agent exhibiting it of each
     * and every new agent detected or lost.
     */
    @Override
    public void afterMessages() {
        // Send a tracking message for the next iteration
        getAgent().send(new TrackingMessage());
    }

    /**
     * Get the set of neighbors that have been added in the current iteration.
     *
     * @return set of recently added neighbors.
     */
    public Set<MessagingAgent> getAddedNeighbors() {
        Set<MessagingAgent> result = new LinkedHashSet<MessagingAgent>();
        for (MessagingAgent neighbor : neighbors) {
            if (!oldNeighbors.contains(neighbor)) {
                result.add(neighbor);
            }
        }
        return result;
    }

    /**
     * Get the set of neighbors that have just gone out of range.
     *
     * @return set of recently lost neighbors.
     */
    public Set<MessagingAgent> getRemovedNeighbors() {
        Set<MessagingAgent> result = new LinkedHashSet<MessagingAgent>();
        for (MessagingAgent neighbor : oldNeighbors) {
            if (!neighbors.contains(neighbor)) {
                result.add(neighbor);
            }
        }
        return result;
    }

    public class TrackingMessage extends AbstractMessage {}

}