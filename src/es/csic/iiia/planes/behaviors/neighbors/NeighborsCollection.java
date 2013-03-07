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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Collection that holds the list of neighbors, including for how many
 * iterations are they guaranteed to still be neighbors.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class NeighborsCollection implements Collection<NeighborEntry>
{
    private Map<MessagingAgent, Integer> map = new HashMap<MessagingAgent, Integer>();
    private TreeSet<NeighborEntry> set = new TreeSet<NeighborEntry>();

    /**
     * Adds a new neighbor to the collection.
     *
     * @param a agent that has been detected.
     * @param iters number of iterations during which this neighbor is
     *              guaranteed to still be a neighbor.
     * @return True if the neighbors collection has been updated, or False otherwise.
     */
    public boolean add(MessagingAgent a, Integer iters) {

        if (map.containsKey(a)) {
            int old_iters = map.get(a);
            if (old_iters == iters) {
                // Re-adding an entry that is already inserted
                return false;
            }

            // We need to update the iters value
            if (!remove(new NeighborEntry(a, old_iters))) {
                throw new IllegalStateException("Corruption detected in this neihgbor's list");
            }
        }

        NeighborEntry entry = new NeighborEntry(a, iters);
        map.put(a, iters);
        set.add(entry);
        return true;
    }

    /**
     * Get the list of agents that are guaranteed to remain neighbors for at
     * least <em>iterations</em> iterations.
     *
     * @param iterations required number of iterations.
     * @return {@link Iterable} of agents that are guaranteed to remain neighbors.
     */
    public Iterable<MessagingAgent> get(final int iterations) {
        return new Iterable<MessagingAgent>() {

            @Override
            public Iterator<MessagingAgent> iterator() {
                return new NeighborIterator(iterations);
            }

        };
    }

    /**
     * Check if there are any neighbors for at least the given number of
     * iterations.
     *
     * @param iterations
     * @return
     */
    public boolean hasNeighbors(final int iterations) {
        return set.lower(new NeighborEntry(null, iterations-1)) != null;
    }

    /**
     * Check if an agent is guaranteed to be a neighbor for the given number
     * of iterations.
     *
     * @param a agent to check as neighbor.
     * @param iters number of iterations during which the agent must remain a
     *              neighbor.
     * @return True if the agent is guaranteed to be a neighbor for at least
     *         <em>iters</em> iterations, or False otherwise.
     */
    public boolean contains(MessagingAgent a, int iters) {
        if (!map.containsKey(a)) {
            return false;
        }

        final int i = map.get(a);
        return iters <= i;
    }

    @Override
    public void clear() {
        set.clear();
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<NeighborEntry> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return set.toArray(ts);
    }

    @Override
    public boolean add(NeighborEntry e) {
        return add(e.agent, e.iters);
    }

    /**
     * Removes the given entry from the collection.
     *
     * @param e entry to remove
     * @return True if the entry has been removed, or False otherwise.
     */
    public boolean remove(NeighborEntry e) {
        boolean result = set.remove(e);
        if (result) {
            result = map.remove(e.agent) != null;
        }
        return result;
    }

    /**
     * Removes the given neighbor from the collection.
     *
     * @param a neighbor agent to remove.
     * @return True if the neighbor has been removed, or False otherwise.
     */
    public boolean remove(MessagingAgent a) {
        Integer niters = map.remove(a);
        if (niters == null) {
            return false;
        }

        return set.remove(new NeighborEntry(a, niters));
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof NeighborEntry) {
            return remove((NeighborEntry)o);
        }
        if (o instanceof MessagingAgent) {
            return remove((MessagingAgent)o);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return set.containsAll(clctn);
    }

    @Override
    public boolean addAll(Collection<? extends NeighborEntry> clctn) {
        boolean changed = false;

        for (NeighborEntry e : clctn) {
            changed = add(e) || changed;
        }

        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        boolean changed = false;

        for (Object e : clctn) {
            changed = remove(e) || changed;
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private class NeighborIterator implements Iterator<MessagingAgent> {

        private final int iterations;
        private final Iterator<NeighborEntry> iterator;
        private MessagingAgent next = null;

        public NeighborIterator(int iterations) {
            iterator = NeighborsCollection.this.iterator();
            this.iterations = iterations;
            advance();
        }

        private void advance() {
            if (iterator.hasNext()) {
                NeighborEntry e = iterator.next();
                if (e.iters >= iterations) {
                    next = e.agent;
                    return;
                }
            }
            next = null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public MessagingAgent next() {
            MessagingAgent result = next;
            advance();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}