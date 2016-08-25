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
package es.csic.iiia.planes.auctions;

import es.csic.iiia.planes.*;
import es.csic.iiia.planes.auctions.bidding.BiddingRule;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.cli.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Implementation of a plane that coordinates using auctions.
 * <p/>
 * Aside from the auction coordination, this plane use the "nearest task"
 * strategy.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AuctionPlane extends AbstractPlane {

    private ArrayList<Task> localTasks = new ArrayList<Task>();

    public AuctionPlane(Location location) {
        super(location);
        addBehavior(new NeighborTracking(this));
        addBehavior(new AuctionBehavior(this));
    }

    @Override
    protected void taskCompleted(Task t) {}

    @Override
    protected void taskAdded(Task t) {
        localTasks.add(t);

        final Task current = getNextTask();
        final double newdist = getLocation().distance(t.getLocation());
        if (current == null || newdist < getLocation().distance(current.getLocation())) {
            setNextTask(t);
        }
    }

    @Override
    protected void taskRemoved(Task t) {
        localTasks.remove(t);
        setNextTask(findClosest(getLocation(), localTasks));
    }

    private static double replan(Location origin, List<Task> tasks) {
        List<Task> pending = new ArrayList<Task>(tasks);
        tasks.clear();
        Location current = origin;
        double cost = 0;
        while (!pending.isEmpty()) {
            Task next = findClosest(current, pending);
            cost += current.distance(next.getLocation());
            pending.remove(next);
            tasks.add(next);
            current = next.getLocation();
        }
        return cost;
    }

    private static Task findClosest(Location location, List<Task> candidates) {
        double mind = Double.MAX_VALUE;
        Task result = null;
        for (Task t : candidates) {
            final double d = location.distance(t.getLocation());
            if (d < mind) {
                result = t;
                mind = d;
            }
        }
        return result;
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

        if (!getTasks().isEmpty()) {
            for (Task t:tasksToRemove) {
                getTasks().remove(t);
                taskRemoved(t);
            }
        }
        tasksToRemove.clear();*/

        if (getWorld().getUnassignedBlocks().isEmpty()  || !getTasks().isEmpty()) {
            super.step();
        }
        else if (!getWorld().getUnassignedBlocks().isEmpty()){
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

    /**.
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
                // Handle this iteration's messages
                super.behaviorStep();
            }
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

    @Override
    public void initialize() {
        super.initialize();
        // TODO: Added in next line, but maybe should check if already initialized
        setNextBlockBasic();
    }

}
