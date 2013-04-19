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
package es.csic.iiia.planes.maxsum.distributed;

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.maxsum.centralized.Factor;
import es.csic.iiia.planes.maxsum.centralized.SelectorFactor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Max-sum task node to run inside a plane.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class MSTaskNode {
    private static final Logger LOG = Logger.getLogger(MSTaskNode.class.getName());

    private SelectorFactor factor;
    private Task task;
    private Plane plane;
    private Map<Plane, TaskProxyFactor> proxies = new HashMap<Plane, TaskProxyFactor>();

    /**
     * Build a new node for the given task, running inside the specified plane.
     *
     * @param plane plane where this node runs
     * @param task task that this node represents
     */
    public MSTaskNode(Plane plane, Task task) {
        factor = new SelectorFactor();
        this.plane = plane;
        this.task = task;
    }

    /**
     * Add a new candidate plane to service this node's task.
     *
     * @param remote candidate plane
     */
    public void addNeighbor(Plane remote) {
        TaskProxyFactor proxy = new TaskProxyFactor(task, remote, plane, remote);
        proxy.addNeighbor(factor);
        factor.addNeighbor(proxy);
        proxies.put(remote, proxy);
    }

    /**
     * Remove all candidate planes.
     */
    public void clearNeighbors() {
        proxies.clear();
        factor.getNeighbors().clear();
    }

    /**
     * Receive a network message.
     *
     * @param message message to receive
     */
    public void receive(MSMessage<Plane, Task> message) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0} receives {1}", new Object[]{this, message});
        }

        final Plane p = message.getLogicalSender();
        if (p == null || !proxies.containsKey(p)) {
            LOG.severe("A task node just received a message from an unknown neighbor.");
            throw new RuntimeException("Unreachable code.");
        }
        proxies.get(p).receive(message);
    }

    /**
     * Run the gather phase of this node.
     */
    public void gather() {
        factor.tick();
        factor.gather();
    }

    /**
     * Run the scatter phase of this node.
     */
    public void scatter() {
        factor.scatter();
    }

    /**
     * Pick the most suitable plane to become the new owner of this node's
     * task.
     *
     * @return most suitable plane to service this task (at the current time)
     */
    public Plane makeDecision() {
        Factor choice = factor.select();

        // This task has not been negotiated yet
        if (choice == null) {
            return plane;
        }

        if (choice instanceof ProxyFactor) {
            ProxyFactor<Task, Plane> f = (ProxyFactor<Task, Plane>)choice;
            return f.getTo();
        }

        throw new RuntimeException("Unreachable code.");
    }

    @Override
    public String toString() {
        return "TaskFactor(" + task.getId() + ")";
    }

}
