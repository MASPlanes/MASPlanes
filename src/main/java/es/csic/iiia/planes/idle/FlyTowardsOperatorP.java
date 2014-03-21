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
package es.csic.iiia.planes.idle;

import es.csic.iiia.planes.Operator;
import es.csic.iiia.planes.Plane;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Planes using this strategy will head back to the nearest operator after waiting
 * for a random time of up to 24 hours.
 * <p/>
 * <em>Warning:</em> This idling behavior does not really help in anything.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FlyTowardsOperatorP implements IdleStrategy {
    private static final Logger LOG = Logger.getLogger(FlyTowardsOperatorP.class.getName());

    private long n_steps = 0;
    private long last_time = 0;
    private long timeout = 100;
    private boolean moving = false;
    private static Random rand = new Random(0L);

    @Override
    public boolean idleAction(Plane plane) {
        final long cur_time = plane.getWorld().getTime();

        if (cur_time != last_time + 1) {
            n_steps = 1;

            double r = rand.nextDouble();
            moving = false;
            timeout = 1 + rand.nextInt(10*60*60*24);
        } else {
            n_steps++;
        }

        if (n_steps == timeout) {
            moving = true;
        }
        last_time = cur_time;

        if (moving) {
            Operator o = plane.getWorld().getNearestOperator(plane.getLocation());
            if (plane.getLocation().getDistance(o.getLocation()) >= o.getCommunicationRange()) {
                plane.setDestination(o.getLocation());
                plane.move();
                return true;
            }
        }

        return false;
    }

}
