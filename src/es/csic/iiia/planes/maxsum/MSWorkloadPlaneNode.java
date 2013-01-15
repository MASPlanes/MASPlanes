/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Expression application is undefined on line 6, column 57 in Templates/Licenses/license-bsd.txt..
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
 *   Neither the name of Expression application is undefined on line 21, column 41 in Templates/Licenses/license-bsd.txt.
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   Expression application is undefined on line 25, column 21 in Templates/Licenses/license-bsd.txt.
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
package es.csic.iiia.planes.maxsum;

import es.csic.iiia.planes.Task;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omg.CORBA.TRANSACTION_MODE;

/**
 * Max-sum plane node that introduces the cost for each task as specified
 * by the plane's {@link MSPlane#getCost(es.csic.iiia.planes.Task)}, plus an
 * additional cost based on the workload (number of tasks) of the plane.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSWorkloadPlaneNode extends MSPlaneNode {
    private static final Logger LOG = Logger.getLogger(MSWorkloadPlaneNode.class.getName());

    private double mean = 0;

    public MSWorkloadPlaneNode(MSPlane plane) {
        super(plane);
    }

    @Override
    public void iter() {

        final Set<Task> domain = getDomain();
        final int size = domain.size();

        int i=0; Triplet[] vals_and_indices = new Triplet[size];
        double sum=0;
        for (Task t : domain) {
            vals_and_indices[i] = new Triplet(t, getMessageValue(t), i);
            sum += vals_and_indices[i].cost;
            i++;
        }
        mean = sum/size;
//        System.out.println(Arrays.deepToString(vals_and_indices));

        // Sort them in ascending cost order
        Arrays.sort(vals_and_indices, new Sorter());
//        System.out.println(Arrays.deepToString(vals_and_indices));
        int[] ridx = new int[size];
        for (i=0; i<size; i++) {
            ridx[vals_and_indices[i].idx] = i;
        }

        // Cumulative sums
        double[] cum_ws    = new double[size+1];
        double[] cum_w_s_1 = new double[size+1];
        double[] cum_w_s0  = new double[size+1];
        double[] cum_w_s1  = new double[size+1];
        for (i=0; i<=size; i++) {

            if (i==0) {
                cum_ws[i] = 0;
            } else {
                cum_ws[i] = vals_and_indices[i-1].cost + cum_ws[i-1];
            }

            cum_w_s0[i] = cum_ws[i] + getAdditionalCost(i);

            if (i>0) {
                cum_w_s_1[i] = cum_ws[i] + getAdditionalCost(i-1);
            } else {
                cum_w_s_1[i] = Double.POSITIVE_INFINITY;
            }

            if (i<size) {
                cum_w_s1[i] = cum_ws[i] + getAdditionalCost(i+1);
            } else {
                cum_w_s1[i] = Double.POSITIVE_INFINITY;
            }
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Sums:");
            LOG.finest(Arrays.toString(cum_ws));
            LOG.finest(Arrays.toString(cum_w_s_1));
            LOG.finest(Arrays.toString(cum_w_s0));
            LOG.finest(Arrays.toString(cum_w_s1));
        }

        // Cumulative maxes
        double[] m_1 = new double[size+1];
        double[] m0R = new double[size+1];
        double[] m0L = new double[size+1];
        double[] m1  = new double[size+1];
        for (i=0; i<=size; i++) {

            if (i==0) {
                m1[i]       = cum_w_s1[i];
                m0L[i]      = cum_w_s0[i];
                m0R[size-i] = cum_w_s0[size-i];
                m_1[size-i] = cum_w_s_1[size-i];
            } else {
                m1[i]       = Math.min(cum_w_s1[i], m1[i-1]);
                m0L[i]      = Math.min(cum_w_s0[i], m0L[i-1]);
                m0R[size-i] = Math.min(cum_w_s0[size-i], m0R[size-i+1]);
                m_1[size-i] = Math.min(cum_w_s_1[size-i], m_1[size-i+1]);
            }

        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Maxes:");
            LOG.finest(Arrays.toString(m_1));
            LOG.finest(Arrays.toString(m0R));
            LOG.finest(Arrays.toString(m0L));
            LOG.finest(Arrays.toString(m1));
        }

        int pos;
        double msg0;
        double msg1;
        for (i=0; i<size; i++) {

            pos  = ridx[i];
            msg0 = Double.POSITIVE_INFINITY;
            msg1 = Double.POSITIVE_INFINITY;

            if (pos > 0) {
                msg0 = Math.min(msg0, m0L[pos-1]);
                msg1 = Math.min(msg1, m1[pos-1]);
            }
            if (pos<size) {
                msg0 = Math.min(msg0, m_1[pos+1] - vals_and_indices[pos].cost);
                msg1 = Math.min(msg1, m0R[pos+1] - vals_and_indices[pos].cost);
            }

            final Task t = vals_and_indices[pos].task;
            double value = msg1 - msg0 + getPlane().getCost(t);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "({0},{1})", new Object[]{msg0, msg1});
            }
            MSPlane2Task msg = new MSPlane2Task(getPlane(), t, value);
            send(msg, t);
        }

    }

    private double getAdditionalCost(double i) {
        return getPlane().getWorld().getFactory().getConfiguration().msWorkloadK
                * Math.pow(i, 2);
    }

    private Double getMessageValue(Task task) {
        MSTask2Plane msg = getMessage(task);
        return (msg != null ? msg.getValue() : 0) + getPlane().getCost(task);
    }

    private class Sorter implements Comparator<Triplet> {

        @Override
        public int compare(Triplet t, Triplet t1) {
            return t.cost.compareTo(t1.cost);
        }

    }

    private class Triplet {
        public final Task task;
        public final Double cost;
        public final int idx;
        public Triplet(Task task, Double cost, int idx) {
            this.task = task;
            this.cost = cost;
            this.idx = idx;
        }
        @Override
        public String toString() {
            return "[" + idx + "]" + task.getId() + ":" + cost;
        }
    }

}