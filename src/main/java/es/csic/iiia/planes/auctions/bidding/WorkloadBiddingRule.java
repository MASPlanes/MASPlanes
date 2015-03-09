/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2015 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.planes.auctions.bidding;

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.auctions.AuctionPlane;
import es.csic.iiia.planes.auctions.BidMessage;

import java.util.List;

/**
 * Bids based on task cost and plane workload.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class WorkloadBiddingRule implements BiddingRule {

    private final double k;
    private final double alpha;

    public WorkloadBiddingRule(double k, double alpha) {
        this.k = k;
        this.alpha = alpha;
    }

    public BidMessage getBid(AuctionPlane plane, Task task) {

        // The task cost is always accounted for
        double cost = plane.getCost(task);

        // Load is the number of tasks the plane has, except for the task being evaluated
        final List<Task> currentTasks = plane.getTasks();
        int load = currentTasks.size();
        if (currentTasks.contains(task)) {
            load--;
        }

        // And now the final cost is tweaked by the cost of the increment in load
        cost += getWorkloadCost(load+1) - getWorkloadCost(load);

        return new BidMessage(task, cost);
    }

    /**
     * Get the workload cost of handling <em>n</em> tasks.
     *
     * @param n number of tasks to handle
     * @return cost associated to handling <em>n</em> tasks
     */
    private double getWorkloadCost(int n) {
        return k * Math.pow(n, alpha);
    }

}
