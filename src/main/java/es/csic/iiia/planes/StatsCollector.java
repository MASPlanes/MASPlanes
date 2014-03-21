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

import es.csic.iiia.planes.util.TimeTracker;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class StatsCollector {

    private AbstractWorld world;
    private DescriptiveStatistics taskStats = new DescriptiveStatistics();
    private DescriptiveStatistics planeStats = new DescriptiveStatistics();

    public StatsCollector(AbstractWorld w) {
        world = w;
    }

    public void collect(Task t) {
        final long time = world.getTime() - t.getSubmissionTime();
        taskStats.addValue(time);
    }

    public void collect(Plane p) {
        planeStats.addValue(p.getTotalDistance());
    }

    public void display() {
        // Final stats
        StringBuilder buf = new StringBuilder();
        buf.append("task_min=").append((int)taskStats.getMin()).append("\n")
           .append("task_mean=").append((int)taskStats.getMean()).append("\n")
           .append("task_max=").append((int)taskStats.getMax()).append("\n")
           .append("task_p25=").append((int)taskStats.getPercentile(25)).append("\n")
           .append("task_median=").append((int)taskStats.getPercentile(50)).append("\n")
           .append("task_p75=").append((int)taskStats.getPercentile(75)).append("\n")
        .append("\n");

        buf.append("plane_min=").append((long)(planeStats.getMin()/1000)).append("\n")
           .append("plane_mean=").append((long)(planeStats.getMean()/1000)).append("\n")
           .append("plane_max=").append((long)(planeStats.getMax()/1000)).append("\n")
           .append("plane_p25=").append((long)(planeStats.getPercentile(25)/1000)).append("\n")
           .append("plane_median=").append((long)(planeStats.getPercentile(50)/1000)).append("\n")
           .append("plane_p75=").append((long)(planeStats.getPercentile(75)/1000)).append("\n")
           .append("\n");

        buf.append("time=").append(TimeTracker.getUserTime()/1e6d);

        System.out.println(buf);
    }

}