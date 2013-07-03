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

import java.awt.geom.Point2D;

/**
 * Represents a point in the world's space.
 *
 * @see World
 * @see Space
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Location extends Point2D {

    private double x;
    private double y;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Location(Point2D point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    /**
     * Moves this location towards the given destination, at speed meters per
     * millisecond.
     *
     * @param destination
     * @param speed
     * @return true if the destination has been reached, false otherwise.
     *
    public boolean move(Location destination, double speed) {
        if (destination == null) {
            return true;
        }

        double dx = destination.x - x;
        double dy = destination.y - y;
        double alpha = Math.atan2(dy, dx);
        double incx = speed * Math.cos(alpha);
        double incy = speed * Math.sin(alpha);

        if (Math.abs(incx) >= Math.abs(dx) &&
            Math.abs(incy) >= Math.abs(dy)){
            x = destination.x;
            y = destination.y;
            return true;
        }

        if (Math.abs(incx) >= Math.abs(dx)) {
            x = destination.x;
        } else {
            x += incx;
        }
        if (Math.abs(incy) >= Math.abs(dy)) {
            y = destination.y;
        } else {
            y += incy;
        }

        return false;
    }*/

    /**
     * Continue advancing along a MoveStep plan built with 
     * {@link #buildMoveStep(Location, double)}.
     * 
     * @param step movestep plan to follow.
     * @return True if the destination has been reached, or false otherwise.
     */
    public boolean move(MoveStep step) {
        return step.move();
    }

    /**
     * Returns the movestep plan to reach the desired destination.
     * <p/>
     * Because it is usually needed to advance during multiple steps (tenths of
     * second) to reach the desired destination, this function returns a
     * MoveStep object. Then, the subsequent steps can be made using
     * {@link #move(es.csic.iiia.planes.Location.MoveStep)}, which is more
     * efficient.
     *
     * @see MoveStep
     * @param destination
     * @param speed
     * @return MoveStep to reach the given destination.
     */
    public MoveStep buildMoveStep(Location destination, double speed) {
        if (destination == null) {
            return null;
        }

        return new MoveStep(destination, speed);
    }

    @Override
    public double getX() {
        return x;
    }
    public int getXInt() {
        return (int)x;
    }

    @Override
    public double getY() {
        return y;
    }
    public int getYInt() {
        return (int)y;
    }

    public double getDistance(Location l) {
        double dx = x - l.x;
        double dy = y - l.y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public boolean within(double range, Location l) {
        double dx = x - l.x;
        if (dx > range) {
            return false;
        }

        double dy = y - l.y;
        if (dy > range) {
            return false;
        }

        return Math.sqrt(dx*dx + dy*dy) >= range;
    }

    @Override public String toString() {
        return "(" + getXInt() + "," + getYInt() + ")";
    }

    public double getAngle(Location destination) {
        double dx = x - destination.x;
        double dy = y - destination.y;
        return Math.atan2(dy, dx);
    }

    @Override
    public void setLocation(double d, double d1) {
        x = d;
        y = d1;
    }

    public class MoveStep {
        public final double dx;
        public final double dy;
        public final double alpha;
        public final double incx;
        public final double incy;
        public final Location destination;
        public int steps;

        public MoveStep(Location destination, double speed) {
            this.destination = destination;
            dx = destination.x - x;
            dy = destination.y - y;
            alpha = Math.atan2(dy, dx);
            incx = speed * Math.cos(alpha);
            incy = speed * Math.sin(alpha);

            final double distance = getDistance(destination);
            steps = (int)Math.ceil(distance/speed);
        }

        protected boolean move() {
            steps--;
            if (steps <= 0) {
                x = destination.x;
                y = destination.y;
                return true;
            }

            x += incx;
            y += incy;
            return false;
        }
    }
}