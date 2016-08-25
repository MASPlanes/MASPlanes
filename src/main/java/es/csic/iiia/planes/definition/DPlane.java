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
package es.csic.iiia.planes.definition;

import es.csic.iiia.planes.Plane;

/**
 * Definition of a Plane.
 *
 * @see Plane
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class DPlane extends DLocation {
    private double speed;
    private long batteryCapacity;
    private long initialBattery;
    private int[] color;
    private double communicationRange;
    private double searchRange;

    /**
     * Get the color of this plane.
     *
     * The color is defined as an array of exactly three integers, containing
     * the amount of red, green, and blue (RGB) in a 0-255 scale.
     *
     * @return color of this plane, as an RGB triplet.
     */
    public int[] getColor() {
        return color;
    }

    /**
     * Set the color of this plane.
     *
     * @see #getColor() on the format of the color parameter.
     *
     * @param color to set.
     */
    public void setColor(int[] color) {
        this.color = color;
    }

    /**
     * Get the speed of this plane, in meters per tenth of second.
     *
     * @return speed of this plane, in meters per tenth of second.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the speed of this plane
     *
     * @param speed of this plane in meters per tenth of second.
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Get the battery capacity of this plane.
     *
     * At the beggining of a simulation, all of the planes are supposed to be
     * fully charged.
     *
     * @return battery capacity of this plane.
     */
    public long getBatteryCapacity() {
        return batteryCapacity;
    }

    /**
     * Set the battery capacity of this plane.
     *
     * @param battery capacity of this plane.
     */
    public void setBatteryCapacity(long battery) {
        this.batteryCapacity = battery;
    }

    /**
     * Get the communication range of this plane.
     *
     * @return the communication range of this plane.
     */
    public double getCommunicationRange() {
        return communicationRange;
    }

    /**
     * Set the communication range of this plane.
     *
     * @param communicationRange communication range to set.
     */
    public void setCommunicationRange(double communicationRange) {
        this.communicationRange = communicationRange;
    }

    /** Modified by Guillermo B.:
     * Get the search range of this plane.
     *
     * @return the search range of this plane.
     */
    public double getSearchRange() { return searchRange; }

    /** Modified by Guillermo B.:
     * Set the search range of this plane.
     *
     * @param searchRange communication range to set.
     */
    public void setSearchRange(double searchRange) { this.searchRange = searchRange; }

    /**
     * Get the initial amount of battery of this plane.
     *
     * @return initial amount of battery of this plane.
     */
    public long getInitialBattery() {
        return initialBattery;
    }

    /**
     * Set the initial amount of battery of this plane.
     *
     * @param initialBattery initial amount of battery of this plane.
     */
    public void setInitialBattery(long initialBattery) {
        this.initialBattery = initialBattery;
    }
}