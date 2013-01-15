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
import es.csic.iiia.planes.evaluation.EvaluationStrategy;
import es.csic.iiia.planes.evaluation.IndependentDistanceBatteryEvaluation;
import es.csic.iiia.planes.evaluation.IndependentDistanceEvaluation;
import es.csic.iiia.planes.maxsum.MSIndependentPlaneNode;
import es.csic.iiia.planes.maxsum.MSPlane;
import es.csic.iiia.planes.maxsum.MSPlaneNode;
import es.csic.iiia.planes.maxsum.MSWorkloadPlaneNode;
import es.csic.iiia.planes.operator_behavior.Nearest;
import es.csic.iiia.planes.operator_behavior.NearestInRange;
import es.csic.iiia.planes.operator_behavior.OperatorStrategy;
import es.csic.iiia.planes.operator_behavior.Random;
import es.csic.iiia.planes.operator_behavior.RandomInRange;
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

    /**
     * Class of the evaluation strategy used by the planes in this simulation.
     */
    public final Class<? extends EvaluationStrategy> evaluationClass;

    /* AUCTIONS specific stuff */
    public final int aucEvery;

    /* MAXSUM specific stuff */
    public final int msIterations;
    public final int msStartEvery;
    public final Class<? extends MSPlaneNode> msPlaneNodeType;
    public final double msWorkloadK;

    public Configuration(Properties settings) {

        String value = settings.getProperty("operator-strategy");
        if (value.equalsIgnoreCase("nearest")) {
            operatorStrategy = new Nearest();
        } else if (value.equalsIgnoreCase("random")) {
            operatorStrategy = new Random();
        } else if (value.equalsIgnoreCase("nearest-inrange")) {
            operatorStrategy = new NearestInRange();
        } else if (value.equalsIgnoreCase("random-inrange")) {
            operatorStrategy = new RandomInRange();
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

        value = settings.getProperty("task-evaluation");
        if (value.equalsIgnoreCase("independent-distance")) {
            evaluationClass = IndependentDistanceEvaluation.class;
        } else if (value.equalsIgnoreCase("independent-distance-battery")) {
            evaluationClass = IndependentDistanceBatteryEvaluation.class;
        } else {
            throw new IllegalArgumentException("Illegal task evaluation strategy \"" + value + "\".");
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

        // Auctions settings
        aucEvery = Integer.valueOf(settings.getProperty("auction-every"));

        // Max-sum settings
        msIterations = Integer.valueOf(settings.getProperty("maxsum-iterations"));
        msStartEvery = Integer.valueOf(settings.getProperty("maxsum-start-every"));
        value = settings.getProperty("maxsum-planes-function");
        if (value.equalsIgnoreCase("independent")) {
            msPlaneNodeType = MSIndependentPlaneNode.class;
        } else if (value.equalsIgnoreCase("workload")) {
            msPlaneNodeType = MSWorkloadPlaneNode.class;
        } else {
            throw new IllegalArgumentException("Illegal maxsum planes function type \"" + value + "\".");
        }
        msWorkloadK = Double.valueOf(settings.getProperty("maxsum-workload-k"));

    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("###### Settings:\n")
            .append("# gui = ").append(gui).append("\n")
            .append("# quiet = ").append(quiet).append("\n")
            .append("# problem = ").append(problemFile).append("\n")
            .append("# operator = ").append(operatorStrategy.getClass().getSimpleName()).append("\n")
            .append("# planes = ").append(planesClass.getSimpleName()).append("\n")
            .append("# task-evaluation = ").append(evaluationClass.getSimpleName()).append("\n")
            .append("# auction-every = ").append(aucEvery).append("\n")
            .append("# maxsum-start-every = ").append(msStartEvery).append("\n")
            .append("# maxsum-iterations = ").append(msIterations).append("\n")
            .append("# maxsum-planes-function = ").append(msPlaneNodeType.getSimpleName()).append("\n")
            .append("# maxsum-workload-k = ").append(msWorkloadK).append("\n")
        ;

        return buf.toString();
    }

}