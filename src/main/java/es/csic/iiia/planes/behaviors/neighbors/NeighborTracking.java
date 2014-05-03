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
package es.csic.iiia.planes.behaviors.neighbors;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.messaging.AbstractMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a neighbor tracking behavior.
 * <p/>
 * This behavior tracks neighbors when they get in and out of range.
 * Additionally, it allows depending behaviors to require that other agents
 * must be guaranteed to stay neighbors for a fixed number of iterations.
 *
 * @see #isNeighbor(es.csic.iiia.planes.MessagingAgent, int)
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class NeighborTracking extends AbstractBehavior<Plane> {
    private static final Logger LOG = Logger.getLogger(NeighborTracking.class.getName());

    private NeighborsCollection neighbors = new NeighborsCollection();
    private Location lastLocation;

    /**
     * Builds a new neighbor tracking behavior.
     *
     * @param agent exhibiting this behavior.
     */
    public NeighborTracking(Plane agent) {
        super(agent);
        lastLocation = agent.getLocation();
    }

    @Override
    public Class[] getDependencies() {
        return new Class[0];
    }

    /**
     * Check if the given agent is a neighbor (and is guaranteed to receive
     * any messages that we send him during this iteration)
     *
     * @param agent to check for.
     * @return True if the given agent is a neighbor, or False otherwise.
     */
    public boolean isNeighbor(MessagingAgent agent) {
        return isNeighbor(agent, 1);
    }

    /**
     * Check if the given agent will be a neighbor for at least
     * <em>iterations</em> iterations.
     *
     * @param agent to check for.
     * @return True if the given agent is a neighbor, or False otherwise.
     */
    public boolean isNeighbor(MessagingAgent agent, int iterations) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{1} checking if {0} will be neighbor for {2} iterations.",
                    new Object[]{agent, getAgent(), iterations});
            LOG.log(Level.FINEST, "Neighbors: {0}", neighbors);
        }

        return neighbors.contains(agent, iterations);
    }

    /**
     * Get the list of agents that are guaranteed to remain neighbors for at
     * least <em>iterations</em> iterations.
     *
     * @param iterations required number of iterations.
     * @return {@link Iterable} of agents that are guaranteed to remain neighbors.
     */
    public Iterable<MessagingAgent> getNeighbors(int iterations) {
        return neighbors.get(iterations);
    }

    /**
     * Check if there is any neighbor for at least the given number of
     * iterations.
     *
     * @param iterations required number of iterations.
     * @return True if there are neighbors, or false otherwise.
     */
    public boolean hasNeighbors(int iterations) {
        return neighbors.hasNeighbors(iterations);
    }

    @Override
    public void beforeMessages() {
        neighbors.clear();
        neighbors.add(getAgent(), Integer.MAX_VALUE);
    }

    /**
     * Update our knowledge about this plane.
     *
     * @param m beacon message of the detected possible neighbor.
     */
    public void on(TrackingMessage m) {
        final MessagingAgent neighbor = m.getSender();

        // Compute the number of steps that the neighbor is guaranteed to still
        // be in range.
        LOG.log(Level.FINEST, "My location: {0}, theirs: {1}", new Object[]{lastLocation, m.getLocation()});
        final double d = lastLocation.getDistance(m.getLocation());

        double d_step = getAgent().getSpeed();
        if (neighbor instanceof Plane) {
            d_step += ((Plane)neighbor).getSpeed();
        }

        // The objective is max(n) s.t. d + d_step * n < comm_range
        // we compute that as n=int(s) where s = (comm_range - d)/d_step
        final double s = (getAgent().getCommunicationRange() - d) / d_step;
        LOG.log(Level.FINEST, "d = {0}, d_step = {1}, range = {2}", new Object[]{d, d_step, getAgent().getCommunicationRange()});
        final int n = (int)s;

        if (n > 0) {
            LOG.log(Level.FINER, "Adding {0} as a neighbor for {1} iterations.",
                    new Object[]{neighbor, n});
            neighbors.add(neighbor, n);
        } else {
            LOG.log(Level.FINEST, "Ignoring {0} as a neighbor (s={1}, d_step={2}).",
                    new Object[]{neighbor, s, d_step});
        }
    }

    /**
     * {@inheritDoc}
     *
     * In this case, this behavior notifies the agent exhibiting it of each
     * and every new agent detected or lost.
     */
    @Override
    public void postStep() {
        MessagingAgent a = getAgent();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0} sending beacon.", new Object[]{a});
        }
        lastLocation = new Location(a.getLocation());
        a.send(new TrackingMessage(lastLocation));
    }

    /**
     * Beacon message sent by agents that keep track of their neighbors.
     */
    public class TrackingMessage extends AbstractMessage {

        private final Location location;

        public TrackingMessage(Location location) {
            this.location = new Location(location);
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "TrackingMessage(" + getAgent() + ")";
        }
    }

}