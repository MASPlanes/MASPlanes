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
package es.csic.iiia.planes.maxsum.distributed;

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.maxsum.centralized.Factor;
import es.csic.iiia.planes.maxsum.centralized.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxy factor that connects two (possibly) remotely running factors through
 * the plane's network layer.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class ProxyFactor<LocalType, RemoteType> implements Factor {

    private LocalType from;
    private RemoteType to;
    private Plane fromLocation;
    private Plane toLocation;
    private Factor factor;

    /**
     * Build a new proxy
     *
     * @param from logical origin (task or plane) of this proxy
     * @param to logical destination (task or plane) of this proxy
     * @param fromLocation physical location (plane) where this factor runs
     * @param toLocation physical location (plane) where the remote factor runs
     */
    public ProxyFactor(LocalType from, RemoteType to, Plane fromLocation, Plane toLocation) {
        this.from = from;
        this.to = to;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    /**
     * Get the logical origin (task or plane) of this proxy.
     * @return logical origin of this proxy.
     */
    public LocalType getFrom() {
        return from;
    }

    /**
     * Get the logical destination (task or plane) of this proxy.
     * @return logical destination of this proxy.
     */
    public RemoteType getTo() {
        return to;
    }

    /**
     * Get the physical location (plane) where this factor is running.
     * @return plane where this factor is running.
     */
    public Plane getFromLocation() {
        return fromLocation;
    }

    /**
     * Get the physical location (plane) where the remote factor is running.
     * @return plane where the remote factor is running.
     */
    public Plane getToLocation() {
        return toLocation;
    }

    /**
     * Builds an outgoing message of a specific type.
     * @return specific outgoing message
     */
    protected abstract MSMessage buildMessage(double value);

    /**
     * Receive a logical message, forwarding it to the remote location
     * encapsulated in a "physical" message.
     *
     * @param message message to forward.
     */
    @Override
    public void receive(Message message) {
        MSMessage msg = buildMessage(message.value);
        msg.setLogicalRecipient(to);
        msg.setLogicalSender(from);
        msg.setRecipient(toLocation);
        msg.setSender(fromLocation);
        fromLocation.send(msg);
    }

    /**
     * Receive a network message, forwarding it to the local max-sum node as a
     * logical message.
     *
     * @param message message to receive.
     */
    public void receive(MSMessage message) {
        Message msg = new Message(this, message.getValue());
        send(msg, factor);
    }

    /**
     * Set the neighbor (logical factor for which we are proxying) of this proxy
     *
     * @param factor logical factor
     */
    @Override
    public void addNeighbor(Factor factor) {
        this.factor = factor;
    }

    /**
     * Get the "neighbor" of this factor (the recipient to which this object
     * is proxying).
     *
     * @return neighbor of this factor.
     */
    @Override
    public List<Factor> getNeighbors() {
        List<Factor> neighbors = new ArrayList<Factor>();
        neighbors.add(factor);
        return neighbors;
    }

    /**
     * Sends a message to the specified recipient (which <em>must</em> be the
     * only proxy's neighbor.
     *
     * @param message message to be sent.
     * @param recipient recipient of the message.
     */
    @Override
    public void send(Message message, Factor recipient) {
        recipient.receive(message);
    }

    /**
     * Do nothing, since this is not a "real" factor.
     */
    @Override
    public void tick() {}

    /**
     * Do nothing, since this is not a "real" factor.
     */
    @Override
    public void gather() {}

    /**
     * Do nothing, since this is not a "real" factor.
     */
    @Override
    public void scatter() {}

}
