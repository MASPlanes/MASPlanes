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
package es.csic.iiia.planes.maxsum.novel;

import es.csic.iiia.planes.AbstractPlane;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
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

    private MSPlaneNode planeFunction;
    private Map<Task, MSTaskNode> taskFunctions = new TreeMap<Task, MSTaskNode>();

    private boolean inactive;

    public MSPlaneNode getPlaneFunction() {
        return planeFunction;
    }

    public MSPlane(Location location) {
        super(location);
        addBehavior(new NeighborTracking(this));
        addBehavior(new MSUpdateGraphBehavior(this));
        addBehavior(new MSExecutionBehavior(this));
        addBehavior(new MSTasksDecideBehavior(this));
    }

    @Override
    public void initialize() {
        super.initialize();
        try {
            planeFunction = new MSPlaneNode(this);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to instantiate the specified plane node type.", ex);
        }
    }



    @Override
    protected void taskCompleted(Task t) {
        getPlaneFunction().removeNeighbor(t);
        replan();
    }

    @Override
    protected void taskAdded(Task t) {
        // Create a node for this task
        LOG.log(Level.FINE, "{0} now owns {1}", new Object[]{this, t});
        MSTaskNode node = new MSTaskNode(this, t);
        node.addNeighbor(this);
        planeFunction.addNeighbor(t, this);
        taskFunctions.put(t, node);
        replan(t);
    }

    @Override
    protected void taskRemoved(Task t) {
        // Cleanup any actions done at taskAdded...
        LOG.log(Level.FINE, "{0} is no longer the owner of {1}", new Object[]{this, t});
        MSTaskNode n = taskFunctions.get(t);
        n.clearNeighbors();
        taskFunctions.remove(t);
        planeFunction.removeNeighbor(t);

        // And replan if necessary
        setNextTask(getNearest(getLocation(), getTasks()));
    }

    @Override
    public List<Location> getPlannedLocations() {
        return null;
    }

    MSTaskNode getTaskFunction(Task task) {
        return taskFunctions.get(task);
    }

    Map<Task, MSTaskNode> getTaskFunctions() {
        return taskFunctions;
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

    /**
     * Retrieve the task from the candidates list that is nearest to the given
     * position
     *
     * @param position
     * @param candidates
     * @return
     */
    protected static Task getNearest(Location position, List<Task> candidates) {
        double max = Double.MAX_VALUE;
        Task result = null;
        for (Task candidate : candidates) {
            double d = position.distance(candidate.getLocation());
            if (d < max) {
                max = d;
                result = candidate;
            }
        }

        return result;
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
    List<MSPlane> getNeighbors() {
        return neighbors;
    }

}
