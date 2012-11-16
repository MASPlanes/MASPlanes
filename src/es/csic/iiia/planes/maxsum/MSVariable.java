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
package es.csic.iiia.planes.maxsum;

import es.csic.iiia.planes.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSVariable {

    private MSPlane plane;

    private double[] belief;

    private Map<Task, MSPlane> domain;

    private Map<Task, MSFunction2VariableMessage> lastMessages =
            new TreeMap<Task, MSFunction2VariableMessage>();

    public MSVariable(MSPlane plane) {
        this.plane = plane;
    }

    public void update(Map<Task, MSPlane> domain) {
       this.domain = domain;
    }

    public void gather() {
        belief = new double[domain.size()];

        int i=0;
        for (Task t : domain.keySet()) {

            belief[i] = plane.getCost(t);
            MSFunction2VariableMessage msg = lastMessages.get(t);
            belief[i] += msg.getValue();

            i++;
        }

    }

    public void scatter() {

        // Compute the two minimum values
        int[] midx = {-1, -1};
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE};
        for (int i=0; i<belief.length; i++) {

            if (belief[i] < min[0]) {

                // Minimum value so far
                min[1] = min[0]; midx[1] = midx[0];
                min[0] = belief[i]; midx[0] = i;

            } else if (belief[i] < min[1]) {

                // Second minimum so far
                min[1] = belief[i]; midx[1] = i;

            }

        }

        int i = 0;
        for (Task t : domain.keySet()) {

            final double value = plane.getCost(t) - min[i == midx[0] ? 1 : 0];
            MSVariable2FunctionMessage msg = new MSVariable2FunctionMessage(t);
            msg.setRecipient(domain.get(t));
            plane.send(msg);

        }

    }

    public Task makeDecision() {
        throw new RuntimeException("Not implemented yet.");
    }

}