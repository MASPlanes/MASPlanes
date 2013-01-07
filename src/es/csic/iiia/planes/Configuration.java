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
package es.csic.iiia.planes;

import es.csic.iiia.planes.auctions.AuctionPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.maxsum.MSPlane;
import es.csic.iiia.planes.operator_behavior.Nearest;
import es.csic.iiia.planes.operator_behavior.OperatorStrategy;
import es.csic.iiia.planes.operator_behavior.Random;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Holds the configuration settings of the simulator.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Configuration {

    /**
     * True if running with a graphical display, false otherwise.
     */
    public final boolean gui;

    /**
     * True if running in "quiet" mode (no output except for final
     * statistics & errors)
     */
    public final boolean quiet;

    /**
     * Problem's file name.
     */
    public final String problemFile;

    /**
     * Pointer to the problem definition of the problem (scenario definition)
     * being simulated.
     */
    public final DProblem problemDefinition;

    /**
     * Strategy used by the {@link Operator} to decide to which plane it will
     * submit the task.
     */
    public final OperatorStrategy operatorStrategy;

    /**
     * Class of planes used by this simulation.
     *
     * Because different planes use different solving strategies, changing their
     * class is how the simulator runs one solving algorithm or another.
     */
    public final Class<? extends Plane> planesClass;

    public Configuration(Properties settings) {

        String value = settings.getProperty("operator-strategy");
        if (value.equalsIgnoreCase("nearest")) {
            operatorStrategy = new Nearest();
        } else if (value.equalsIgnoreCase("random")) {
            operatorStrategy = new Random();
        } else {
            throw new IllegalArgumentException("Illegal operator strategy \"" + value + "\".");
        }

        value = settings.getProperty("planes");
        if (value.equalsIgnoreCase("auction")) {
            planesClass = AuctionPlane.class;
        } else if (value.equalsIgnoreCase("none")) {
            planesClass = DefaultPlane.class;
        } else if (value.equalsIgnoreCase("maxsum")) {
            planesClass = MSPlane.class;
        } else {
            throw new IllegalArgumentException("Illegal plane strategy \"" + value + "\".");
        }

        value = settings.getProperty("gui");
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            gui = true;
        } else {
            gui = false;
        }

        value = settings.getProperty("quiet");
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            quiet = true;
        } else {
            quiet = false;
        }

        DProblem d = new DProblem();
        ObjectMapper mapper = new ObjectMapper();
        problemFile = settings.getProperty("problem");
        try {
            d = mapper.readValue(new File(problemFile), DProblem.class);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error reading problem file \"" + problemFile + "\"");
        }
        problemDefinition = d;

    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{\n")
            .append("\tGUI:      ").append(gui).append("\n")
            .append("\tQuiet:    ").append(quiet).append("\n")
            .append("\tProblem:  ").append(problemFile).append("\n")
            .append("\tOperator: ").append(operatorStrategy.getClass().getSimpleName()).append("\n")
            .append("\tPlanes:   ").append(planesClass.getSimpleName()).append("\n")
        .append("}");

        return buf.toString();
    }

}