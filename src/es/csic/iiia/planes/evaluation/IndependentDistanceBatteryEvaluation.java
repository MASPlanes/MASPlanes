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
package es.csic.iiia.planes.evaluation;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;

/**
 * Evaluation that computes costs based on the distance between the plane and
 * the task to be completed, considering the plane's current battery level.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class IndependentDistanceBatteryEvaluation implements EvaluationStrategy<Plane> {

    /**
     * Computes the cost for <em>plane</em> to perform <em>task</em>.
     * <p/>
     * The cost is computed by calculating the distance between the plane and
     * the task. The plane's battery is taken into account through, by
     * adding the distance required to go to charge first if the task can not
     * be completed with the current amount of battery of the plane.
     *
     * @param plane plane that would perform the task.
     * @param task task that is being evaluated.
     * @return cost for plane to perform task.
     */
    @Override
    public double getCost(Plane plane, Task task) {
        final World world = plane.getWorld();
        final Location pl = plane.getLocation();
        final Location tl = task.getLocation();
        final Location sl = world.getNearestStation(tl).getLocation();

        final double plane2task   = pl.distance(tl);
        final double task2station = tl.distance(sl);

        // Reject tasks when not ready
        if (plane.getState() != Plane.State.NORMAL) {
            return Double.MAX_VALUE;
        }

        // Battery required to fulfill the task before recharging
        double reqBattery = (long)((plane2task + task2station) / plane.getSpeed());
        if (plane.getBattery().getEnergy() > reqBattery) {
            return plane2task;
        }

        // The plane can't fulfill the task without recharging, so reject it.
        return Double.MAX_VALUE;
    }

}
