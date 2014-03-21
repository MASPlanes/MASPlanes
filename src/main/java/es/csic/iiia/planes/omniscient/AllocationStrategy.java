/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.planes.omniscient;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.MessagingAgent;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Interface that defines an allocation strategy for the omniscient
 * "God".
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface AllocationStrategy {

    /**
     * Compute an allocation of planes to requests.
     *
     * @param world simulation world state.
     * @param planes list of all planes in the simulation.
     * @param visibilityMap map that represents the plane's knowledge about tasks. Each key in
     *                      this map is a plane, and the corresponding entry is a set of all the
     *                      tasks of which the pane is aware.
     * @param assignmentMap map of plane to task assignments. This must be filled by the
     *                      implementing class (however it sees fit).
     * @param reverseMap map of task to plane assigments. This is the reverse of
     *                   <em>assignmentMap</em> and has also to be filled by the implementor.
     */
    public void allocate(
            World world,
            OmniscientPlane[] planes,
            TreeMap<MessagingAgent, Set<Task>> visibilityMap,
            TreeMap<OmniscientPlane, Task>      assignmentMap,
            TreeMap<Task, OmniscientPlane>      reverseMap);

    public List<Location> getPlannedLocations(OmniscientPlane plane);

    /**
     * Get the name of this allocation strategy.
     *
     * This is the identifier employed in the configuration settings file.
     *
     * @return name of this allocation strategy.
     */
    public String getName();

    /**
     * Get the description of this allocation strategy.
     *
     * The description is displayed in the generated configuration settings file.
     *
     * @return the description of this allocation strategy.
     */
    public String getDescription();

}
