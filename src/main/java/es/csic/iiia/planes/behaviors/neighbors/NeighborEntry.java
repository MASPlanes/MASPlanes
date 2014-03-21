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
package es.csic.iiia.planes.behaviors.neighbors;

import es.csic.iiia.planes.MessagingAgent;

/**
 * Represents an entry in the NeighborList
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class NeighborEntry implements Comparable<NeighborEntry> {

    public final int iters;
    public final MessagingAgent agent;

    protected NeighborEntry(MessagingAgent agent, int iters) {
        this.agent = agent;
        this.iters = iters;
    }

    @Override
    public int compareTo(NeighborEntry o) {
        int r = o.iters - iters;
        if (r == 0 && agent != null && o.agent != null) {
            r = agent.hashCode() - o.agent.hashCode();
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof NeighborEntry)) return false;

        NeighborEntry other = (NeighborEntry)o;
        return agent.equals(other.agent) && iters == other.iters;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.agent != null ? this.agent.hashCode() : 0);
        hash = 67 * hash + iters;
        return hash;
    }

    @Override
    public String toString() {
        return "(" + agent + ":" + iters + ")";
    }

}