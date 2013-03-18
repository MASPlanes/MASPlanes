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

import apple.awt.CColor;
import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DStation;
import es.csic.iiia.planes.definition.DTask;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Generator of problem instances (scenarios).
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Generator {

    // Duration in tenths of second (10us/s * 60s/m * 60m/h * 24h/d * 30d/month)
    private long duration = 10L * 60L * 60L * 24L * 30L;
    private int width = 10000;
    private int height = 10000;
    private int num_planes = 10;
    private int num_operators = 1;
    // 1 task per minute
    private int num_tasks = 60*24*30;
    private int num_stations = 1;
    private int num_crisis = 4;

    private int[][] colorList = new int[][]{
        new int[]{0, 0, 0}, new int[]{233, 222, 187}, new int[]{173, 35, 35},
        new int[]{255, 238, 51}, new int[]{255, 146, 51}, new int[]{255, 205, 243},
        new int[]{42, 75, 215}, new int[]{29, 105, 20}, new int[]{129, 74, 25},
        new int[]{129, 38, 192}, new int[]{160, 160, 160}, new int[]{129, 197, 122},
        new int[]{157, 175, 255}, new int[]{41, 208, 208}, new int[]{87, 87, 87},
    };

    private Random r = new Random();

    /**
     * Entry point of the execution of this generator.
     *
     * @param args list of command line arguments (ignored)
     */
    public static void main(String[] args) {
        Generator t = new Generator();
        if (args.length >= 1 && args[0].equalsIgnoreCase("short")) {
            t.shorten();
        }
        t.run();
    }

    private void shorten() {
        duration /= 10;
        num_tasks /= 10;
    }

    /**
     * Executes the generator.
     */
    public void run() {
        DProblem p = createProblemDefinition();
        addPlanes(p);
        addOperators(p);
        addTasks(p);
        addStations(p);

        writeProblem(p);
    }

    private void writeProblem(DProblem p) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(System.out, p);
        } catch (IOException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private DProblem createProblemDefinition() {
        DProblem p = new DProblem();

        p.setDuration(duration);
        p.setWidth(width);
        p.setHeight(height);

        return p;
    }

    private void addPlanes(DProblem p) {
        ArrayList<DPlane> planes = p.getPlanes();
        for (int i=0;i<num_planes;i++) {
            DPlane pl = new DPlane();
            // Speed in meters per tenth of second
            pl.setSpeed(50d/36);
            pl.setX(r.nextInt(p.getWidth()));
            pl.setY(r.nextInt(p.getHeight()));
            // battery capacity in tenths of second
            pl.setBatteryCapacity(3600*3*10);
            pl.setInitialBattery((long)(pl.getBatteryCapacity()*r.nextDouble()));
            pl.setCommunicationRange(2000);
            pl.setColor(colorList[i]);
            planes.add(pl);
        }
    }

    private void addOperators(DProblem p) {
        ArrayList<DOperator> operators = p.getOperators();
        for (int i=0;i<num_operators;i++) {
            DOperator o = new DOperator();
            o.setX(r.nextInt(p.getWidth()));
            o.setY(r.nextInt(p.getHeight()));
            o.setCommunicationRange(2000);
            operators.add(o);
        }
    }

    private void addTasks(DProblem p) {
        ArrayList<DTask> tasks = new ArrayList<DTask>();

        // Create the tasks, randomly located
        for (int i=0;i<num_tasks;i++) {
            DTask t = new DTask();
            t.setX(r.nextInt(p.getWidth()));
            t.setY(r.nextInt(p.getHeight()));
            tasks.add(t);
            p.getOperators().get(r.nextInt(num_operators)).getTasks().add(t);
        }

        // Set task times. Use the crisis model for now.

        // How is it done?
        // 1.a Create a "base" uniform distribution between 0 and duration
        RealDistribution[] timeDistributions = new RealDistribution[num_crisis+1];
        timeDistributions[0] = new UniformRealDistribution(0, duration);
        timeDistributions[0].reseedRandomGenerator(r.nextLong());
        // 1.b Create a "base" uniform distribution for the 2d space
        MultivariateRealDistribution[] spaceDistributions =
                new MultivariateRealDistribution[num_crisis+1];
        spaceDistributions[0] = new MultivariateUniformDistribution(
                new double[]{0, 0}, new double[]{p.getWidth(), p.getHeight()} );
        spaceDistributions[0].reseedRandomGenerator(r.nextLong());

        // 2.a Create one gaussian distribution for each crisis, trying to
        //    spread them out through time.
        for (int i=1; i<=num_crisis; i++) {
            double mean = r.nextDouble()*duration;
            double std = (duration/(double)num_crisis)*0.05;
            timeDistributions[i] = new NormalDistribution(mean, std);
            timeDistributions[i].reseedRandomGenerator(r.nextLong());
        }
        // 2.b Create one multivariate gaussian distribution for each crisis
        for (int i=1; i<=num_crisis; i++) {
            double[] means = new double[]{
                r.nextInt(p.getWidth()), r.nextInt(p.getHeight()),
            };
//            double [][] covariances = new double[][]{
//                new double[]{p.getWidth() * r.nextDouble() * 50 + 10, 0},
//                new double[]{0, p.getHeight() * r.nextDouble() * 50 + 10},
//            };
            double[][] covariances = getCovarianceMatrix(p.getWidth(), p.getHeight());
            spaceDistributions[i] = new MultivariateNormalDistribution(
                    means, covariances);
            spaceDistributions[i].reseedRandomGenerator(r.nextLong());
        }

        // 3. Uniformly sample task times from these distributions
        for (DTask t : tasks) {
            final int i = (int)(r.nextDouble()*(num_crisis+1));
            t.setnCrisis(i);

            // Time sampling
            long time = (long)timeDistributions[i].sample();
            while (time < 0 || time > duration) {
                time = (long)timeDistributions[i].sample();
            }
            t.setTime(time);

            // Position sampling
            double[] position = spaceDistributions[i].sample();
            while (  position[0] < 0 || position[1] < 0
                  || position[0] > p.getWidth() || position[1] > p.getHeight())
            {
                position = spaceDistributions[i].sample();
            }
            t.setX((int)position[0]);
            t.setY((int)position[1]);
        }

        // 4. Debug stuff
        printTaskHistogram(tasks);
    }

    private double[][] getCovarianceMatrix(int width, int height) {
        double scale = .01 + .05*r.nextDouble();
        double w = width * scale;
        double h = width * scale;
        RealMatrix m = new Array2DRowRealMatrix(new double[][]{
                new double[]{w + w, r.nextDouble() * h + w},
                new double[]{0, h + h},
        });
        RealMatrix result = m.multiply(m.transpose());
        return result.getData();
    }

    private void printTaskHistogram(ArrayList<DTask> tasks) {

        double[] data = new double[tasks.size()];
        for (int i=0; i<tasks.size(); i++) {
            data[i] = tasks.get(i).getTime();
        }

        EmpiricalDistribution d = new EmpiricalDistribution(30);
        d.load(data);

        for (SummaryStatistics stats : d.getBinStats()) {
            StringBuilder buf = new StringBuilder();
            for (int i=0, len=Math.round(stats.getN()/50f); i<len; i++) {
                buf.append('#');
            }
            System.err.println(buf);
        }
    }

    private void addStations(DProblem p) {
        ArrayList<DStation> stations = p.getStations();
        for (int i=0; i<num_stations; i++) {
            DStation st = new DStation();
            st.setX(r.nextInt(p.getWidth()));
            st.setY(r.nextInt(p.getHeight()));
            stations.add(st);
        }
    }

}