/*
 * Copyright (c) 2013, Marc Pujol <mpujol@iiia.csic.es>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.tutorial;

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Behavior that implements the Parallel Single-Item Auctions 
 * coordination mechanism.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class PSIAuctionBehavior extends AbstractBehavior<TutorialPlane> {

    private Map<Task, List<BidMessage>> collectedBids =
            new HashMap<Task, List<BidMessage>>();

    private NeighborTracking neighborTracker;

    /**
     * Build a new Parallel Single Item Auctions behavior.
     * @param agent plane that will display this behavior.
     */
    public PSIAuctionBehavior(TutorialPlane agent) {
        super(agent);
    }

    @Override
    public void initialize() {
        super.initialize();
        neighborTracker = getAgent().getBehavior(NeighborTracking.class);
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{NeighborTracking.class};
    }

    @Override
    public void beforeMessages() {
        collectedBids.clear();
    }

    public void on(OpenAuctionMessage auction) {
        TutorialPlane plane = getAgent();
        Task t = auction.getTask();
        
        double cost = plane.getLocation().distance(t.getLocation());
        BidMessage bid = new BidMessage(t, cost);
        bid.setRecipient(auction.getSender());
        plane.send(bid);
    }
    
    public void on(BidMessage bid) {
        Task t = bid.getTask();

        // Ignore bids from planes that may run out of range
        if (!neighborTracker.isNeighbor(bid.getSender(), 1)) {
            return;
        }

        // Get the list of bids for this task, or create a new list if
        // this is the first bid for this task.
        List<BidMessage> taskBids = collectedBids.get(t);
        if (taskBids == null) {
            taskBids = new ArrayList<BidMessage>();
            collectedBids.put(t, taskBids);
        }

        taskBids.add(bid);
    }

    public void on(ReallocateMessage msg) {
        getAgent().addTask(msg.getTask());
    }

    @Override
    public void afterMessages() {
        // Open new auctions only once every four steps
        if (getAgent().getWorld().getTime() % 4 == 0) {
            openAuctions();
        }

        // Compute auction winners only if we have received bids in this step
        if (!collectedBids.isEmpty()) {
            computeAuctionWinners();
        }
    }

    private void openAuctions() {
        TutorialPlane plane = getAgent();
        for (Task t : plane.getTasks()) {
            OpenAuctionMessage msg = new OpenAuctionMessage(t);
            plane.send(msg);
        }
    }

    private void computeAuctionWinners() {
        for (Task t : collectedBids.keySet()) {
            BidMessage winner = computeAuctionWinner(collectedBids.get(t));
            reallocateTask(winner);
        }
    }

    private BidMessage computeAuctionWinner(List<BidMessage> bids) {
        BidMessage winner = null;
        double minCost = Double.MAX_VALUE;

        for (BidMessage bid : bids) {
            if (bid.getCost() < minCost) {
                winner = bid;
                minCost = bid.getCost();
            }
        }

        return winner;
    }

    private void reallocateTask(BidMessage winner) {
        TutorialPlane plane = getAgent();
        
        // No need to reallocate when the task is already ours
        if (winner.getSender() == plane) {
            return;
        }

        // Remove the task from our list of pending tasks
        plane.removeTask(winner.getTask());
        
        // Send it to the auction's winner
        ReallocateMessage msg = new ReallocateMessage(winner.getTask());
        msg.setRecipient(winner.getSender());
        plane.send(msg);
    }

}
