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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a node in a MaxSum graph.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class MSNode<DK, DV> {
    private static final Logger LOG = Logger.getLogger(MSNode.class.getName());

    private final MSPlane plane;

    public MSPlane getPlane() {
        return plane;
    }

    private Map<DK, DV> domain = new TreeMap<DK, DV>();

    private final Minimizer<DK> minimizer = new Minimizer<DK>();

    protected final Map<DK, MSMessage> lastMessages =
            new TreeMap<DK, MSMessage>();

    public MSNode(MSPlane plane) {
        this.plane = plane;
    }

    public void update(Map<DK, DV> domain) {
       this.domain = domain;

       // Cleanup old messages
       Iterator<Map.Entry<DK, MSMessage>> it = lastMessages.entrySet().iterator();
       while (it.hasNext()) {
           if (!domain.containsKey(it.next().getKey())) {
               it.remove();
           }
       }
    }

    protected abstract double getPotential(DK p);
    protected abstract MSMessage buildOutgoingMessage(DK t, double value);
    protected abstract String getIdentifier();

    public void gather() {
        minimizer.reset();

        double[] vs = new double[domain.size()];
        int i = 0;
        for (DK p : domain.keySet()) {
            MSMessage msg = lastMessages.get(p);
            final double value = msg != null ? msg.getValue() : 0;
            final double belief = getPotential(p) + value;
            minimizer.track(p, belief);
            vs[i++] = belief;
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0}''s belief: {1}", new Object[]{getIdentifier(), Arrays.toString(vs)});
        }
    }

    public void scatter() {
        for (DK p : domain.keySet()) {
            final double value = getPotential(p) - minimizer.getComplementary(p);
            MSMessage msg = buildOutgoingMessage(p, value);
            msg.setRecipient(getRecipient(p));

            plane.send(msg);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Sending {0} to {1}", new Object[]{msg, msg.getRecipient()});
            }
        }

    }

    public DK makeDecision() {
        //LOG.log(Level.SEVERE, "M: {0}", minimizer.toString());
        return minimizer.getBest();
    }

    /**
     * Get the key used to isert this message into the last received messages
     * map.
     *
     * @param msg message being received.
     * @return key used to insert this message into the map.
     */
    protected abstract DK getKey(MSMessage msg);

    /**
     * Get the recipient plane of a message that is about the given key.
     *
     * @param key about which message is "speaking".
     * @return plane where the message should be sent to.
     */
    protected abstract MSPlane getRecipient(DK key);

    void receive(MSMessage msg) {
        lastMessages.put(getKey(msg), msg);
    }

    public Map<DK, DV> getDomain() {
        return domain;
    }
}
