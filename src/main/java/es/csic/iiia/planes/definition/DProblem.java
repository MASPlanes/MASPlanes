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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Definition of a problem.
 * <p/>
 * The definition of a problem or scenario includes:
 * <ul>
 * <li>The world's properties (width, height, and duration)
 * <li>An enumeration of all the planes, with their initial locations and
 *     maximum speeds.
 * <li>An enumeration of all the tasks that will be submitted throghout the
 *     simulation.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class DProblem {

    private int width = 1000;
    private int height = 1000;

    private int widthRegions = 100;
    private int heightRegions = 100;
    private int blockSize = 3;

    private long duration = 3600*24*30;
    private int nCrisis = 5;
    private ArrayList<DOperator> operators = new ArrayList<DOperator>();
    private ArrayList<DPlane> planes = new ArrayList<DPlane>();
    private ArrayList<DStation> stations = new ArrayList<DStation>();
    private HashMap<String, String> generatorSettings = new HashMap<String, String>();

    /**
     * Get the blockSize of the simulation space.
     *
     * @return blockSize of the simulation space.
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Set the blockSize of the simulation space.
     *
     * @param blockSize of the simulation space.
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Get the widthRegions of the simulation space.
     *
     * @return widthRegions of the simulation space.
     */
    public int getWidthRegions() {
        return widthRegions;
    }

    /**
     * Set the widthRegions of the simulation space.
     *
     * @param widthRegions of the simulation space.
     */
    public void setWidthRegions(int widthRegions) {
        this.widthRegions = widthRegions;
    }

    /**
     * Get the heightRegions of the simulation space.
     *
     * @return heightRegions of the simulation space.
     */
    public int getHeightRegions() {
        return heightRegions;
    }

    /**
     * Set the heightRegions of the simulation space.
     *
     * @param heightRegions of the simulation space.
     */
    public void setHeightRegions(int heightRegions) {
        this.heightRegions = heightRegions;
    }

    /**
     * Get the width of the simulation space.
     *
     * @return width of the simulation space.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the width of the simulation space.
     *
     * @param width of the simulation space.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the height of the simulation space.
     *
     * @return height of the simulation space.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the height of the simulation space.
     *
     * @param height of the simulation space.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the duration of this scenario.
     *
     * @return duration of this scenario (in tenths of second).
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Get the number of crisis in this problem.
     *
     * @return number of crisis in this problem.
     */
    public int getnCrisis() {
        return nCrisis;
    }

    /**
     * Set the number of crisis in this problem.
     *
     * @param nCrisis number of crisis.
     */
    public void setnCrisis(int nCrisis) {
        this.nCrisis = nCrisis;
    }

    /**
     * Set the duration of this scenario.
     *
     * @param duration of this scenario (in tenths of second).
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Get a list of the operators in this scenario.
     *
     * @return list of operators in this scenario.
     */
    public ArrayList<DOperator> getOperators() {
        return operators;
    }

    /**
     * Set the list of operators in this scenario.
     *
     * @param operators list of operators to set.
     */
    public void setOperators(ArrayList<DOperator> operators) {
        this.operators = operators;
    }


    /**
     * Get a list of the planes in this scenario.
     *
     * @return list of planes in this scenario.
     */
    public ArrayList<DPlane> getPlanes() {
        return planes;
    }

    /**
     * Set the list of planes in this scenario.
     *
     * @param planes list of planes to set.
     */
    public void setPlanes(ArrayList<DPlane> planes) {
        this.planes = planes;
    }

    /**
     * Get the list of recharging stations in this scenario.
     *
     * @return list of recharging stations.
     */
    public ArrayList<DStation> getStations() {
        return stations;
    }

    /**
     * Set the list of recharging stations in this scenario.
     *
     * @param stations list of recharging stations to set.
     */
    public void setStations(ArrayList<DStation> stations) {
        this.stations = stations;
    }

    /**
     * Get the generator settings used to generate this problem.
     *
     * @return generator settings used to generate this problem.
     */
    public HashMap<String, String> getGeneratorSettings() {
        return generatorSettings;
    }

    /**
     * Set the generator settings used to generate this problem.
     *
     * @param generatorSettings generator settings used to generate this problem.
     */
    public void setGeneratorSettings(HashMap<String, String> generatorSettings) {
        this.generatorSettings = generatorSettings;
    }
}