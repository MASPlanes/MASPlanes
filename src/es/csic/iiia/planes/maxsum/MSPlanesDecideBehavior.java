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
import es.csic.iiia.planes.messaging.MessagingAgent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSPlanesDecideBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(MSPlanesDecideBehavior.class.getName());

    public MSPlanesDecideBehavior(MessagingAgent agent) {
        super(agent);
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{MSExecutionBehavior.class};
    }

    @Override
    public boolean isPromiscuous() {
        return false;
    }

    @Override
    public MSPlane getAgent() {
        return (MSPlane)super.getAgent();
    }

    @Override
    public void beforeMessages() {

    }

    public void on(RequestTaskMessage msg) {
        final Task t = msg.getTask();
        final MSPlane p = getAgent();
        if (p.getTasks().contains(t) && p.getLocation().distance(t.getLocation()) > p.getSpeed()) {
            // Remove & dispatch this task to the asking plane
            p.removeTask(t);
            HandTaskMessage outmsg = new HandTaskMessage(t);
            outmsg.setRecipient(msg.getSender());
            p.send(outmsg);
            LOG.log(Level.FINER, "Handing {0} to {1}",
                    new Object[]{t, msg.getSender()});

            if (t == p.getNextTask()) {
                p.replan();
            }
        }
    }

    public void on(HandTaskMessage msg) {
        getAgent().addTask(msg.getTask());
        LOG.log(Level.FINER, "{0} incorporates {1}",
                new Object[]{getAgent(), msg.getTask()});
    }

    @Override
    public void afterMessages() {
        final long remainder = getAgent().getWorld().getTime() % getConfiguration().msStartEvery;
        if (remainder != getConfiguration().msIterations) {
            return;
        }

        final MSPlane p = getAgent();
        final MSPlaneNode v = p.getVariable();

        Task decision = v.makeDecision();
        if (decision == null) {
            return;
        }

        MSPlane owner = v.getDomain().get(decision);

        if (owner != p) {
            RequestTaskMessage msg = new RequestTaskMessage(decision);
            msg.setRecipient(owner);
            p.send(msg);
            LOG.log(Level.FINE, "{0} requesting {1}",
                    new Object[]{getAgent(), decision});
        }
    }



}
