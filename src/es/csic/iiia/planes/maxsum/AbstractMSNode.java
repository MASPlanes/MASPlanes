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
package es.csic.iiia.planes.maxsum;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Skeletal implementation of a max-sum node (function).
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractMSNode<Domain extends Object, Message extends MSMessage>
    implements MSNode<Domain, Message> {

    private MSPlane plane;

    private final Map<Domain, Message> messages = new TreeMap<Domain, Message>();

    private Map<Domain, MSPlane> edges = new TreeMap<Domain, MSPlane>();

    /**
     * Build a new node that will run within the given plane.
     *
     * @param plane where this node will be executed.
     */
    public AbstractMSNode(MSPlane plane) {
        this.plane = plane;
    }

    @Override
    public MSPlane getPlane() {
        return plane;
    }

    @Override
    public Set<Domain> getDomain() {
        return edges.keySet();
    }

    /**
     * Retrieves the last message received about the specified domain value
     * (variable).
     *
     * @param value variable of interest.
     * @return last message received from this variable.
     */
    public Message getMessage(Domain value) {
        return messages.get(value);
    }

    /**
     * Get the map of domain values (variables) and where are their
     * corresponding nodes running.
     *
     * @return map of domain values to planes that run their nodes.
     */
    public Map<Domain, MSPlane> getEdges() {
        return edges;
    }

    /**
     * Returns the domain element to which the given message refers.
     *
     * @param message message where to get the domain from.
     * @return the domain element this message is about.
     */
    public abstract Domain getDomain(Message message);

    @Override
    public void receive(Message message) {
        messages.put(getDomain(message), message);
    }

    /**
     * Sends a message to the specified domain object (variable).
     *
     * @param m message to send.
     * @param object domain value where the message is being sent.
     */
    public void send(MSMessage m, Domain object) {
        m.setRecipient(edges.get(object));
        getPlane().send(m);
    }
}