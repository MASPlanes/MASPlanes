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
package es.csic.iiia.planes;

import java.awt.Color;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface Plane extends Agent, Positioned {
    
    /**
     * Plane states
     */
    public enum State {
        NORMAL, TO_CHARGE, CHARGING
    }
    
    /**
     * Number of history points to store, for debugging and displaying
     * reasons.
     */
    public static int NUM_COMPLETED_TASKS = 20;
    
    /**
     * Gets the plane's id.
     * 
     * @return the id.
     */
    public int getId();

    /**
     * Adds a new task to the list of tasks owned by this plane
     *
     * Task addition triggers a reevaluation of the next task to be completed
     *
     * @param task to add
     */
    void addTask(Task task);
    
    /**
     * Gets the list of tasks assigned to this plane.
     * 
     * @return list of tasks.
     */
    List<Task> getTasks();

    /**
     * Set plane's the remaining battery, in seconds
     *
     * @param battery
     */
    void setBattery(long battery);
    
    /**
     * Get the plane's remianing battery in seconds
     * @return
     */
    long getBattery();
    
    /**
     * Get the plane's current angle
     * 
     * @return the angle.
     */
    double getAngle();

    /**
     * Set the plane's speed, in meters per second
     *
     * @param speed
     */
    void setSpeed(double speed);
    
    /**
     * Get the plane's speed in meters per second
     *
     * @return
     */
    double getSpeed();
    
    /**
     * Set the battery capacity (maximum battery charge in seconds)
     * 
     * @param capacity 
     */
    public void setBatteryCapacity(long capacity);
    
    /**
     * Get the battery capacity (maximum battery charge in seconds)
     * 
     * @return
     */
    public long getBatteryCapacity();
    
    /**
     * Get the completed locations.
     */
    public List<Location> getCompletedLocations();
    
    /**
     * Get the planned locations.
     */
    public List<Location> getPlannedLocations();
    
    /**
     * Get the next planned task.
     */
    public Task getNextTask();
    
    /**
     * Get the drawer for this plane.
     */
    public Drawable getDrawer();
    
    /**
     * Get the plane's color.
     */
    public Color getColor();
    
    /**
     * Set the plane's color.
     * 
     * @param color.
     */
    public void setColor(Color color);
    
}