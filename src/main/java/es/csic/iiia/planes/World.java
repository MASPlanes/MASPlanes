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

import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.messaging.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the world where a simulation runs.
 *
 * This is the object that keeps track of all the elements participating in the
 * simulation, and that synchronizes their actions.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface World extends Runnable {

    /**
     * Get the factory used to build elements for this simulation.
     *
     * @return the factory.
     */
    Factory getFactory();

    /**
     * Add a new plane to this world.
     *
     * @param p plane to add.
     */
    public void addPlane(Plane p);

    /**
     * Get the duration of the World for this simulation.
     *
     * @return the duration.
     */
    public long getDuration();

    /**
     * Get the list of planes in this simulation.
     *
     * @return list of the simulation's planes.
     */
    List<Plane> getPlanes();

    /**
     * Get the {@link Space} of this simulation.
     *
     * @return the {@link Space} of this simulation.
     */
    Space getSpace();

    /**
     * Get the {@link Block} of this simulation.
     *
     * @return the {@link Block} of this simulation.
     */
    Block[][] getBlocks();

    /**
     * Get the {@link Region} of this simulation.
     *
     * @return the {@link Region} of this simulation.
     */
    ArrayList<Region> getRegions();

    /**
     * Add a chargin station.
     *
     * @param station to be added.
     */
    void addStation(Station station);

    /**
     * Get the charging station that is closest to the given location.
     *
     * @param location where an element is querying from.
     * @return the recharging station that is closest to the given location.
     */
    Station getNearestStation(Location location);

    /**
     * Get the operator that is closest to the given location.
     *
     * @param location where an element is querying from.
     * @return the operator that is closest to the given location.
     */
    Operator getNearestOperator(Location location);

    /**
     * Add a new task to the world.
     *
     * The world keeps track of the tasks just for counting purposes.
     * {@link Plane}s must not obtain information about the tasks from here.
     * Instead, it is the {@link Operator}'s responsibility to communicate any
     * required tasks to the planes.
     *
     * @param task to add.
     */
    void addTask(Task task);

    /**
     * Remove the given task from the world.
     *
     * The world keeps track of the tasks just for counting purposes.
     * The {@link Plane}s must notify to the world that a Task has been
     * completed by calling this method.
     *
     * @param task to remove.
     */
    void removeTask(Task task);

    //TODO: comment what this does
    void foundTask(Task task);

    void removeExpired(Task task);

    /**
     * Get the list of tasks in the world.
     *
     * This method should not be used by any simulation agent, unless it is
     * considered as being omniscient.
     */
    List<Task> getTasks();

    //TODO: Explain this
    List<Block> getUnassignedBlocks();

    Block[][] getBlockGrid();

    List<SARPlane> getStandbyAvailable();

    boolean sendStandby(Block b);

    double getTimeRescuePenalty();

    double getRescuePowerPenalty();

    double getPowerFactor();

    double getTimeFactor();

    /**
     * Get the current simulation time (in tenths of second).
     *
     * @return current simulation time (in tenths of second).
     */
    long getTime();

    /**
     * Initialize the simulation according to the given problem definition.
     *
     * @param d problem definition.
     */
    void init(DProblem d);

    /**
     * Sends a message to all {@link MessagingAgent}s in range of the sender.
     *
     * @see Message#getSender()
     * @see MessagingAgent#getCommunicationRange()
     *
     * @param message to be sent.
     */
    public void sendMessage(Message message);

    /**
     * Set the duration of this scenario (in tenths of second).
     *
     * @param duration of this scenario in tenths of second.
     */
    public void setDuration(long duration);

    public List<Operator> getOperators();

}