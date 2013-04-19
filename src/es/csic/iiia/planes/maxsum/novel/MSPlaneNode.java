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
package es.csic.iiia.planes.maxsum.novel;

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.maxsum.algo.CostFactor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Max-sum plane node.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class MSPlaneNode {
    private static final Logger LOG = Logger.getLogger(MSPlaneNode.class.getName());

    private CostFactor factor;
    private Plane plane;
    private Map<Task, PlaneProxyFactor> proxies = new HashMap<Task, PlaneProxyFactor>();

    /**
     * Build a new plane node for the given plane.
     *
     * @param plane plane that this node represents.
     */
    public MSPlaneNode(Plane plane) {
        factor = plane.getWorld().getFactory().buildCostFactor(plane);
        this.plane = plane;
    }

    /**
     * Add a new task that can be serviced by this plane.
     *
     * @param task task that can be serviced by this plane.
     * @param location plane where this task's node is running (its current owner)
     */
    public void addNeighbor(Task task, Plane location) {
        PlaneProxyFactor proxy = new PlaneProxyFactor(plane, task, location);
        factor.addNeighbor(proxy);
        proxy.addNeighbor(factor);
        proxies.put(task, proxy);
    }

    /**
     * Remove all candidate tasks to service.
     */
    public void clearNeighbors() {
        proxies.clear();
        factor.getNeighbors().clear();
        factor.clearCosts();
    }

    /**
     * Remove the given candidate task.
     * @param task to remove
     */
    public void removeNeighbor(Task task) {
        ProxyFactor<Plane, Task> proxy = proxies.remove(task);
        factor.removeCost(proxy);
        factor.getNeighbors().remove(proxy);
    }

    /**
     * Receive a network message, and deliver it through the corresponding
     * proxy.
     *
     * @param message message to receive
     */
    public void receive(MSMessage<Task, Plane> message) {
        LOG.log(Level.FINER, "{0} receives {1}", new Object[]{this, message});
        PlaneProxyFactor f = proxies.get(message.getLogicalSender());
        if (f == null) {
            throw new RuntimeException("Fatal: can't find plane node.");
        }
        f.receive(message);
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
        // Set the potentials first
        for (Task t : proxies.keySet()) {
            factor.setPotential(proxies.get(t), plane.getCost(t));
        }

        factor.scatter();
    }

    @Override
    public String toString() {
        return "PlaneFactor(" + plane + ")";
    }

}
