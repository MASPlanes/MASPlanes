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
package es.csic.iiia.planes.cli;

import es.csic.iiia.bms.Factor;
import es.csic.iiia.planes.*;
import es.csic.iiia.planes.auctions.bidding.*;
import es.csic.iiia.planes.evaluation.PercentageBatteryEvaluation;
import es.csic.iiia.planes.operator_behavior.*;
import it.univr.ia.planes.dsa.DSAPlane;
import es.csic.iiia.planes.Battery;
import es.csic.iiia.planes.DefaultBattery;
import es.csic.iiia.planes.DefaultPlane;
import es.csic.iiia.planes.InfiniteBattery;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.auctions.AuctionPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.evaluation.EvaluationStrategy;
import es.csic.iiia.planes.evaluation.IndependentDistanceBatteryEvaluation;
import es.csic.iiia.planes.evaluation.IndependentDistanceEvaluation;
import es.csic.iiia.planes.idle.DoNothing;
import es.csic.iiia.planes.idle.FlyTowardsOperator;
import es.csic.iiia.planes.idle.FlyTowardsOperatorP;
import es.csic.iiia.planes.idle.IdleStrategy;
import es.csic.iiia.planes.maxsum.centralized.CostFactorFactory;
import es.csic.iiia.planes.maxsum.centralized.IndependentFactory;
import es.csic.iiia.planes.maxsum.centralized.KAlphaFactory;
import es.csic.iiia.planes.maxsum.centralized.WorkloadFactory;
import es.csic.iiia.planes.maxsum.centralized.WorkloadFunctionFactory;
import es.csic.iiia.planes.maxsum.distributed.MSPlane;
import es.csic.iiia.planes.omniscient.AllocationStrategy;
import es.csic.iiia.planes.omniscient.HungarianMethodAllocation;
import es.csic.iiia.planes.omniscient.IncrementalSSIAllocation;
import es.csic.iiia.planes.omniscient.IndependentAuctionAllocation;
import es.csic.iiia.planes.omniscient.MaxSumAllocation;
import es.csic.iiia.planes.omniscient.NaiveAdhocAllocation;
import es.csic.iiia.planes.omniscient.NofirstSSIAllocation;
import es.csic.iiia.planes.omniscient.Omniscient;
import es.csic.iiia.planes.omniscient.OmniscientPlane;
import es.csic.iiia.planes.omniscient.SSIAllocation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Holds the configuration settings of the simulator.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public final class Configuration {

    /**
     * True if running with a graphical display, false otherwise.
     */
    private boolean gui;

    /**
     * True if running in "quiet" mode (no output except for final
     * statistics & errors)
     */
    private boolean quiet;

    /**
     * Problem's file name.
     */
    private String problemFile;

    /**
     * Pointer to the problem definition of the problem (scenario definition)
     * being simulated.
     */
    private DProblem problemDefinition;

    /**
     * Strategy used by the {@link es.csic.iiia.planes.Operator} to decide to which
     * plane it will submit the task.
     */
    private OperatorStrategy operatorStrategy;

    /**
     * Class of planes used by this simulation.
     *
     * Because different planes use different solving strategies, changing their
     * class is how the simulator runs one solving algorithm or another.
     */
    private Class<? extends Plane> planesClass;

    /**
     * Class of allocation used by the omniscient god.
     */
    private Class<? extends AllocationStrategy> omniscientAllocationStrategy;

    /**
     * Class of the battery used by the planes.
     */
    private Class<? extends Battery> batteryClass;

    /**
     * Class of the idle strategy used by the planes in this simulation.
     */
    private Class<? extends IdleStrategy> idleClass;

    /**
     * Class of the evaluation strategy used by the planes in this simulation.
     */
    private Class<? extends EvaluationStrategy<Plane>> evaluationClass;

    /* AUCTIONS specific stuff */
    private int aucEvery;
    private BiddingRuleFactory aucBiddingRuleFactory;
    private double aucWorkloadK;
    private double aucWorkloadAlpha;

    /* MAXSUM specific stuff */
    private int msIterations;
    private int msStartEvery;
    private double msWorkloadK;
    private double msWorkloadAlpha;

    /* DSA specific stuff */
    private int dsaIterations;
    private int dsaEvery;
    private double dsaP;
    private double dsaWorkloadK;
    private double dsaWorkloadAlpha;
    private String dsaEvaluationFunction;

    /* LIAM specific stuff */
    private double eaglePower;
    private double standbyPower;
    private double scoutSpeed;
    private double eagleSpeed;
    private double scoutJumpDistance;
    private double eagleJumpDistance;
    private int eagleCrowdDistance;

    private LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
    private CostFactorFactory<Factor<?>> msCostFactorFactory;
    private WorkloadFunctionFactory msWorkloadFunctionFactory;

    private <T> T fetch(Properties settings, Map<String, T> map, String key) {
        String value = settings.getProperty(key).toLowerCase();
        if (map.containsKey(value)) {
            values.put(key, value);
            return map.get(value);
        }
        throw new IllegalArgumentException("Illegal " + key + " \"" + value + "\".");
    }

    public Configuration(Properties settings) {
        operatorStrategy = fetch(settings, getOperatorStrategies(), "operator-strategy");
        planesClass = fetch(settings, getPlaneClasses(), "planes");

        if (values.get("operator-strategy").equals("omniscient")) {
            omniscientAllocationStrategy = fetch(settings, getAllocationStrategies(), "omniscient-allocation");
        }

        batteryClass = fetch(settings, getBatteryClasses(), "battery");
        idleClass = fetch(settings, getIdleClasses(), "idle-strategy");

        // Omniscient planes must run with omniscient operators and viceversa.
        boolean o1 = operatorStrategy instanceof Omniscient;
        boolean o2 = planesClass == OmniscientPlane.class;
        if (o1 != o2) {
            throw new IllegalArgumentException("Omniscient planes must run with omniscient operators.");
        }

        evaluationClass = fetch(settings, getEvaluationClasses(), "task-evaluation");
        gui = fetch(settings, getBooleanValues(), "gui");
        quiet = fetch(settings, getBooleanValues(), "quiet");


        DProblem d = new DProblem();
        ObjectMapper mapper = new ObjectMapper();
        problemFile = settings.getProperty("problem");
        values.put("problem", problemFile);
        try {
            d = mapper.readValue(new File(getProblemFile()), DProblem.class);
            // Register the generator settings used to generate this problem
            for (String key : d.getGeneratorSettings().keySet()) {
                values.put("g-" + key, d.getGeneratorSettings().get(key));
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Error reading problem file \"" + getProblemFile() + "\"");
        }
        problemDefinition = d;

        // Auctions settings
        if (values.get("planes").equals("auction")) {
            aucEvery = Integer.valueOf(settings.getProperty("auction-every"));
            values.put("auction-every", String.valueOf(aucEvery));

            aucBiddingRuleFactory = fetch(settings, getBiddingRuleFactories(), "auction-bidding-rule");
            if (values.get("auction-bidding-rule").equals("workload")) {
                aucWorkloadK = Double.valueOf(settings.getProperty("auction-workload-k"));
                values.put("auction-workload-k", String.valueOf(aucWorkloadK));

                aucWorkloadAlpha = Double.valueOf(settings.getProperty("auction-workload-alpha"));
                values.put("auction-workload-alpha", String.valueOf(aucWorkloadAlpha));
            }
        }

        // Max-sum settings
        if (  values.get("planes").equals("maxsum")
           || (  values.get("planes").equals("omniscient")
              && values.get("omniscient-allocation").equals("maxsum") )
           )
        {
            msIterations = Integer.valueOf(settings.getProperty("maxsum-iterations"));
            values.put("maxsum-iterations", String.valueOf(msIterations));

            if ( values.get("planes").equals("maxsum")) {
                msStartEvery = Integer.valueOf(settings.getProperty("maxsum-start-every"));
                values.put("maxsum-start-every", String.valueOf(msStartEvery));
            }

            msCostFactorFactory = fetch(settings, getCostFactorFactories(), "maxsum-planes-function");
            if (values.get("maxsum-planes-function").equals("workload")) {
                msWorkloadFunctionFactory = fetch(settings, getWorkloadFunctionFactories(), "maxsum-workload-function");

                if (values.get("maxsum-workload-function").equals("k-alpha")) {
                    msWorkloadK = Double.valueOf(settings.getProperty("maxsum-workload-k"));
                    values.put("maxsum-workload-k", String.valueOf(msWorkloadK));
                    msWorkloadAlpha = Double.valueOf(settings.getProperty("maxsum-workload-alpha"));
                    values.put("maxsum-workload-alpha", String.valueOf(msWorkloadAlpha));
                }
            }
        }

        // DSA settings
        if (values.get("planes").equals("dsa")) {
            dsaIterations = Integer.valueOf(settings.getProperty("dsa-iterations"));
            values.put("dsa-iterations", String.valueOf(dsaIterations));

            dsaEvery = Integer.valueOf(settings.getProperty("dsa-every"));
            values.put("dsa-every", String.valueOf(dsaEvery));
            if(dsaEvery <= dsaIterations) {
                throw new IllegalArgumentException("dsa-iterations must be < dsa-every");
            }

            dsaP = Double.valueOf(settings.getProperty("dsa-p"));
            values.put("dsa-p", String.valueOf(dsaP));
            if(dsaP < 0 ||dsaP > 1) {
                throw new IllegalArgumentException("dsa-p must be between 0 and 1.");
            }

            dsaEvaluationFunction = settings.getProperty("dsa-planes-function");
            if(dsaEvaluationFunction.equals("workload")){

                values.put("dsa-planes-function", dsaEvaluationFunction);

                dsaWorkloadK = Double.valueOf(settings.getProperty("dsa-workload-k"));
                values.put("dsa-workload-k", String.valueOf(dsaWorkloadK));

                dsaWorkloadAlpha = Double.valueOf(settings.getProperty("dsa-workload-alpha"));
                values.put("dsa-workload-alpha", String.valueOf(dsaWorkloadAlpha));

            }
            else {
                if(dsaEvaluationFunction.equals("pathcost")) {
                    values.put("dsa-planes-function", dsaEvaluationFunction);
                }
                else {
                    throw new IllegalArgumentException("Two possible type of dsa function used to represent plane's preferences: pathcost or workload.");

                }
            }
        }

        if (values.get("planes").equals("liam")) {
            eaglePower = Double.valueOf(settings.getProperty("eagle-power"));
            values.put("eagle-power", String.valueOf(eaglePower));
            if(eaglePower < 0 ||eaglePower > 1) {
                throw new IllegalArgumentException("Eagle power conversion level must be between 0 and 1.");
            }

            standbyPower = Double.valueOf(settings.getProperty("standby-power"));
            values.put("standby-power", String.valueOf(standbyPower));
            if(standbyPower < 0 ||standbyPower > 1) {
                throw new IllegalArgumentException("Standby power conversion level must be between 0 and 1.");
            }

            scoutSpeed = Double.valueOf(settings.getProperty("scout-speed"));
            values.put("scout-speed", String.valueOf(scoutSpeed));
            if(scoutSpeed < 0 ||scoutSpeed > 1) {
                throw new IllegalArgumentException("Scout speed level must be between 0 and 1.");
            }

            eagleSpeed = Double.valueOf(settings.getProperty("eagle-speed"));
            values.put("eagle-speed", String.valueOf(eagleSpeed));
            if(eagleSpeed < 0 ||eagleSpeed > 1) {
                throw new IllegalArgumentException("Scout speed level must be between 0 and 1.");
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("###### Settings:\n");
        for (String key : values.keySet()) {
            buf.append("# ").append(key).append(" = ").append(values.get(key)).append("\n");
        }

        return buf.toString();
    }

    /**
     * @return the gui
     */
    public boolean isGui() {
        return gui;
    }

    /**
     * @return the quiet
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * @return the problemFile
     */
    public String getProblemFile() {
        return problemFile;
    }

    /**
     * @return the problemDefinition
     */
    public DProblem getProblemDefinition() {
        return problemDefinition;
    }

    /**
     * @return the operatorStrategy
     */
    public OperatorStrategy getOperatorStrategy() {
        return operatorStrategy;
    }

    /**
     * @return the planesClass
     */
    public Class<? extends Plane> getPlanesClass() {
        return planesClass;
    }

    /**
     * @return the omniscientAllocationStrategy
     */
    public Class<? extends AllocationStrategy> getOmniscientAllocationStrategy() {
        return omniscientAllocationStrategy;
    }

    /**
     * @return the batteryClass
     */
    public Class<? extends Battery> getBatteryClass() {
        return batteryClass;
    }

    /**
     * @return the idleClass
     */
    public Class<? extends IdleStrategy> getIdleClass() {
        return idleClass;
    }

    /**
     * @return the evaluationClass
     */
    public Class<? extends EvaluationStrategy<Plane>> getEvaluationClass() {
        return evaluationClass;
    }

    /**
     * @return the aucEvery
     */
    public int getAucEvery() {
        return aucEvery;
    }

    /**
     * @return the aucBiddingRuleFactory
     */
    public BiddingRuleFactory getAucBiddingRuleFactory() { return aucBiddingRuleFactory; }

    /**
     * @return the aucWorkloadK
     */
    public double getAucWorkloadK() {
        return aucWorkloadK;
    }

    /**
     * @return the aucWorkloadAlpha
     */
    public double getAucWorkloadAlpha() {
        return aucWorkloadAlpha;
    }

    /**
     * @return the msIterations
     */
    public int getMsIterations() {
        return msIterations;
    }

    /**
     * @return the msStartEvery
     */
    public int getMsStartEvery() {
        return msStartEvery;
    }

    /**
     * @return the msWorkloadK
     */
    public double getMsWorkloadK() {
        return msWorkloadK;
    }

    /**
     * @return the msWorkloadAlpha
     */
    public double getMsWorkloadAlpha() {
        return msWorkloadAlpha;
    }

    /**
     * @return the CostFactor factory
     */
    public CostFactorFactory<Factor<?>> getMsCostFactorFactory() {
        return msCostFactorFactory;
    }

    /**
     * @return the workload functionf actory.
     */
    public WorkloadFunctionFactory getMsWorkloadFunctionFactory() {
        return msWorkloadFunctionFactory;
    }

    /**
     * @return the number of DSA iterations.
     */
    public int getDsaIterations() {
        return dsaIterations;
    }

    /**
     * @return the number of tenths of seconds between dsa executions.
     */
    public int getDsaEvery() {
        return dsaEvery;
    }

    /**
     * @return the value of the probability used by dsa algorithm.
     */
    public double getDsaP() {
        return dsaP;
    }

    public double getDsaWorkloadK() {
        return dsaWorkloadK;
    }

    public double getDsaWorkloadAlpha() {
        return dsaWorkloadAlpha;
    }

    public String getDsaEvaluationFunction() {
        return dsaEvaluationFunction;
    }


    private Map<String, OperatorStrategy> getOperatorStrategies() {
        return new HashMap<String, OperatorStrategy>() {{
           put("nearest", new Nearest());
           put("random", new Random());
           put("random-inrange", new RandomInRange());
           put("nearest-inrange", new NearestInRange());
           put("omniscient", new Omniscient());
           put("send-all", new SendAll());
        }};
    }

    private Map<String, Class<? extends Plane>> getPlaneClasses() {
        return new HashMap<String, Class<? extends Plane>>() {{
           put("auction", AuctionPlane.class);
           put("none", DefaultPlane.class);
           put("maxsum", MSPlane.class);
           put("omniscient", OmniscientPlane.class);
           put("dsa", DSAPlane.class);
           put("liam", SARPlane.class);
        }};
    }

    private Map<String, Class<? extends AllocationStrategy>> getAllocationStrategies() {
        return new HashMap<String, Class<? extends AllocationStrategy>>() {{
           put("auction", IndependentAuctionAllocation.class);
           put("adhoc", NaiveAdhocAllocation.class);
           put("hungarian", HungarianMethodAllocation.class);
           put("ssi", SSIAllocation.class);
           put("incremental-ssi", IncrementalSSIAllocation.class);
           put("nofirst-ssi", NofirstSSIAllocation.class);
           put("maxsum", MaxSumAllocation.class);
        }};
    }

    private Map<String, Class<? extends Battery>> getBatteryClasses() {
        return new HashMap<String, Class<? extends Battery>>() {{
           put("default", DefaultBattery.class);
           put("infinite", InfiniteBattery.class);
        }};
    }

    private Map<String, Class<? extends IdleStrategy>> getIdleClasses() {
        return new HashMap<String, Class<? extends IdleStrategy>>() {{
           put("do-nothing", DoNothing.class);
           put("fly-towards-operator", FlyTowardsOperator.class);
           put("fly-towards-operator-p", FlyTowardsOperatorP.class);
        }};
    }

    private Map<String, Class<? extends EvaluationStrategy<Plane>>> getEvaluationClasses() {
        return new HashMap<String, Class<? extends EvaluationStrategy<Plane>>>() {{
           put("independent-distance", IndependentDistanceEvaluation.class);
           put("independent-distance-battery", IndependentDistanceBatteryEvaluation.class);
           put("percentage-battery-time", PercentageBatteryEvaluation.class);
        }};
    }

    private Map<String, Boolean> getBooleanValues() {
        return new HashMap<String, Boolean>() {{
           put("true", true);
           put("yes", true);
           put("1", true);
           put("false", false);
           put("no", false);
           put("0", false);
        }};
    }

    private Map<String, BiddingRuleFactory> getBiddingRuleFactories() {
        return new HashMap<String, BiddingRuleFactory>() {{
            put("cost", new CostBiddingRuleFactory());
            put("workload", new WorkloadBiddingRuleFactory());
        }};
    }

    private Map<String, CostFactorFactory<Factor<?>>> getCostFactorFactories() {
        return new HashMap<String, CostFactorFactory<Factor<?>>>() {{
           put("independent", new IndependentFactory<Factor<?>>());
           put("workload", new WorkloadFactory<Factor<?>>());
        }};
    }

    private Map<String, WorkloadFunctionFactory> getWorkloadFunctionFactories() {
        return new HashMap<String, WorkloadFunctionFactory>() {{
           put("k-alpha", new KAlphaFactory());
        }};
    }

}