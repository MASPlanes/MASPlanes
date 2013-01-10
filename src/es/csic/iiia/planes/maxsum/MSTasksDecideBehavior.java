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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSTasksDecideBehavior extends AbstractBehavior {
    private static final Logger LOG = Logger.getLogger(MSTasksDecideBehavior.class.getName());

    public MSTasksDecideBehavior(MessagingAgent agent) {
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

    public void on(HandTaskMessage msg) {
        getAgent().addTask(msg.getTask());
        LOG.log(Level.FINER, "[{2}] {0} incorporates {1}",
                new Object[]{getAgent(), msg.getTask(), getAgent().getWorld().getTime()});
    }

    @Override
    public void afterMessages() {
        final long remainder = getAgent().getWorld().getTime() % getConfiguration().msStartEvery;
        if (getAgent().isInactive() || remainder != getConfiguration().msIterations) {
            return;
        }

        final MSPlane p = getAgent();

        // And now the tasks choose
        List<Task> tasks = p.getTasks();
        for (int i=tasks.size()-1; i>=0; i--) {
            final Task t = tasks.get(i);
            final MSOldTaskNode f = p.getFunction(t);
            final MSPlane choice = f.makeDecision();
            LOG.log(Level.FINE, "[{2}] {0} chooses {1} (inside {3})", new Object[]{f.getIdentifier(), choice, getAgent().getWorld().getTime(), getAgent()});
            if (choice != p && choice != null) {
                relocateTask(t, choice);
            }
        }

    }

    /**
     * Send a task to a neighbor.
     *
     * @param t
     * @param choice
     */
    private void relocateTask(Task t, MSPlane choice) {
        getAgent().removeTask(t);
        HandTaskMessage msg = new HandTaskMessage(t);
        msg.setRecipient(choice);
        getAgent().send(msg);
    }

}
