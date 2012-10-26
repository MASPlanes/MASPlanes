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

import es.csic.iiia.planes.gui.Drawable;
import es.csic.iiia.planes.gui.PlaneDrawer;
import es.csic.iiia.planes.messaging.AbstractMessagingAgent;
import es.csic.iiia.planes.util.RotatingList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Skeletal implementation of a Plane, that implements very basic lower-level
 * behavior.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractPlane extends AbstractMessagingAgent implements Plane {
    
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
    private State state;
    
    /**
     * Recharge ratio (flight seconds per charning second)
     */
    private long rechargeRatio = 3;
    
    /**
     * Remaining battery in seconds
     */
    private long battery;
    
    /**
     * Battery capacity in seconds
     */
    private long batteryCapacity;
    
    /**
     * Plane speed in meters per second
     */
    private double speed = 50/3.6d;
    
    /**
     * List of completed tasks' locations, for tracking purposes
     */
    private RotatingList<Location> completedLocations;
    
    /**
     * List of tasks owned by this plane
     */
    private List<Task> tasks = null;
    
    /**
     * Next task to be completed by the plane
     */
    private Task nextTask = null;
    
    /**
     * Current plane angle, used only for drawing purposes
     */
    private double angle = 0;
    
    /**
     * Color of this plane, used only for drawing purposes
     */
    private Color color;
    
    /**
     * Default constructor
     * 
     * @param location initial location of the plane
     */
    public AbstractPlane(Location location) {
        super(location);
        tasks = new ArrayList<Task>();
        completedLocations = new RotatingList<Location>(Plane.NUM_COMPLETED_TASKS);
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public void setBattery(long battery) {
        this.battery = battery;
    }
    
    @Override
    public long getBattery() {
        return battery;
    }

    /**
     * Updates the battery, consuming one second
     */
    private void updateBattery() {
        battery--;
    }

    @Override
    public void setBatteryCapacity(long capacity) {
        batteryCapacity = capacity;
    }
    
    @Override
    public long getBatteryCapacity() {
        return batteryCapacity;
    }
    
    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public void setColor(int[] color) {
        this.color = new Color(color[0], color[1], color[2]);
    }
    
    @Override
    public void step() {
        if (state == State.CHARGING) {
            this.battery += rechargeRatio;
            if (this.battery >= this.batteryCapacity) {
                battery = batteryCapacity;
                state = State.NORMAL;
            }
            super.step();
            return;
        }
        
        Station st = getWorld().getNearestStation(getLocation());
        if (  state == State.TO_CHARGE
           || battery <= getLocation().getDistance(st.getLocation())/speed)
        {
            goCharge(st);
            super.step();
            return;
        }
        
        // Handle this iteration's messages
        super.step();
        
        // Move the plane if not charging or going to charge
        if (nextTask != null) {
            angle = getLocation().getAngle(nextTask.getLocation());
            if (getLocation().move(nextTask.getLocation(), speed)) {
                final Task completed = nextTask;
                nextTask = null;
                triggerTaskCompleted(completed);
            }
            updateBattery();
        }
    }
    
    /**
     * Method executed when a plane has just enough battery to go recharge
     * itself
     * 
     * @param st chargin station where to recharge
     */
    private void goCharge(Station st) {
        state = State.TO_CHARGE;
        angle = getLocation().getAngle(st.getLocation());
        if (this.getLocation().move(st.getLocation(), speed)) {
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
        nextTask = t;
    }
    
    /**
     * Record a task completion trigger any post-completion effects
     * 
     * @param t task that has been completed
     */
    private void triggerTaskCompleted(Task t) {
        tasks.remove(t);
        getWorld().removeTask(t);
        completedLocations.add(t.getLocation());
        
        taskCompleted(t);
    }
    
    /**
     * Signals that a new task has been added.
     * 
     * @param t task that has been added.
     */
    protected abstract void taskAdded(Task t);

    /**
     * Adds a new task to the list of tasks owned by this plane
     * 
     * Task addition triggers a reevaluation of the next task to be completed
     * 
     * @param task to add
     */
    @Override
    public void addTask(Task task) {
        tasks.add(task);

        taskAdded(task);
    }
    
    @Override
    public List<Task> getTasks() {
        return tasks;
    }
    
    private Drawable drawer = new PlaneDrawer(this);
    @Override
    public Drawable getDrawer() {
        return drawer;
    }

    @Override
    public List<Location> getCompletedLocations() {
        return completedLocations;
    }

    @Override
    public Color getColor() {
        return this.color;
    }
    
    @Override
    public void setColor(Color color) {
        this.color = color;
    }
    
    @Override
    public double getAngle() {
        return angle;
    }
    
    @Override
    public Task getNextTask() {
        return nextTask;
    }
    
    @Override
    public String toString() {
        return "Plane " + getId();
    }
    
}
