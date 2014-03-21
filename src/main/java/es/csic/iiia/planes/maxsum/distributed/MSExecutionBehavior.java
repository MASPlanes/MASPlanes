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
package es.csic.iiia.planes.maxsum.distributed;

import es.csic.iiia.bms.Factor;
import es.csic.iiia.bms.factors.SelectorFactor;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.maxsum.centralized.CostFactor;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Behavior that implements the actual max-sum algorithm.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSExecutionBehavior extends AbstractBehavior<MSPlane> {
    private static final Logger LOG = Logger.getLogger(MSExecutionBehavior.class.getName());

    public MSExecutionBehavior(MSPlane plane) {
        super(plane);
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{MSUpdateGraphBehavior.class};
    }

    @Override
    public void beforeMessages() {

    }

    /**
     * Collect {@link MSPlane2Task} messages destined to a task node running
     * whithin this plane.
     *
     * @param msg message to collect.
     */
    public void on(MSMessage msg) {
        Factor<FactorID> f = getAgent().getFactor(msg.recipientFactor);
        if (f == null) {
            LOG.log(Level.FINER, "Factor {0} does not exist in {1}. Recently completed?", new Object[]{msg.recipientFactor, getAgent()});
            return;
        }
        f.receive(msg.value, msg.senderFactor);
    }

    @Override
    public void afterMessages() {
        final MSPlane plane = getAgent();

        // Update costs according to the current positions
        final CostFactor<FactorID> planeFactor = plane.getPlaneFactor();
        for (FactorID id : planeFactor.getNeighbors()) {
            LOG.log(Level.FINEST, "{0}''s potential for {1}: {2}",
                    new Object[]{plane, id.task, plane.getCost(id.task)});
            planeFactor.setPotential(id, plane.getCost(id.task));
        }
    }

    @Override
    public void postStep() {
        final long remainder = getAgent().getWorld().getTime() % getConfiguration().getMsStartEvery();
        if (getAgent().isInactive() || remainder < 1 || remainder >= getConfiguration().getMsIterations()) {
            return;
        }

        final CostFactor<FactorID> planeFactor = getAgent().getPlaneFactor();
        final Map<FactorID, SelectorFactor<FactorID>> taskFactors = getAgent().getTaskFactors();

        // Everyone run
        planeFactor.run();
        for (SelectorFactor<FactorID> f : taskFactors.values()) {
            f.run();
        }
    }

}