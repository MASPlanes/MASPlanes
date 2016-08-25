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
package es.csic.iiia.planes.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration holder class.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Configuration {

    private long random_seed;
    private long duration;
    private int widthRegions;
    private int heightRegions;
    private int width;
    private int height;
    private int num_planes;
    private int num_operators;
    private int num_tasks;
    private int num_stations;
    private int num_crisis;
    private long batteryCapacity;
    private double communicationRange;
    private int blockSize;
    private double planeSpeed;
    private BufferedWriter outputFile;
    private Properties settings;
    private TaskDistributionFactory taskDistributionFactory;

    // Hotspot-related settings
    private double hotspotRadius;
    private double hotspotFreedomDegrees;

    // Generator settings
    private HashMap<String, String> generatorSettings = new HashMap<String, String>();

    private int[][] colorList = new int[][]{
        new int[]{0, 0, 0}, new int[]{29, 105, 20}, new int[]{173, 35, 35},
        new int[]{129, 38, 192}, new int[]{255, 146, 51}, new int[]{129, 197, 122},
        new int[]{42, 75, 215}, new int[]{233, 222, 187}, new int[]{129, 74, 25},
        new int[]{255, 238, 51}, new int[]{255, 205, 243}, new int[]{160, 160, 160},
        new int[]{157, 175, 255}, new int[]{41, 208, 208}, new int[]{87, 87, 87},
    };

    Configuration(Properties settings) {
        this.settings = settings;
        random_seed = Long.valueOf(fetch("random-seed"));
        duration = Long.valueOf(fetch("duration"));
        blockSize = Integer.valueOf(fetch("block-size"));
        widthRegions = Integer.valueOf(fetch("width-regions"));
        heightRegions = Integer.valueOf(fetch("height-regions"));
        num_planes = Integer.valueOf(fetch("planes"));
        num_operators = Integer.valueOf(fetch("operators"));
        num_tasks = Integer.valueOf(fetch("task-quantity"));
        num_stations = Integer.valueOf(fetch("charging-stations"));
        num_crisis = Integer.valueOf(fetch("crises"))+1;
        batteryCapacity = Long.valueOf(fetch("battery-capacity"));
        communicationRange = Integer.valueOf(fetch("communication-range"));
        planeSpeed = Double.valueOf(fetch("plane-speed"));

        // Calculate and record simulation space dimensions
        width = widthRegions*3*blockSize;
        height = heightRegions*3*blockSize;
        generatorSettings.put("width", Integer.toString(width));
        generatorSettings.put("height", Integer.toString(height));

        String td = fetch("task-distribution");
        if (td.equals("hotspot")) {
            taskDistributionFactory = new HotspotFactory();
        } else if (td.equals("uniform")) {
            taskDistributionFactory = new UniformFactory();
        } else {
            error("Unknown task-distribution \"" + td + "\"");
        }

        if (taskDistributionFactory instanceof HotspotFactory) {
            hotspotRadius = Double.valueOf(fetch("hotspot-radius"));
            hotspotFreedomDegrees = Double.valueOf(fetch("hotspot-freedom-degrees"));
        }

        // Open output file
        FileWriter fw;
        try {
            File f = new File(settings.getProperty("problem"));
            fw = new FileWriter(f);
            outputFile = new BufferedWriter(fw);
        } catch (IOException ex) {
            error("Unable to write to file \"" + settings.getProperty("problem")
                + "\": " + ex.getLocalizedMessage());
        }
    }

    private void error(String message) {
        System.err.println(message);
        System.exit(1);
    }

    private String fetch(String key) {
        if (settings.containsKey(key)) {
            String value = settings.getProperty(key).toLowerCase();
            generatorSettings.put(key, value);
            return value;
        }
        error("Missing setting \"" + key + "\".");
        return null;
    }

    public long getRandom_seed() {
        return random_seed;
    }

    public long getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidthRegions() { return widthRegions; }

    public int getHeightRegions() { return heightRegions; }

    public int getNum_planes() {
        return num_planes;
    }

    public int getNum_operators() {
        return num_operators;
    }

    public int getNum_tasks() {
        return num_tasks;
    }

    public int getNum_stations() {
        return num_stations;
    }

    public int getNum_crisis() {
        return num_crisis;
    }

    public int[] getColor(int i) {
        return colorList[i % colorList.length];
    }

    public long getBatteryCapacity() {
        return batteryCapacity;
    }

    double getCommunicationRange() {
        return communicationRange;
    }

    int getBlockSize() { return blockSize; }

    double getPlaneSpeed() {
        return planeSpeed;
    }

    public TaskDistributionFactory getTaskDistributionFactory() {
        return taskDistributionFactory;
    }

    public double getHotspotRadius() {
        return hotspotRadius;
    }

    public double getHotspotFreedomDegrees() {
        return hotspotFreedomDegrees;
    }

    public HashMap<String, String> getGeneratorSettings() {
        return generatorSettings;
    }

    public BufferedWriter getOutputFile() {
        return outputFile;
    }

}
