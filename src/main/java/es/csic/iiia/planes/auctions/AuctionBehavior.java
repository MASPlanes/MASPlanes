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
import es.csic.iiia.planes.auctions.bidding.BiddingRule;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import es.csic.iiia.planes.cli.Configuration;
import es.csic.iiia.planes.messaging.Message;
import es.csic.iiia.planes.MessagingAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the auctioning and replying behavior for AuctionPlanes.
 *
 * <strong>TODO:</strong> Study how could we improve the situation for recharging planes.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AuctionBehavior extends AbstractBehavior {

    private static final Logger LOG = Logger.getLogger(AuctionBehavior.class.getName());

    private Map<Task, List<BidMessage>> bids = new TreeMap<Task, List<BidMessage>>();

    private NeighborTracking neighborTracker;

    private BiddingRule biddingRule;

    /**
     * Builds an auctioning behavior for the given agent.
     *
     * @param agent that will display this behavior.
     */
    public AuctionBehavior(AuctionPlane agent) {
        super(agent);
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{NeighborTracking.class};
    }

    @Override
    public void initialize() {
        super.initialize();
        final Configuration config = getAgent().getWorld().getFactory().getConfiguration();

        neighborTracker = getAgent().getBehavior(NeighborTracking.class);
        biddingRule = config.getAucBiddingRuleFactory().build(config);
    }

    @Override
    public AuctionPlane getAgent() {
        return (AuctionPlane)super.getAgent();
    }

    @Override
    public void beforeMessages() {
    }

    /**
     * React to a bid message.
     * <p/>
     * Bids are stored to be processed at the end of this step, so that all
     * bids are accounted for before picking the winner.
     *
     * @see #afterMessages()
     * @param bid being received.
     */
    public void on(BidMessage bid) {
        final Task task = bid.getTask();

        // Ignore this bid if the bidder may be out of communications range
        if (!neighborTracker.isNeighbor(bid.getSender())) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "{0} dropping bid for {1} from {2}: agent out of range.",
                        new Object[]{getAgent(), task.getId(), getSenderID(bid)});
            }
            return;
        }

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "{0} accepted bid for {1} from {2}, offer: {3}",
                    new Object[]{getAgent(), task.getId(), getSenderID(bid), bid.getPrice()});
        }

        List<BidMessage> tbids = bids.get(task);
        if (tbids == null) {
            tbids = new ArrayList<BidMessage>();
            bids.put(task, tbids);
        }
        tbids.add(bid);
    }

    public void on(WinnerMessage win) {
        getAgent().addTask(win.getTask());

        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0} wins task {1}", new Object[]{getAgent(), win.getTask().getId()});
        }
    }

    public void on(AskMessage ask) {
        final AuctionPlane agent = getAgent();
        final BidMessage bid = biddingRule.getBid(agent, ask.getTask());

        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Plane {0} bid for {1}: {2}", new Object[]{agent.getId(), ask.getTask().getId(), bid.getPrice()});
        }

        bid.setRecipient(ask.getSender());
        agent.send(bid);
    }

    @Override
    public void afterMessages() {
        processBids();
        beginAuctions();
    }

    private void processBids() {

        if (!bids.isEmpty()) {
            for (Task t : bids.keySet()) {
                doAuction(t);
            }
            bids.clear();
        }

    }

    private void doAuction(Task t) {
        final AuctionPlane agent = getAgent();

        BidMessage winner = computeWinner(bids.get(t));
        if (winner.getSender() != agent) {

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} loses task {1} to {2} (cost: {3})",
                        new Object[]{agent, t.getId(), getSenderID(winner),
                        winner.getPrice()});
            }

            agent.removeTask(t);
            WinnerMessage win = new WinnerMessage(t);
            win.setRecipient(winner.getSender());
            agent.send(win);
        } else {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} keeps task {1} (cost: {2})",
                        new Object[]{agent, t.getId(), winner.getPrice()});
            }
        }
    }

    private static BidMessage computeWinner(List<BidMessage> bids) {
        BidMessage winner = null;
        double minCost = Double.MAX_VALUE;
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
        if (agent.getWorld().getTime() % getConfiguration().getAucEvery() == 0) {
            for(Task t : agent.getTasks()) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "{0} auctioning task {1}", new Object[]{agent, t.getId()});
                }

                AskMessage ask = new AskMessage(t);
                agent.send(ask);
            }
        }
    }

    private static int getSenderID(AuctionMessage m) {
        MessagingAgent sender = m.getSender();
        if (!(sender instanceof AuctionPlane)) {
            throw new ClassCastException();
        }

        return ((AuctionPlane)sender).getId();
    }

}