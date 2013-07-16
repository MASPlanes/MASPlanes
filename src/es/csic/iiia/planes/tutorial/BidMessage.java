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
import es.csic.iiia.planes.messaging.AbstractMessage;

/**
 * Message containing a bid for a task.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BidMessage extends AbstractMessage {
    
    private double cost;
    
    private Task task;
    
    /**
     * Build a new bid message
     * @param cost 
     */
    public BidMessage(Task t, double cost) {
        this.task = t;
        this.cost = cost;
    }
    
    /**
     * Get the cost for the sending plane to perform the specified task.
     * @return cost for {@link #getSender()} to perform {@link #getTask()}.
     */
    public double getCost() {
        return cost;
    }
    
    /**
     * Get the task for which this bid is.
     * @return task for which this bid is.
     */
    public Task getTask() {
        return this.task;
    }
    
}
