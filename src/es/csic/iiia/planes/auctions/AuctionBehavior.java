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
package es.csic.iiia.planes.auctions;

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.messaging.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the auctioning and replying behavior for AuctionPlanes.
 * 
 * @TODO Implement finer neighbor tracking, so that no tasks are assigned to
 *       planes that don't receive the Winner bid.
 * @TODO Study how could we improve the situation for recharging planes.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AuctionBehavior extends AbstractBehavior {
    
    private HashMap<Task, List<BidMessage>> bids =
            new HashMap<Task, List<BidMessage>>();

    /**
     * Builds an auctioning behavior for the given agent.
     * 
     * @param agent that will display this behavior.
     */
    public AuctionBehavior(AuctionPlane agent) {
        super(agent);
    }
    
    @Override
    public AuctionPlane getAgent() {
        return (AuctionPlane)super.getAgent();
    }

    @Override
    public void beforeMessages() {
        bids.clear();
    }
    
    public void on(BidMessage bid) {
        if (bid.getRecipient() != getAgent()) {
            return;
        }
        
        // Store the bid to process it at the end of this step
        final Task task = bid.getTask();
        List<BidMessage> tbids = bids.get(task);
        if (tbids == null) {
            tbids = new ArrayList<BidMessage>();
            bids.put(task, tbids);
        }
        tbids.add(bid);
    }
    
    public void on(WinnerMessage win) {
        if (win.getRecipient() != getAgent()) return;
        
//        System.err.println("Agent " + getAgent().getId() + " wins task " + win.getTask().getId());
        getAgent().addTask(win.getTask());
        
        // Send ack
//        System.err.println("Agent " + getAgent().getId() + " wins task " + win.getTask().getId());
        AckWinnerMessage ack = new AckWinnerMessage(win.getTask());
        ack.setRecipient(win.getSender());
        getAgent().send(ack);
    }
    
    public class AckWinnerMessage extends AuctionMessage {

        public AckWinnerMessage(Task task) {
            super(task);
        }
        
    }
    
    public void on(AckWinnerMessage ack) {
        if (ack.getRecipient() == getAgent());
//        System.err.println("Agent " + getAgent().getId() + " releasing task " + ack.getTask().getId());
        getAgent().removeTask(ack.getTask());
    }
    
    public void on(AskMessage ask) {
        final AuctionPlane agent = getAgent();
        
        //double offer = agent.getTaskCost(ask.getTask());
        double offer = agent.getLocation().distance(ask.getTask().getLocation());
//        System.err.println("Plane " + agent.getId() + " bid for " + ask.getTask().getId() + ": " + offer);
        Message bid = new BidMessage(ask.getTask(), offer);
        bid.setRecipient(ask.getSender());
        agent.send(bid);
    }
    
    @Override
    public void afterMessages() {
        processBids();
        beginAuctions();
    }
    
    private void processBids() {
        final AuctionPlane agent = getAgent();
        
        for (Task t : bids.keySet()) {
            double baseCost = agent.getTaskCost(t);
//            System.err.println("Agent " + agent.getId() + ", task: " + t.getId() + 
//                    ", base: " + baseCost + ", offers: " + bids.get(t));
            BidMessage winner = computeWinner(baseCost, bids.get(t));
            if (winner != null) {
//                System.err.println("Agent " + agent.getId() + " wins task " + t.getId());
                //agent.removeTask(t);
                WinnerMessage win = new WinnerMessage(t);
                win.setRecipient(winner.getSender());
                agent.send(win);
            }
        }
    }
    
    private static BidMessage computeWinner(double baseCost, List<BidMessage> bids) {
        BidMessage winner = null;
        double minCost = baseCost;
        for (BidMessage bid : bids) {
            if (bid.getPrice() < minCost) {
                minCost = bid.getPrice();
                winner = bid;
            }
        }
        return winner;
    }

    private void beginAuctions() {
        final Plane agent = getAgent();
        
        // Auction our tasks every minute
        if (agent.getWorld().getTime() % 1000 == 1) {
            for(Task t : agent.getTasks()) {
                AskMessage ask = new AskMessage(t);
                agent.send(ask);
            }
        }
    }
    
}
