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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class NeighborTracking extends AbstractBehavior {
    
    private Set<MessagingAgent> oldNeighbors = new HashSet<MessagingAgent>();
    private Set<MessagingAgent> neighbors = new HashSet<MessagingAgent>();
    private NeighborTrackingListener agent;

    public NeighborTracking(NeighborTrackingListener agent) {
        super(agent);
        this.agent = agent;
    }

    @Override
    public void beforeMessages() {
        Set<MessagingAgent> aux = oldNeighbors;
        oldNeighbors = neighbors;
        neighbors = aux;
        neighbors.clear();
    }
    
    public void on(TrackingMessage m) {
        final MessagingAgent neighbor = m.getSender();
        neighbors.add(neighbor);
    }

    /**
     * @{inheritDoc}
     * 
     * In this case, this behavior notifies the agent exhibiting it of each
     * and every new agent detected or lost.
     */
    @Override
    public void afterMessages() {
        
        for (MessagingAgent neighbor : neighbors) {
            if (!oldNeighbors.contains(neighbor)) {
                agent.neighborDetected(neighbor);
            }
        }
        
        for (MessagingAgent neighbor : oldNeighbors) {
            if (!neighbors.contains(neighbor)) {
                agent.neighborLost(neighbor);
            }
        }
        
        // Send a tracking message for the next iteration
        agent.send(new TrackingMessage());
    }
    
    public class TrackingMessage extends AbstractMessage {}
    
    public interface NeighborTrackingListener extends MessagingAgent {
        
        /**
         * Signals that a new neighbor has been detected.
         * @param neighbor that has been detected.
         */
        public void neighborDetected(MessagingAgent neighbor);
        
        /**
         * Signals that a neighbor has gone out of range.
         * @param neighbor that has been lost.
         */
        public void neighborLost(MessagingAgent neighbor);
        
    }
}
