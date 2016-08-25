/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2012, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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
package es.csic.iiia.planes;

import es.csic.iiia.planes.behaviors.AbstractBehaviorAgent;
import es.csic.iiia.planes.evaluation.EvaluationStrategy;
import es.csic.iiia.planes.evaluation.IndependentDistanceEvaluation;
import es.csic.iiia.planes.gui.Drawable;
import es.csic.iiia.planes.gui.PlaneDrawer;
import es.csic.iiia.planes.idle.IdleStrategy;
import es.csic.iiia.planes.util.RotatingList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Skeletal implementation of a Plane, that implements very basic lower-level
 * behavior.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractPlane extends AbstractBehaviorAgent
    implements Plane {
    private static final Logger LOG = Logger.getLogger(AbstractPlane.class.getName());

    /**
     * ID Generator
     */
    final private static AtomicInteger idGenerator = new AtomicInteger();

    /**
     * Plane id
     */
    final int id = idGenerator.incrementAndGet();

    /**
     * Current plane state
     */
    private State state = State.NORMAL;

    /**
     * Recharge ratio (flight seconds per charning second)
     */
    private long rechargeRatio = 3;

    /**
     * Agent speed in meters per tenth of second
     */
    private double speed = 0;

    private long waitingTime = 0;

    public void waitFor(long time) { waitingTime = time; }

    public long getWaitingTime() { return waitingTime; }

    public void tick() { waitingTime--; }

    /**
     * Remaining battery in tenths of second
     */
    private Battery battery;

    /**
     * List of completed tasks' locations, for tracking purposes
     */
    private RotatingList<Location> completedLocations;

    /** G.B.
     * List of tasks that the plane is searching for
     */
    private List<Task> searchForTasks = null;

    /** G.B.
     * List of tasks that the plane is searching for
     */
    public List<Task> tasksToRemove = null;

    /**
     * List of tasks owned by this plane
     */
    private List<Task> tasks = null;

    /**
     * Next task to be completed by the plane
     */
    private Task nextTask = null;

    /**
     * Evaluation strategy used by the plane
     */
    private EvaluationStrategy evaluationStrategy = new IndependentDistanceEvaluation();

    /**
     * Idling strategy of this plane
     */
    private IdleStrategy idleStrategy;

    /**
     * Current plane angle, used only for drawing purposes
     */
    private double angle = 0;

    /**
     * Color of this plane, used only for drawing purposes
     */
    private int[] color;

    /**
     * Total flight distance of this plane.
     */
    private double flightDistance;

    private Location.MoveStep currentDestination;

    private PlaneDrawer drawer = null;

    /**
     * Default constructor
     *
     * @param location initial location of the plane
     */
    public AbstractPlane(Location location) {
        super(location);
        tasks = new ArrayList<Task>();
        tasksToRemove = new ArrayList<Task>();
        searchForTasks = new ArrayList<Task>();
        completedLocations = new RotatingList<Location>(Plane.NUM_COMPLETED_TASKS);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (drawer != null) {
            drawer.initialize();
        }
    }

    public Logger getLog() {
        return LOG;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    @Override
    public Battery getBattery() {
        return battery;
    }

    @Override
    public EvaluationStrategy getEvaluationStrategy() {
        return evaluationStrategy;
    }

    @Override
    public void setEvaluationStrategy(EvaluationStrategy evaluationStrategy) {
        this.evaluationStrategy = evaluationStrategy;
    }

    @Override
    public IdleStrategy getIdleStrategy() {
        return idleStrategy;
    }

    @Override
    public void setIdleStrategy(IdleStrategy idleStrategy) {
        this.idleStrategy = idleStrategy;
    }

    public long getRechargeRatio() {
        return rechargeRatio;
    }

    public void setRechargeRatio(long rechargeRatio) {
        this.rechargeRatio = rechargeRatio;
    }

    @Override
    public final double getCost(Task task) {
        return evaluationStrategy.getCost(this, task);
    }

    @Override
    public void setDestination(Location l) {
        if (currentDestination != null && currentDestination.destination.equals(l)) {
            return;
        }

        angle = getLocation().getAngle(l);
        currentDestination = getLocation().buildMoveStep(l, getSpeed());
    }

    public Location.MoveStep getCurrentDestination() { return currentDestination; }

    protected Task getNearest(List<Task> tasks) {
        final Location l = getLocation();
        double mind = Double.MAX_VALUE;
        Task best = null;

        for (Task t : tasks) {
            final double d = l.distance(t.getLocation());
            if (d < mind) {
                mind = d;
                best = t;
            }
        }

        return best;
    }

    @Override
    public List<Location> getPlannedLocations() {
        List<Location> plannedLocations = new ArrayList<Location>();
        // Add all the tasks to a list
        ArrayList<Task> candidateTasks = new ArrayList<Task>(getTasks());
        Location nextLocation = getLocation();
        while (!candidateTasks.isEmpty()) {
            Task next = getNearest(nextLocation, candidateTasks);
            candidateTasks.remove(next);
            nextLocation = next.getLocation();
            plannedLocations.add(nextLocation);
        }

        return plannedLocations;
    }

    /**
     * Retrieve the task from the candidates list that is nearest to the given
     * position
     *
     * @param position location of plane
     * @param candidates list of task locations being evaluated
     * @return nearest task to the given location.
     */
    protected Task getNearest(Location position, List<Task> candidates) {
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

    @Override
    public void step() {
        if (state == State.CHARGING) {
            battery.recharge(rechargeRatio);
            if (battery.isFull()) {
                state = State.NORMAL;
                setNextTask(nextTask);
            }
            super.step();
            return;
        }

        Station st = getWorld().getNearestStation(getLocation());
        if (state == State.TO_CHARGE) {
            goCharge(st);
            super.step();
            return;
        } else if (battery.getEnergy() <= getLocation().getDistance(st.getLocation())/getSpeed()) {
            setDestination(st.getLocation());
            goCharge(st);
            super.step();
            return;
        }

        // Handle this iteration's messages
        super.step();

        // Move the plane if it has some task to fulfill and is not charging
        // or going to charge
        if (nextTask != null) {
            if (getWaitingTime() > 0) {
                idleAction();
                tick();
                return;
            }
            setDestination(nextTask.getLocation());
            if (move()) {
                if (nextTask.isAlive()) {
                    final long timeLeft = getWorld().getDuration() - getWorld().getTime()%getWorld().getDuration();
                    waitFor((long)(timeLeft*getWorld().getTimeRescuePenalty()));
                    battery.consume((long)(getBattery().getEnergy()*getWorld().getRescuePowerPenalty()));
                }
                final Task completed = nextTask;
                nextTask = null;
                triggerTaskCompleted(completed);
            }
            return;
        }

        // If we reach this point, it means that the plane is idle, so let it
        // do some "idle action"
        idleAction();
    }

    /**
     * Action done by the plane whenever it is ready to handle tasks but no
     * task has been assigned to it.
     */
    protected void idleAction() {
        if (!idleStrategy.idleAction(this)) {
            angle += 0.01;
            if(getWorld().getTime()%3 == 0) {
                getBattery().consume(1);
            }
        }
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public boolean move() {
        flightDistance += getSpeed();
        battery.consume(1);
        angle = currentDestination.alpha;
        return getLocation().move(currentDestination);
    }

    /**
     * Method executed when a plane has just enough battery to go recharge
     * itself
     *
     * @param st charging station where to recharge
     */
    protected void goCharge(Station st) {
        state = State.TO_CHARGE;
        if (move()) {
            state = State.CHARGING;
            this.completedLocations.add(st.getLocation());
        }
    }

    /**
     * Signals that a task has been completed.
     *
     * @param t task that has been completed.
     */
    protected abstract void taskCompleted(Task t);

    /**
     * Sets the next task that this plane is going to fulfill.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - It runs out of battery (and therefore goes to recharse itself).
     *   - A new "next task" is set by calling this method again.
     *
     * @param t task to try to fulfill.
     */
    protected void setNextTask(Task t) {
        if (t != null && state == State.NORMAL) {
            LOG.log(Level.FINE, "{0} heads towards {1}", new Object[]{this, t});
            setDestination(t.getLocation());
        } else if (state == State.NORMAL) {
            LOG.log(Level.FINE, "{0} heads towards {1}", new Object[]{this, getWorld().getNearestOperator(getLocation())});
            setDestination(getWorld().getNearestOperator(getLocation()).getLocation());
        }
        nextTask = t;
    }

    /**
     * Record a task completion trigger any post-completion effects
     *
     * @param t task that has been completed
     */
    private void triggerTaskCompleted(Task t) {
        LOG.log(Level.FINE, "{0} completes {1}", new Object[]{this, t});
        completedLocations.add(t.getLocation());
        getWorld().removeTask(t);
        removeTask(t);
        taskCompleted(t);
        if (tasks.isEmpty() || nextTask == null) {
            Operator o = getWorld().getNearestOperator(getLocation());
            setDestination(o.getLocation());
        }
    }

    /**
     * Signals that a new task has been added.
     *
     * @param t task that has been added.
     */
    protected abstract void taskAdded(Task t);

    @Override
    public void addTask(Task task) {
        tasks.add(task);

        taskAdded(task);
    }

    @Override
    public void addSearchTask(Task task) {
        searchForTasks.add(task);
    }

    /**
     * Signals that a task has been removed.
     *
     * @param t task that has been removed.
     */
    protected abstract void taskRemoved(Task t);

    @Override
    public Task removeTask(Task task) {
        searchForTasks.remove(task);
        tasks.remove(task);
        taskRemoved(task);
        return task;
    }

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public List<Task> getSearchForTasks() {
        return searchForTasks;
    }

    @Override
    public Drawable getDrawer() {
        return drawer;
    }

    @Override
    public void setDrawer(PlaneDrawer drawer) {
        this.drawer = drawer;
    }

    @Override
    public List<Location> getCompletedLocations() {
        return completedLocations;
    }

    /* Horrible HACK:
     * This is to avoid creating java.awt.Color objects when running without
     * GUI, because creating a single Color object makes java think that this
     * is a GUI application, launch the GUI Thread, etc...
     */
    private Color cachedColor = null;

    @Override
    public Color getColor() {
        if (cachedColor == null) {
            cachedColor = new Color(color[0], color[1], color[2]);
        }
        return cachedColor;
    }

    @Override
    public void setColor(int[] color) {
        this.color = color;
    }

    @Override
    public double getTotalDistance() {
        return flightDistance;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public Task getNextTask() {
        return nextTask;
    }

    @Override
    public String toString() {
        return "Plane " + getId();
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }

        if (o instanceof Plane) {
            return id - ((Plane)o).getId();
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Plane)) {
            return false;
        }
        final Plane p = (Plane)o;

        return id == p.getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.id;
        return hash;
    }

    public void behaviorStep() {
        super.step();
    }

}