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
import es.csic.iiia.planes.*;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.maxsum.centralized.CostFactor;
import es.csic.iiia.planes.messaging.Message;

import java.util.*;
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

        // TODO: Added in next line, but maybe should check if already initialized
        setNextBlockBasic();
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

    /**
     * Tasks that should have been submitted to some UAV, but were not because
     * no plane is range.
     */
    private List<Task> tasksFound = new ArrayList<Task>();

    /**
     * Next block to be searched by the plane
     */
    private Block nextBlock = null;

    @Override
    public void step() {
        // Iterate through all of the plane's list of survivors it's trying
        // to find, and see if they have died at this point.
        // TODO: Uncomment when survivors can expire
        /*for (Task t:getSearchForTasks()) {
            if (t.getExpireTime()<=getWorld().getTime()){
                t.expire();
                getWorld().removeExpired(t);
                tasksToRemove.add(t);
            }
        }*/

        if (!tasksToRemove.isEmpty()) {
            for (Task t:tasksToRemove) {
                t.expire();
                getWorld().removeExpired(t);
            }
        }

        tasksToRemove.clear();

        // Iterate through all of the plane's list of survivors it's trying
        // to rescue, and see if they have died at this point.
        /*for (Task t:getTasks()) {
            if (t.getExpireTime()<=getWorld().getTime()){
                t.expire();
                getWorld().removeExpired(t);
                tasksToRemove.add(t);
            }
        }

        for (Task t:tasksToRemove) {
            getTasks().remove(t);
            taskRemoved(t);
        }

        tasksToRemove.clear();*/

        if (getWorld().getUnassignedBlocks().isEmpty() || !getTasks().isEmpty()) {
            super.step();
        }
        else if (!getWorld().getUnassignedBlocks().isEmpty()) {
            stepSearch();
        }
        else {
            idleAction();
        }
    }

    private void setNextBlockBasic() {
        Random rnd = new Random();
        if (getWorld().getUnassignedBlocks().isEmpty()){
            idleAction();
            return;
        }
        nextBlock = getWorld().getUnassignedBlocks().remove(rnd.nextInt(getWorld().getUnassignedBlocks().size()));
        setDestination(nextBlock.getCenter());
    }

    /**
     * TODO: Write this code for Eagle Planes.
     * Record a task completion trigger any post-completion effects
     *
     * @param b block that has been completed
     */
    private void triggerTaskFound(Block b) {
        Task t = b.getSurvivor();
        getLog().log(Level.FINE, "{0} finds {1}", new Object[]{this, t});
        getCompletedLocations().add(t.getLocation());
        //TODO: Change to set as discovered and put the location in the operator's list of survivors in need of rescue
        tasksFound.add(t);
        getWorld().foundTask(t);
        if (nextBlock == null) {
            Operator o = getWorld().getNearestOperator(getLocation());
            setDestination(o.getLocation());
        }
    }

    public void stepSearch() {
        if (getState() == State.CHARGING) {
            getBattery().recharge(getRechargeRatio());
            if (getBattery().isFull()) {
                setState(State.NORMAL);
                if(nextBlock != null) {
                    setDestination(nextBlock.getCenter());
                }
                else {
                    setNextBlockBasic();
                }
            }
            // Handle this iteration's messages
            super.behaviorStep();
            return;
        }

        Station st = getWorld().getNearestStation(getLocation());
        if (getState() == State.TO_CHARGE) {
            goCharge(st);
            // Handle this iteration's messages
            super.behaviorStep();
            return;
        } else if (getBattery().getEnergy() <= getLocation().getDistance(st.getLocation())/getSpeed()) {
            setDestination(st.getLocation());
            goCharge(st);
            // Handle this iteration's messages
            super.behaviorStep();
            return;
        }

        // Handle this iteration's messages
        super.behaviorStep();

        // Move the plane if it has some task to fulfill and is not charging
        // or going to charge
        if (nextBlock != null) {
            if (getWaitingTime() > 0) {
                idleAction();
                tick();
                return;
            }
            setDestination(nextBlock.getCenter());
            if (move()) {
                waitFor(100);
                getBattery().consume(5);
                final Block completed = nextBlock;
                nextBlock = null;
                if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
                    triggerTaskFound(completed);
                }
                Operator o = getWorld().getNearestOperator(getLocation());
                Location l = o.getLocation();
                if (l.getDistance(getLocation()) <= getCommunicationRange()) {
                    for (Task t: tasksFound) {
                        o.getPendingTasks().add(t);
                    }
                    tasksFound.clear();
                }
                setNextBlockBasic();
            }
            return;
        }

        // If we reach this point, it means that the plane is idle, so let it
        // do some "idle action"
        idleAction();
    }
}
