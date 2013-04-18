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
package es.csic.iiia.planes.maxsum.algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractFactor implements Factor {

    private Map<Factor, Message> newMessages = new HashMap<Factor, Message>();
    private Map<Factor, Message> curMessages = new HashMap<Factor, Message>();
    private List<Factor> neighbors = new ArrayList<Factor>();

    @Override
    public void addNeighbor(Factor factor) {
        neighbors.add(factor);
    }

    @Override
    public List<Factor> getNeighbors() {
        return neighbors;
    }

    public Message getMessage(Factor neighbor) {
        if (curMessages.containsKey(neighbor)) {
            return curMessages.get(neighbor);
        }
        return new Message(neighbor, 0);
    }

    @Override
    public void receive(Message message) {
        newMessages.put(message.sender, message);
    }

    @Override
    public void send(Message message, Factor recipient) {
        recipient.receive(message);
    }

    @Override
    public void tick() {
        Map<Factor, Message> tmp = curMessages;
        curMessages = newMessages;
        newMessages = tmp;
    }

    protected Message buildMessage(double value) {
        return new Message(this, value);
    }

}
