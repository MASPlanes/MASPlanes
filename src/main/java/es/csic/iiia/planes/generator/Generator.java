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
package es.csic.iiia.planes.generator;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.util.MultivariateUniformDistribution;
import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DStation;
import es.csic.iiia.planes.definition.DTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
//import org.apache.commons.math3.random.EmpiricalDistribution;
//import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Generator of problem instances (scenarios).
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Generator {

    private final Configuration config;

    private final Random r;

    public Generator(Configuration config) {
        this.config = config;
        this.r = new Random(config.getRandom_seed());
    }

    /**
     * Executes the generator.
     */
    public void run() {
        DProblem p = createProblemDefinition();
        p.setGeneratorSettings(config.getGeneratorSettings());
        addPlanes(p);
        addOperators(p);
        addTasks(p);
        addStations(p);

        writeProblem(p);
    }

    private void writeProblem(DProblem p) {
        // Open output file
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(config.getOutputFile(), p);
        } catch (IOException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DProblem createProblemDefinition() {
        DProblem p = new DProblem();

        p.setDuration(config.getDuration());
        p.setnCrisis(config.getNum_crisis());
        p.setWidth(config.getWidth());
        p.setHeight(config.getHeight());
        p.setBlockSize(config.getBlockSize());
        p.setHeightRegions(config.getHeightRegions());
        p.setWidthRegions(config.getWidthRegions());

        return p;
    }

    private void addPlanes(DProblem p) {
        ArrayList<DPlane> planes = p.getPlanes();
        for (int i=0;i<config.getNum_planes();i++) {
            DPlane pl = new DPlane();
            // Speed in meters per tenth of second
            pl.setSpeed(config.getPlaneSpeed());
            pl.setX(r.nextInt(p.getWidth()));
            pl.setY(r.nextInt(p.getHeight()));
            // battery capacity in tenths of second
            pl.setBatteryCapacity(config.getBatteryCapacity());
            // If  wish to set initial batteries to random, use *r.nextDouble()
            pl.setInitialBattery((long)(pl.getBatteryCapacity()));
            pl.setCommunicationRange(config.getCommunicationRange());
            pl.setColor(config.getColor(i));
            planes.add(pl);
        }
    }

    private void addOperators(DProblem p) {
        ArrayList<DOperator> operators = p.getOperators();
        for (int i=0;i<config.getNum_operators();i++) {
            DOperator o = new DOperator();
            o.setX(r.nextInt(p.getWidth()));
            o.setY(r.nextInt(p.getHeight()));
            o.setCommunicationRange(config.getCommunicationRange());
            operators.add(o);
        }
    }

    private void addTasks(DProblem p) {
        ArrayList<DTask> tasks = new ArrayList<DTask>();

        // Create the tasks, randomly located
        for (int i=0;i<config.getNum_tasks();i++) {
            DTask t = new DTask();
            t.setX(r.nextInt(p.getWidth()));
            t.setY(r.nextInt(p.getHeight()));
            tasks.add(t);
            p.getOperators().get(r.nextInt(config.getNum_operators())).getTasks().add(t);
        }

        // Set task times. Use the crisis model for now.

        // How is it done?

        // 1.a Create a "base" uniform distribution between 0 and duration
        RealDistribution[] timeDistributions = new RealDistribution[config.getNum_crisis()];
        timeDistributions[0] = new UniformRealDistribution(0, config.getDuration());
        timeDistributions[0].reseedRandomGenerator(r.nextLong());

        // 1.b Create a "base" uniform distribution for the 2d space
        MultivariateRealDistribution[] spaceDistributions =
                new MultivariateRealDistribution[config.getNum_crisis()];
        spaceDistributions[0] = new MultivariateUniformDistribution(
                new double[]{0, 0}, new double[]{p.getWidth(), p.getHeight()} );
        spaceDistributions[0].reseedRandomGenerator(r.nextLong());

        // 2.a Create one gaussian distribution for each crisis, trying to
        //    spread them out through time.
        for (int i=1; i<config.getNum_crisis(); i++) {
            double mean = r.nextDouble()*config.getDuration();
            double std = (config.getDuration()/(double)config.getNum_crisis())*0.05;
            timeDistributions[i] = new NormalDistribution(mean, std);
            timeDistributions[i].reseedRandomGenerator(r.nextLong());
        }

        // 2.b Create one distribution for each crisis
        for (int i=1; i<config.getNum_crisis(); i++) {
            spaceDistributions[i] = config.getTaskDistributionFactory().buildDistribution(config, r);
        }

        // 3. Uniformly sample tasks from these distributions
        int i = 0;
        for (DTask t : tasks) {
            final int j = (int)(r.nextDouble()*(config.getNum_crisis()));
            t.setnCrisis(j);

            // Time sampling
            /** UNCOMMENT TO MAKE TIMES RANDOMLY DISTRIBUTED
            long time = (long)timeDistributions[i].sample();
            while (time < 0 || time > config.getDuration()) {
                time = (long)timeDistributions[i].sample();
            }
            */
            // Set all tasks to appear at the start of the simulation. To change
            // this, delete the 0 and replace it with the long variable "time"
            t.setTime(0);

            // Divide simulation space into (a x a) sized blocks
            final Location[][] blocks = Location.buildBlocks(config.getBlockSize(), config.getWidthRegions(),
                    config.getHeightRegions());

            // Position sampling
            double[] position = spaceDistributions[j].sample();
            /*
            * Sample a point until its position is not conflicting with
            * any previous point positions (i.e. it is not located in the same block
            * as a previously assigned point), AND it is a valid position
            * that falls inside the simulation space.
            */

            while (invalidPosition(position[0], position[1], p)){
                    //|| blockConflict(blocks, position[0], position[1], tasks, i)) {
                position = spaceDistributions[j].sample();
            }
//            int k = 0;
//            for (DTask t2: tasks) {
//                if(k < i) {
//                    // Check if the position sampled is within the simulation space
//                    while (invalidPosition(position[0], position[1], p)
//                            || sameBlocks(blocks, position[0], position[1], t2)) {
//                        position = spaceDistributions[j].sample();
//                    }
//                }
//                else {
//                    while (invalidPosition(position[0], position[1], p)) {
//                        position = spaceDistributions[j].sample();
//                    }
//                }
//                k++;
//            }
//            while (invalidPosition(position[0], position[1], p)) {
//                position = spaceDistributions[j].sample();
//            }

//            int posX;
//            int posY;
//            if (i < blocks[0].length) {
//                posX = (int)blocks[0][i].getX();
//                posY = (int)blocks[0][i].getY();
//            }
//            else {
//                posX = (int)blocks[1][0].getX();
//                posY = (int)blocks[1][0].getY();
//            }

            t.setX((int)position[0]);
            t.setY((int)position[1]);

//            t.setX(posX);
//            t.setY(posY);
            i++;
        }

        // 4. Debug stuff
        //printTaskHistogram(tasks);
    }

    private boolean invalidPosition(double x, double y, DProblem p) {
        return x < 0 || y < 0 || x > p.getWidth() || y > p.getHeight();
    }

//    private boolean sameBlocks(Location[][] blocks, double x, double y, DTask t) {
//        for (int i = 0; i < blocks.length; i++) {
//            for (int j = 0; j < blocks[0].length; j++) {
//                double locX = blocks[i][j].getX();
//                double locY = blocks[i][j].getY();
//                if((x < locX && t.getX() >= locX) || (y < locY && t.getY() >= locY)
//                        || (x >= locX && t.getX() < locX) || (y >= locY && t.getY() < locY)) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    private boolean blockConflict(Location[][] blocks, double x, double y, ArrayList<DTask> tasks, int ind1) {
        int k = 0;
        boolean conflictExists = true;
        for (DTask t: tasks) {
            if(k < ind1) {
                for (int i = 0; i < blocks.length; i++) {
                    for (int j = 0; j < blocks[0].length; j++) {
                        double locX = blocks[i][j].getX();
                        double locY = blocks[i][j].getY();
                        if((x < locX && t.getX() >= locX) || (y < locY && t.getY() >= locY)
                                || (x >= locX && t.getX() < locX) || (y >= locY && t.getY() < locY)) {
                            conflictExists = false;
                            break;
                        }
                    }
                    if (!conflictExists) {
                        break;
                    }
                }
                if (conflictExists) {
                    return true;
                }
                else {
                    conflictExists = true;
                }
            }
            else {
                return false;
            }
            k++;
        }
        return false;
    }

//    private void printTaskHistogram(ArrayList<DTask> tasks) {
//
//        double[] data = new double[tasks.size()];
//        for (int i=0; i<tasks.size(); i++) {
//            data[i] = tasks.get(i).getTime();
//        }
//
//        EmpiricalDistribution d = new EmpiricalDistribution(30);
//        d.load(data);
//
//        for (SummaryStatistics stats : d.getBinStats()) {
//            StringBuilder buf = new StringBuilder();
//            for (int i=0, len=Math.round(stats.getN()/50f); i<len; i++) {
//                buf.append('#');
//            }
//            System.err.println(buf);
//        }
//    }

    private void addStations(DProblem p) {
        ArrayList<DStation> stations = p.getStations();
        for (int i=0; i<config.getNum_stations(); i++) {
            DStation st = new DStation();
            st.setX(r.nextInt(p.getWidth()));
            st.setY(r.nextInt(p.getHeight()));
            stations.add(st);
        }
    }

}