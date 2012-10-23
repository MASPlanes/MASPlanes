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

import es.csic.iiia.planes.gui.DefaultPlaneDrawer;
import es.csic.iiia.planes.util.RotatingList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractPlane extends AbstractElement implements Plane {
    
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
     * Set the plane's location.
     */
    private Location location;
    
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
        this.location = location;
        tasks = new ArrayList<Task>();
        completedLocations = new RotatingList<Location>(Plane.NUM_COMPLETED_TASKS);
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    /**
     * Set plane's the remaining battery, in seconds
     * 
     * @param battery 
     */
    @Override
    public void setBattery(long battery) {
        this.battery = battery;
    }
    
    /**
     * Get the plane's remianing battery in seconds
     * 
     * @return 
     */
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

    /**
     * Set the battery capacity (maximum battery charge in seconds)
     * 
     * @param capacity 
     */
    @Override
    public void setBatteryCapacity(long capacity) {
        batteryCapacity = capacity;
    }
    
    /**
     * Get the battery capacity (maximum battery charge in seconds)
     * 
     * @return
     */
    @Override
    public long getBatteryCapacity() {
        return batteryCapacity;
    }
    
    /**
     * Get the plane's speed in meters per second
     * 
     * @return 
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the plane's speed, in meters per second
     * 
     * @param speed 
     */
    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    /**
     * Set the plane's color, used when drawing the GUI.
     * 
     * The color must be specified as an int array of length 3, representing
     * the RGB color values in the range 0-255.
     * 
     * @param color 
     */
    public void setColor(int[] color) {
        this.color = new Color(color[0], color[1], color[2]);
    }
    
    /**
     * Simulation step function
     */
    @Override
    public void step() {
        if (state == State.CHARGING) {
            this.battery += rechargeRatio;
            if (this.battery >= this.batteryCapacity) {
                battery = batteryCapacity;
                state = State.NORMAL;
            }
            return;
        }
        
        Station st = getWorld().getNearestStation(location);
        if (  state == State.TO_CHARGE
           || battery <= location.getDistance(st.getLocation())/speed)
        {
            goCharge(st);
            return;
        }
        
        if (nextTask != null) {
            angle = location.getAngle(nextTask.getLocation());
            if (location.move(nextTask.getLocation(), speed)) {
                triggerTaskCompleted(nextTask);
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
        angle = location.getAngle(st.getLocation());
        if (this.location.move(st.getLocation(), speed)) {
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
    
    private Drawable drawer = new DefaultPlaneDrawer(this);
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
    public void setLocation(Location position) {
        this.location = position;
    }
    
    @Override
    public Task getNextTask() {
        return nextTask;
    }

    @Override
    public Location getLocation() {
        return location;
    }
    
}
