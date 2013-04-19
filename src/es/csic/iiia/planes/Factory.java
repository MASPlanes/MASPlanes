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

import es.csic.iiia.planes.cli.Configuration;
import es.csic.iiia.planes.definition.DTask;
import es.csic.iiia.planes.maxsum.centralized.CostFactor;
import java.util.List;

/**
 * Factory used to build all of the simulation's participants.
 *
 * Every element participating in the simulation is instantiated through this
 * factory, which is the responsible of "gluing" them together.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface Factory {

    /**
     * Get the configuration object of this simulation.
     *
     * @return the configuration of this simulation.
     */
    public Configuration getConfiguration();

    /**
     * Builds an {@link Operator}.
     *
     * The created operator will create and submit tasks according to the given
     * list of task definitions.
     *
     * @param location location where this operator is positioned.
     * @param taskDefinitions list of task definitions,
     * @return Operator newly build Operator.
     */
    public Operator buildOperator(Location location, List<DTask> taskDefinitions);

    /**
     * Builds a {@link Plane}.
     *
     * @param location initial location of the plane.
     * @return newly built plane.
     */
    public Plane buildPlane(Location location);

    /**
     * Builds a {@link Battery} for the specified plane.
     *
     * @param plane the plane that will use this battery.
     * @return newly built battery.
     */
    public Battery buildBattery(Plane plane);

    /**
     * Builds a charging {@link Station}.
     *
     * @param location location of the recharging station.
     * @return newly built station.
     */
    public Station buildStation(Location location);

    /**
     * Builds a new {@link Task}.
     *
     * The task's submission time is automatically set to the current simulation
     * time.
     *
     * @see Task#getSubmissionTime()
     *
     * @param location that must be visited by some plane.
     * @return newly built task.
     */
    public Task buildTask(Location location);

    /**
     * Builds a new {@link World}.
     * @return newly built World;
     */
    public World buildWorld();

    /**
     * Builds a new {@link  CostFactor} for a plane.
     * @return newly build cost factor.
     */
    public CostFactor buildCostFactor(Plane plane);

}