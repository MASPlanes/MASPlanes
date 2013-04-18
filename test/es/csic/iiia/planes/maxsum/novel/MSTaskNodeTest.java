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

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.messaging.Message;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSTaskNodeTest {

    public MSTaskNodeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    /**
//     * Test of addNeighbor method, of class MSTaskNode.
//     */
//    @Test
//    public void testAddNeighbor() {
//        System.out.println("addNeighbor");
//        MSPlane remote = null;
//        MSTaskNode instance = null;
//        instance.addNeighbor(remote);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearNeighbors method, of class MSTaskNode.
//     */
//    @Test
//    public void testClearNeighbors() {
//        System.out.println("clearNeighbors");
//        MSTaskNode instance = null;
//        instance.clearNeighbors();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of receive method, of class MSTaskNode.
     */
    @Test
    public void testReceive() {
        Mockery context = new Mockery();
        final Plane recipient = context.mock(Plane.class);
        Task task = new Task(null);

        Plane sender1 = context.mock(Plane.class, "plane1");
        MSMessage<Plane, Task> message1 = new MSPlane2Task(1);
        message1.setSender(sender1);
        message1.setLogicalSender(sender1);
        message1.setLogicalRecipient(task);
        message1.setRecipient(recipient);

        Plane sender2 = context.mock(Plane.class, "plane2");
        MSMessage<Plane, Task> message2 = new MSPlane2Task(0);
        message2.setSender(sender2);
        message2.setLogicalSender(sender2);
        message2.setLogicalRecipient(task);
        message2.setRecipient(recipient);

        MSTaskNode instance = new MSTaskNode(recipient, task);
        instance.addNeighbor(sender1);
        instance.addNeighbor(sender2);
        instance.receive(message1);
        instance.receive(message2);

        context.checking(new Expectations() {{
            exactly(2).of(recipient).send(with(any(Message.class)));
        }});

        instance.run();
        assertEquals(sender2, instance.makeDecision());
    }

//    /**
//     * Test of run method, of class MSTaskNode.
//     */
//    @Test
//    public void testRun() {
//        System.out.println("run");
//        MSTaskNode instance = null;
//        instance.run();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of makeDecision method, of class MSTaskNode.
//     */
//    @Test
//    public void testMakeDecision() {
//        System.out.println("makeDecision");
//        MSTaskNode instance = null;
//        MSPlane expResult = null;
//        MSPlane result = instance.makeDecision();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class MSTaskNode.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        MSTaskNode instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
