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
package es.csic.iiia.planes.maxsum.novel;

import es.csic.iiia.planes.Factory;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.maxsum.algo.CostFactor;
import es.csic.iiia.planes.maxsum.algo.IndependentFactor;
import es.csic.iiia.planes.messaging.Message;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSPlaneNodeTest {

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    /**
     * Test of receive method, of class MSPlaneNode.
     */
    @Test
    public void testReceive() {
        final World world = context.mock(World.class);
        final Factory factory = context.mock(Factory.class);
        final CostFactor factor = new IndependentFactor();

        final Plane plane = context.mock(Plane.class, "Plane 1");
        final Task task1 = new Task(null);
        final Task task2 = new Task(null);
        final Task task3 = new Task(null);

        MSMessage<Task, Plane> message1 = new MSTask2Plane(0);
        message1.setSender(plane);
        message1.setLogicalSender(task1);
        message1.setLogicalRecipient(plane);
        message1.setRecipient(plane);

        Plane sender1 = context.mock(Plane.class, "Plane 2");
        MSMessage<Task, Plane> message2 = new MSTask2Plane(0);
        message2.setSender(sender1);
        message2.setLogicalSender(task2);
        message2.setLogicalRecipient(plane);
        message2.setRecipient(plane);

        Plane sender2 = context.mock(Plane.class, "Plane 3");
        MSMessage<Task, Plane> message3 = new MSTask2Plane(0);
        message3.setSender(sender2);
        message3.setLogicalSender(task3);
        message3.setLogicalRecipient(plane);
        message3.setRecipient(plane);

        context.checking(new Expectations() {{
            oneOf(plane).getWorld(); will(returnValue(world));
            oneOf(world).getFactory(); will(returnValue(factory));
            oneOf(factory).buildCostFactor(plane); will(returnValue(factor));

            oneOf(plane).getCost(task1); will(returnValue(1d));
            oneOf(plane).getCost(task2); will(returnValue(0d));
            oneOf(plane).getCost(task3); will(returnValue(3d));

            //exactly(3).of(plane).send(with(any(Message.class)));
            exactly(1).of(Matchers.is(plane)).method("send").with(
                    matchMessageValue(1d)
            );
            exactly(1).of(Matchers.is(plane)).method("send").with(
                    matchMessageValue(0d)
            );
            exactly(1).of(Matchers.is(plane)).method("send").with(
                    matchMessageValue(3d)
            );
        }});

        MSPlaneNode instance = new MSPlaneNode(plane);
        instance.addNeighbor(task1, plane);
        instance.addNeighbor(task2, sender1);
        instance.addNeighbor(task3, sender2);
        instance.receive(message1);
        instance.receive(message2);
        instance.receive(message3);

        instance.gather();
        instance.scatter();
    }

    private Matcher matchMessageValue(double value) {
        return Matchers.allOf(
            Matchers.any(Message.class),
            Matchers.hasProperty("value", Matchers.equalTo(value))
        );
    }

}
