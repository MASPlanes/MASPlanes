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
import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.Minimize;
import es.csic.iiia.bms.factors.SelectorFactor;
import es.csic.iiia.planes.AbstractPlane;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.maxsum.centralized.CostFactor;
import es.csic.iiia.planes.messaging.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plane that coordinates with others by using max-sum.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSPlane extends AbstractPlane {
    private static final Logger LOG = Logger.getLogger(MSPlane.class.getName());

    private static final MaxOperator operator = new Minimize();

    private final MSCommunicationAdapter adapter = new MSCommunicationAdapter(this);
    private CostFactor<FactorID> planeFactor;
    private final Map<FactorID, SelectorFactor<FactorID>> taskFactors =
            new TreeMap<FactorID, SelectorFactor<FactorID>>();

    private boolean inactive;

    public Factor<FactorID> getFactor(FactorID id) {
        if (planeFactor.getIdentity().equals(id)) {
            return planeFactor;
        }
        return taskFactors.get(id);
    }

    public CostFactor<FactorID> getPlaneFactor() {
        return planeFactor;
    }

    public SelectorFactor<FactorID> getTaskFactor(FactorID id) {
        return taskFactors.get(id);
    }

    public SelectorFactor<FactorID> getTaskFactor(Task task) {
        return taskFactors.get(new FactorID(this, task));
    }

    private SelectorFactor<FactorID> createTaskFactor(FactorID id) {
        SelectorFactor<FactorID> factor = new SelectorFactor<FactorID>();
        initialize(factor, id);
        factor.addNeighbor(planeFactor.getIdentity());
        taskFactors.put(factor.getIdentity(), factor);
        planeFactor.addNeighbor(factor.getIdentity());
        return factor;
    }

    public Map<FactorID, SelectorFactor<FactorID>> getTaskFactors() {
        return taskFactors;
    }

    public MSPlane(Location location) {
        super(location);
        addBehavior(new NeighborTracking(this));
        addBehavior(new MSUpdateGraphBehavior(this));
        addBehavior(new MSExecutionBehavior(this));
        addBehavior(new MSTasksDecideBehavior(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        super.initialize();
        planeFactor = getWorld().getFactory().buildCostFactor(this);
        initialize(planeFactor, new FactorID(this));
    }

    private void initialize(Factor<FactorID> factor, FactorID id) {
        factor.setIdentity(id);
        factor.setMaxOperator(operator);
        factor.setCommunicationAdapter(adapter);
    }

    @Override
    protected void taskCompleted(Task t) {
        LOG.log(Level.FINE, "{0} completes {1}", new Object[]{this, t});

        // TODO: I think this is not necessary
        //planeFactor.removeNeighbor(new FactorID(this, t));

        replan();
    }

    @Override
    protected void taskAdded(Task t) {
        // Create a node for this task
        LOG.log(Level.FINE, "{0} now owns {1}", new Object[]{this, t});

        FactorID id = new FactorID(this, t);
        createTaskFactor(id);

        replan(t);
    }

    @Override
    protected void taskRemoved(Task t) {
        // Cleanup any actions done at taskAdded...
        LOG.log(Level.FINE, "{0} is no longer the owner of {1}", new Object[]{this, t});

        Factor<FactorID> taskFactor = taskFactors.remove(new FactorID(this, t));
        taskFactor.clearNeighbors();
        planeFactor.removeNeighbor(taskFactor.getIdentity());

        // And replan if necessary
        replan();
    }

    @Override
    public void send(Message message) {
        super.send(message);
        if (LOG.isLoggable(Level.FINER) && message instanceof MSMessage) {
                LOG.log(Level.FINER, "[{2}] Sending {0} to {1}", new Object[]{message, message.getRecipient(), getWorld().getTime()});
        }
    }

    /**
     * Forces the plane to replan its route, possibly changing the next target
     * to the one that is currently closer.
     */
    protected void replan() {
        setNextTask(getNearest(getLocation(), getTasks()));
    }

    /**
     * Challenges the plane to reconsider its route, but only because the given
     * task has just been added.
     * <p/>
     * In other words, the plane only needs to consider the task it is already
     * attending against the new task, because all others are already known to
     * be worse than the current one.
     *
     * @param newt task to consider.
     */
    protected void replan(Task newt) {
        // Directly choose the new task if the plane is currently inactive
        final Task oldt = getNextTask();
        if (oldt == null) {
            setNextTask(newt);
            return;
        }

        // Choose the closest one if it is active instead
        final Location l = getLocation();
        final double curd = l.distance(oldt.getLocation());
        final double newd = l.distance(newt.getLocation());
        if (newd < curd) {
            setNextTask(newt);
        }
    }

    public void setInactive(boolean inactive) {
        if (inactive) {
            LOG.log(Level.FINEST, "{0} is inactive.", this);
        }
        this.inactive = inactive;
    }

    public boolean isInactive() {
        return inactive;
    }

    private List<MSPlane> neighbors = new ArrayList<MSPlane>();
    protected List<MSPlane> getNeighbors() {
        return neighbors;
    }

}
