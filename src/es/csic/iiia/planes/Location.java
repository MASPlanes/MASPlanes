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
 *
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
     * True if the destination has ben reached, false otherwise.
     * 
     * @param destination
     * @param speed
     * @return 
     */
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
    }
    
    public double getX() {
        return x;
    }
    public int getXInt() {
        return (int)x;
    }
    
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
}
