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

import es.csic.iiia.planes.*;

/**
 * Evaluation that computes costs based on the distance between the plane and
 * the task to be completed.
 *
 * @author Guillermo Bautista <gbau@mit.edu>
 * Created by owner on 7/29/2016.
 */
public class PercentageBatteryEvaluation implements EvaluationStrategy<Plane> {

    /**
     * Computes the cost for <em>plane</em> to perform <em>task</em>.
     * <p/>
     * The cost is computed by calculating the distance between the plane and
     * the task.
     *
     * @param plane plane that would perform the task.
     * @param task task that is being evaluated.
     * @return cost for plane to perform task.
     */
    @Override
    public double getCost(Plane plane, Task task) {
        final Location pl = plane.getLocation();
        final Location tl = task.getLocation();
        final World w = plane.getWorld();

        final long travelCost = (long)Math.ceil(pl.distance(tl)/plane.getSpeed());
        final long batteryRemaining = plane.getBattery().getEnergy() - travelCost;
        final long timeRemaining = w.getDuration() - w.getTime() - travelCost;
        final double timeToRescue = timeRemaining*w.getTimeRescuePenalty();
        final double powerToRescue = batteryRemaining*w.getRescuePowerPenalty();



        return w.getPowerFactor()*(travelCost + powerToRescue) + w.getTimeFactor()*(travelCost + timeToRescue);
    }

}
