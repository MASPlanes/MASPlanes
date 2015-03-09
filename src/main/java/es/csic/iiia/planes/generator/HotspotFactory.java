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

import es.csic.iiia.planes.util.InverseWishartDistribution;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class HotspotFactory implements TaskDistributionFactory {

    private InverseWishartDistribution covDistribution = null;

    BicubicSplineInterpolatingFunction interpolator = null;

    public HotspotFactory() {
        BicubicSplineInterpolator inter = new BicubicSplineInterpolator();
        double[] radiuses = new double[]{
            100, 250, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000,
            5000, 7000, 10000, 25000, 50000, 100000
        };
        double[] fdgs = new double[]{
            2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 32.0, 64.0, 128.0
        };
        // Experimentally computed.
        double[][] values = new double[][]{
            {405.47, 2249.26, 2929.02, 3245.37, 3460.22, 3593.05, 3692.03, 3815.16, 4033.87,
                4148.16, 4210.06},
            {2968.52, 14222.81, 18313.46, 20326.07, 21806.51, 22662.14, 23208.42, 23715.36,
                25399.66, 26366.71, 26669.29},
            {11213.85, 55990.07, 73734.04, 82140.39, 87385.68, 88682.35, 92760.70, 95482.55,
                102553.38, 105103.14, 106806.93},
            {49010.42, 228663.46, 295423.52, 326927.76, 352059.91, 360898.96, 374135.74, 384004.46,
                402880.87, 422733.12, 426979.72},
            {115141.73, 515877.92, 656511.88, 731540.31, 785563.46, 817558.16, 834833.64, 858172.82,
                915961.39, 950127.27, 958091.43},
            {204488.77, 921193.48, 1194094.21, 1305641.74, 1393569.24, 1446751.68, 1494515.20,
                1521839.27, 1623172.70, 1686522.80, 1706109.61},
            {315962.07, 1427293.32, 1838333.85, 2064526.84, 2194314.20, 2282070.43, 2347560.39,
                2380895.84, 2566872.52, 2633651.58, 2664563.70},
            {427402.56, 2057424.10, 2649570.01, 2979870.78, 3138320.77, 3284114.26, 3347837.38,
                3415020.60, 3666426.01, 3759020.21, 3831823.12},
            {588781.11, 2824186.51, 3679129.07, 4033130.44, 4296367.15, 4487871.28, 4499515.48,
                4656533.71, 4974616.82, 5119953.21, 5228491.07},
            {764263.69, 3552337.37, 4658250.05, 5201606.45, 5601340.75, 5765557.99, 6004937.42,
                6099531.86, 6506965.21, 6716150.71, 6813582.92},
            {1167242.51, 5832821.77, 7370311.81, 8212437.61, 8763425.19, 9025475.86, 9310137.75,
                9504330.19, 1.015490172E7, 1.051851015E7, 1.068713522E7},
            {2157176.614293723, 1.1242706314638646E7, 1.4610912287986722E7, 1.6000820081017397E7,
                1.7124023186361756E7, 1.7893695648083758E7, 1.8246629972280245E7, 1.86383199195E7,
                2.0084456422591142E7, 2.0566576138001565E7, 2.0896906104497E7},
            {4353500.393840918, 2.2988463081825737E7, 2.90549513038103E7, 3.280577237113928E7,
                3.505216806709763E7, 3.604196664145296E7, 3.723929675445001E7, 3.805951964896238E7,
                4.0605647635320775E7, 4.19957866558411E7, 4.277469428119505E7},
            {3.0975287951548893E7, 1.4297738646852845E8, 1.8458068323599958E8, 2.0489308970397E8,
                2.1621100173077223E8, 2.272566465399154E8, 2.3372887770569384E8, 2.39182577766E8,
                2.5454589512049797E8, 2.627388914202363E8, 2.6594602068012756E8},
            {1.3397558666247673E8, 5.84929730287084E8, 7.364682859358164E8, 8.296083638249468E8,
                8.772037655540453E8, 9.173318681253092E8, 9.387704256188443E8, 9.455243965367795E8,
                1.0141183227504988E9, 1.0488870113744953E9, 1.0693285060195222E9},
            {5.929071321009706E8, 2.271864712273714E9, 2.976139403928371E9, 3.3049771712232704E9,
                3.5117145404836836E9, 3.648988582554893E9, 3.721228014208862E9, 3.7931515695400E9,
                4.0630762547608566E9, 4.1839517129751816E9, 4.2590029659393644E9},
        };
        interpolator = inter.interpolate(radiuses, fdgs, values);
    }

    @Override
    public MultivariateNormalDistribution buildDistribution(Configuration config, Random r) {
        final double w = config.getWidth();
        final double h = config.getHeight();
        double maxd = interpolator.value(config.getHotspotRadius(),
                config.getHotspotFreedomDegrees());
        double factor = 1/maxd;

        if (covDistribution == null) {
            RealMatrix m = new Array2DRowRealMatrix(new double[][]{
                {factor, 0},
                {0, factor}
            });
            covDistribution = new InverseWishartDistribution(m, config.getHotspotFreedomDegrees());
            covDistribution.reseedRandomGenerator(r.nextLong());
        }

        double[] means = new double[]{
            r.nextInt(config.getWidth()), r.nextInt(config.getHeight()),
        };
        double[][] covariance = getCovarianceMatrix();
        MultivariateNormalDistribution distribution = new MultivariateNormalDistribution(
                means, covariance);
        distribution.reseedRandomGenerator(r.nextLong());
        return distribution;
    }

    private double[][] getCovarianceMatrix() {
        RealMatrix cov = covDistribution.sample();
        double[][] data = cov.getData();
        return data;
    }

}
