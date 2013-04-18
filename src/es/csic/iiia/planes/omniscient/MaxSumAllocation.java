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

import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.maxsum.algo.CostFactor;
import es.csic.iiia.planes.maxsum.algo.WorkloadFactor;
import es.csic.iiia.planes.maxsum.algo.WorkloadFunction;
import es.csic.iiia.planes.maxsum.algo.Factor;
import es.csic.iiia.planes.maxsum.algo.SelectorFactor;
import es.csic.iiia.planes.maxsum.algo.KAlphaFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MaxSumAllocation extends AbstractAllocationStrategy {

    @Override
    public void allocate(
        World w,
        OmniscientPlane[] planes,
        TreeMap<MessagingAgent, Set<Task>> visibilityMap,
        TreeMap<OmniscientPlane, Task> assignmentMap,
        TreeMap<Task, OmniscientPlane> reverseMap)
    {
        // Create the workload cost function
        WorkloadFunction workloadFunction = new KAlphaFunction(
                w.getFactory().getConfiguration().getMsWorkloadK(),
                w.getFactory().getConfiguration().getMsWorkloadAlpha()
        );

        List<Factor> factors = new ArrayList<Factor>();

        // Create a selector factor for each task
        Map<Task, SelectorFactor> selectors = new HashMap<Task, SelectorFactor>();
        for (Task t : w.getTasks()) {
            final SelectorFactor s = new SelectorFactor();
            selectors.put(t, s);
            factors.add(s);
        }

        // Create a cost factor for each plane
        Map<WorkloadFactor, OmniscientPlane> cost2plane =
                new HashMap<WorkloadFactor, OmniscientPlane>();
        for (OmniscientPlane p : planes) {
            final WorkloadFactor c = new WorkloadFactor();
            c.setFunction(workloadFunction);
            factors.add(c);
            cost2plane.put(c, p);

            // Now link it with all the selectors of the tasks it can see
            for(Task t : visibilityMap.get(p)) {
                SelectorFactor s = selectors.get(t);
                s.addNeighbor(c);
                c.addNeighbor(s);
                c.setPotential(s, p.getCost(t));
            }
        }

        // Run maxsum!
        final int n = w.getFactory().getConfiguration().getMsIterations();
        for (int i=0; i<n; i++) {
            for (Factor f : factors) f.tick();
            for (Factor f : factors) f.run();
        }

        // Fetch the assignments
        assignmentMap.clear();
        reverseMap.clear();
        for (Task t : w.getTasks()) {
            final SelectorFactor s = selectors.get(t);
            final OmniscientPlane p = cost2plane.get(s.select());

            // Assign the plane to this task if its current assignment is
            // none or worse
            if (( !assignmentMap.containsKey(p))
               || p.getCost(assignmentMap.get(p)) > p.getCost(t) )
            {
                assignmentMap.put(p, t);
                reverseMap.put(t, p);
            }
        }

    }

}
