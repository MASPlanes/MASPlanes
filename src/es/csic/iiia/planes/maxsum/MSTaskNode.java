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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSTaskNode extends AbstractMSNode<MSPlane, MSPlaneNode.MSPlane2Task> {
    private static final Logger LOG = Logger.getLogger(MSTaskNode.class.getName());

    private Minimizer<MSPlane> minimizer = new Minimizer<MSPlane>();

    private Map<MSPlane, MSPlaneNode.MSPlane2Task> messages =
            new TreeMap<MSPlane, MSPlaneNode.MSPlane2Task>();

    public MSTaskNode(MSPlane plane) {
        super(plane);
    }

    @Override
    public double getPotential(MSPlane domainValue) {
        return 0;
    }

    @Override
    public void receive(MSPlaneNode.MSPlane2Task message) {
        messages.put(message.getSender(), message);
    }

    @Override
    public void iter() {
        minimizer.reset();

        List<MSPlane> domain = getDomain();
        double[] vs = new double[domain.size()];
        int i = 0;
        for (MSPlane p : domain) {
            MSMessage msg = messages.get(p);
            final double value = msg != null ? msg.getValue() : 0;
            final double belief = getPotential(p) + value;
            minimizer.track(p, belief);
            vs[i++] = belief;
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0}''s belief: {1}", new Object[]{getIdentifier(), Arrays.toString(vs)});
        }

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

}
