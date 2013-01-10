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
package es.csic.iiia.planes.maxsum;

import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import java.util.Collection;
import java.util.Map;

/**
 * Behavior that implements the actual max-sum algorithm.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSExecutionBehavior extends AbstractBehavior {

    public MSExecutionBehavior(MSPlane plane) {
        super(plane);
    }

    @Override
    public MSPlane getAgent() {
        return (MSPlane)super.getAgent();
    }

    @Override
    public boolean isPromiscuous() {
        return false;
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{MSUpdateGraphBehavior.class};
    }

    @Override
    public void beforeMessages() {

    }

    public void on(MSPlaneNode.MSPlane2Task msg) {
        MSOldTaskNode recipient = getAgent().getFunction(msg.getTask());
        if (recipient != null) {
            recipient.receive(msg);
        }
    }

    public void on(MSOldTaskNode.MSTask2Plane msg) {
        getAgent().getVariable().receive(msg);
    }

    @Override
    public void afterMessages() {
        final long remainder = getAgent().getWorld().getTime() % getConfiguration().msStartEvery;
        if (getAgent().isInactive() || remainder < 1 || remainder > getConfiguration().msIterations) {
            return;
        }

        final MSPlaneNode variable = getAgent().getVariable();
        final Map<Task, MSOldTaskNode> functions = getAgent().getFunctions();

        // Everyone gather
        variable.gather();
        for (MSOldTaskNode f : functions.values()) {
            f.gather();
        }

        // Everyone scatter
        variable.scatter();
        for (MSOldTaskNode f : functions.values()) {
            f.scatter();
        }
    }

}
