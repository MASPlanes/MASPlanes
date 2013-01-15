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

import es.csic.iiia.planes.Task;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.bcel.generic.GETSTATIC;

/**
 * Max-sum node representing the interests of a task.
 * <p/>
 * Basically, a task tries to guarantee that it is going to be picked up by one
 * (and only one) plane. Further, it tries to do it in a way that minimizes the
 * travel cost of the planes.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSTaskNode extends AbstractMSNode<MSPlane, MSPlane2Task> {
    private static final Logger LOG = Logger.getLogger(MSTaskNode.class.getName());

    private Minimizer<MSPlane> minimizer = new Minimizer<MSPlane>();

    private Task task;

    public MSTaskNode(MSPlane plane, Task task) {
        super(plane);
        this.task = task;
    }

    @Override
    public double getPotential(MSPlane domainValue) {
        return 0;
    }

    @Override
    public MSPlane getDomain(MSPlane2Task message) {
        return message.getPlane();
    }

    @Override
    public void iter() {
        final boolean logEnabled = LOG.isLoggable(Level.FINEST);
        final Set<MSPlane> domain = getDomain();
        minimizer.reset();

        double[] vs = null; int i = 0;
        if (logEnabled) {
            vs = new double[domain.size()];
        }

        for (MSPlane p : domain) {
            MSMessage msg = getMessage(p);
            final double value = msg != null ? msg.getValue() : 0;
            final double belief = getPotential(p) + value;
            minimizer.track(p, belief);
            if (logEnabled) {
                vs[i++] = belief;
            }
        }
        if (logEnabled) {
            LOG.log(Level.FINER, "{0}''s belief: {1}", new Object[]{this, Arrays.toString(vs)});
        }

        for (MSPlane p : domain) {
            final double value = getPotential(p) - minimizer.getComplementary(p);
            MSMessage msg = new MSTask2Plane(task, p, value);
            send(msg, p);

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Sending {0} to {1}", new Object[]{msg, msg.getRecipient()});
            }
        }
    }

    @Override
    public MSPlane makeDecision() {
        return minimizer.getBest();
    }

    @Override
    public String toString() {
        return "T(" + task.getId() + ")";
    }

}
