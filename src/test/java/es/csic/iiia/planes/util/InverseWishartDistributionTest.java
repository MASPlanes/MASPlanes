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
package es.csic.iiia.planes.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class InverseWishartDistributionTest {

    private static int WISHART_SAMPLES = 1000;
    private static int NORMAL_SAMPLES = 1000;

    /**
     * Test that generates values for the interpolator.
     */
    @Test
    @Ignore
    public void testSample() {
        double[] radiuses = new double[]{
            100, 250, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000,
            5000, 7000, 10000, 25000, 50000, 100000
        };
        double[] dfs = new double[]{
             2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0, 16.0, 32.0, 64.0, 128.0
        };

        System.out.print("r\\df");
        for (int j=0; j<dfs.length; j++) {
            System.out.print("\t" + dfs[j]);
        }
        System.out.println();

        for (int i=0; i<radiuses.length; i++) {
            double radius = radiuses[i];
            System.out.print(radius);
            for (int j=0; j<dfs.length; j++) {
                System.out.print("\t" + testParameters(radius, dfs[j]));
                System.out.flush();
            }
            System.out.println();
        }

        // Ugly hack so that netbeans displays full output
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(InverseWishartDistributionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double testParameters(double expRadius, double df) {
        double scale_last  = 1;
        double scale_cur   = 10;
        double scale_diff;
        double radius_last = testScale(scale_last, df);
        double alpha = 1;

        do {
            double radius_cur  = testScale(scale_cur, df);
            scale_diff  = scale_cur - scale_last;
            double radius_diff = radius_cur - radius_last;
            double radius_rem  = expRadius - radius_cur;
            scale_last = scale_cur;
            //System.err.println("radius_rem: " + radius_rem + ", scale_diff: " + scale_diff + ", radius_diff: " + radius_diff + ", alpha: " + alpha);
            scale_cur  += (radius_rem * scale_diff / radius_diff) * alpha;
            radius_last = radius_cur;
            alpha = alpha*0.99;
            //System.err.println("Scale: " + scale_cur + ", radius: " + radius_cur + ", dif: " + scale_diff + ", alpha: " + alpha);
        } while (Math.abs(expRadius - radius_last) > expRadius*0.001 && Math.abs(scale_diff) > 1);

        return scale_last;
    }

    private double testGaussian(double scale) {
        double[] means = new double[]{0,0};
        double[][] covariances = new double[][]{
            {scale, 0}, {0, scale}
        };
        MultivariateNormalDistribution uniform = new MultivariateNormalDistribution(means, covariances);
        DescriptiveStatistics stats = new DescriptiveStatistics(NORMAL_SAMPLES);
        for (int i=0; i<NORMAL_SAMPLES; i++) {
            double[] xy = uniform.sample();
            final double x = xy[0];
            final double y = xy[1];
            stats.addValue(Math.sqrt(x*x + y*y));
        }

        return stats.getPercentile(90);
    }

    private double testScale(double scale, double df) {
        RealMatrix scaleMatrix = new Array2DRowRealMatrix(new double[][]{
            {1/scale, 0},
            {0, 1/scale},
        });

        DescriptiveStatistics stats = new DescriptiveStatistics(WISHART_SAMPLES*NORMAL_SAMPLES);
        for (int i = 0; i<WISHART_SAMPLES; i++) {
            InverseWishartDistribution instance = new InverseWishartDistribution(scaleMatrix, df);
            stats.addValue(computeRadius(instance));
        }

        return stats.getPercentile(50);
    }

    private double computeRadius(InverseWishartDistribution instance) {
        double[] means = new double[]{0,0};
        MultivariateNormalDistribution uniform = null;

        while (uniform == null) {
            try {
                double[][] covariances = instance.sample().getData();
                uniform = new MultivariateNormalDistribution(means, covariances);
            } catch(MathUnsupportedOperationException ex) {}
        }

        DescriptiveStatistics stats = new DescriptiveStatistics(NORMAL_SAMPLES);
        for (int i=0; i<NORMAL_SAMPLES; i++) {
            double[] xy = uniform.sample();
            final double x = xy[0];
            final double y = xy[1];
            stats.addValue(Math.sqrt(x*x + y*y));
        }

        return stats.getPercentile(90);
    }

}
