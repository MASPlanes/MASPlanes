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

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.MessagingAgent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class HungarianMethodAllocation extends AbstractAllocationStrategy {
    private static final Logger LOG = Logger.getLogger(HungarianMethodAllocation.class.getName());

    @Override
    public String getName() {
        return "hungarian";
    }

    @Override
    public String getDescription() {
        return "Allocates tasks using the Hungarian method.";
    }

    @Override
    public void allocate(
        World world,
        OmniscientPlane[] planes,
        TreeMap<MessagingAgent, Set<Task>> visibilityMap,
        TreeMap<OmniscientPlane, Task> assignmentMap,
        TreeMap<Task, OmniscientPlane> reverseMap)
    {
        List<Task> tasks = world.getTasks();
        final double maxWeight = world.getSpace().getDimension().height + world.getSpace().getDimension().width;

        // Remove tasks that can not be seen by any plane
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            final Task t = it.next();
            boolean visible = false;
            for (OmniscientPlane p : planes) {
                if (visibilityMap.get(p).contains(t)) {
                    visible = true;
                    break;
                }
            }
            if (!visible) {
                it.remove();
            }
        }

        // Compute the cost matrix
        double[][] costMatrix = new double[planes.length][tasks.size()+planes.length];
        int i=0;
        for (OmniscientPlane p : planes) {
            Set<Task> visibles = visibilityMap.get(p);
            Arrays.fill(costMatrix[i], Double.POSITIVE_INFINITY);

            for (int j=0, len=tasks.size(); j<len; j++) {
                final Task t = tasks.get(j);

                if (visibles.contains(t)) {
                    costMatrix[i][j] = distance(p, t);
                } else {
                    costMatrix[i][j] = Double.POSITIVE_INFINITY;
                }
            }

            // The task ntasks+i is a special task to say "plane i does nothing".
            costMatrix[i][tasks.size()+i] = maxWeight;

            i++;
        }

        // Solve the allocation problem
        LOG.fine("Cost matrix:" + Arrays.deepToString(costMatrix));
        HungarianAlgorithm algorithm = new HungarianAlgorithm(costMatrix, maxWeight);
        int[] result = algorithm.execute();
        if (result.length != planes.length) {
            LOG.severe("Unexpected output from the hungarian algorithm");
            System.exit(1);
        }
        LOG.fine("result : " + Arrays.toString(result));

        // Translate the result to assignments
        i=0; reverseMap.clear();
        for (OmniscientPlane p : planes) {
            int task = result[i];
            if (task < 0 || task >= tasks.size()) {
                assignmentMap.remove(p);
            } else {
                assignmentMap.put(p, tasks.get(task));
                reverseMap.put(tasks.get(task), p);
            }

            i++;
        }

    }



}
