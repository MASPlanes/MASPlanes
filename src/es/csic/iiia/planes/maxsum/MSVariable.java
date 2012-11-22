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
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSVariable {
    private static final Logger LOG = Logger.getLogger(MSVariable.class.getName());

    private MSPlane plane;

    private Map<Task, MSPlane> domain;

    private final Minimizer<Task> minimizer = new Minimizer<Task>();

    protected final Map<Task, MSFunction2VariableMessage> lastMessages =
            new TreeMap<Task, MSFunction2VariableMessage>();

    public MSVariable(MSPlane plane) {
        this.plane = plane;
    }

    public void update(Map<Task, MSPlane> domain) {
       this.domain = domain;

       // Cleanup old messages
       Iterator<Entry<Task, MSFunction2VariableMessage>> it =
               lastMessages.entrySet().iterator();
       while (it.hasNext()) {
           final Entry<Task, MSFunction2VariableMessage> e = it.next();
           if (!domain.containsKey(e.getKey())) {
               it.remove();
           }
       }
    }

    public void gather() {
        minimizer.reset();

        double[] vs = new double[domain.size()];
        int i = 0;
        for (Task t : domain.keySet()) {
            MSFunction2VariableMessage msg = lastMessages.get(t);
            final double value = msg != null ? msg.getValue() : 0;
            final double belief = plane.getCost(t) + value;
            minimizer.track(t, belief);
            vs[i++] = belief;
        }
        LOG.log(Level.FINE, "{0}''s belief: {1}", new Object[]{plane, Arrays.toString(vs)});
    }

    public void scatter() {
        for (Task t : domain.keySet()) {
            final double value = plane.getCost(t) - minimizer.getComplementary(t);
            MSVariable2FunctionMessage msg = new MSVariable2FunctionMessage(t, value);
            msg.setRecipient(domain.get(t));

            plane.send(msg);
            LOG.log(Level.FINE, "Sending {0} to {1}", new Object[]{msg, msg.getRecipient()});
        }

    }

    public Task makeDecision() {
        return minimizer.getBest();
    }

    void receive(MSFunction2VariableMessage msg) {
        lastMessages.put(msg.getTask(), msg);
    }

    protected Map<Task, MSPlane> getDomain() {
        return domain;
    }

}