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

import es.csic.iiia.planes.Configuration;
import es.csic.iiia.planes.DefaultFactory;
import es.csic.iiia.planes.Factory;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class MSExecutionBehaviorTest {
    private static final Logger LOG = Logger.getLogger(MSExecutionBehaviorTest.class.getName());

    private static Configuration c;

    private Factory factory;
    private World world;
    private MSPlane p1;
    private MSPlane p2;
    private Task t;

    public MSExecutionBehaviorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Properties p = new Properties();
        try {
            p.load(MSExecutionBehaviorTest.class.getResourceAsStream("settings.properties"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        c = new Configuration(p);
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            LOG.severe("Done.");
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MSExecutionBehaviorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Before
    public void setUp() {
        // Create a small problem:
        factory = new DefaultFactory(c);
        world = factory.buildWorld();
        world.init(c.problemDefinition);

    }

    @After
    public void tearDown() {
    }

    private MSPlane buildPlane(double x, double y) {
        MSPlane p = (MSPlane) factory.buildPlane(new Location(x,y));
        p.setCommunicationRange(2000);
        p.setSpeed(0.001);
        p.setBatteryCapacity((long)1e20);
        p.setBattery((long)1e20);
        return p;
    }

    /**
     * Test of getAgent method, of class MSExecutionBehavior.
     */
    @Test
    public void testGetAgent() {
        System.out.println("getAgent");
        MSExecutionBehavior instance = new MSExecutionBehavior(p1);
        MSPlane expResult = p1;
        MSPlane result = instance.getAgent();
        assertSame(expResult, result);
    }

    /**
     * Test of isPromiscuous method, of class MSExecutionBehavior.
     */
    @Test
    public void testIsPromiscuous() {
        System.out.println("isPromiscuous");
        MSExecutionBehavior instance = new MSExecutionBehavior(p1);
        boolean expResult = false;
        boolean result = instance.isPromiscuous();
        assertEquals(expResult, result);
    }

    /**
     * Test of on method, of class MSExecutionBehavior.
     */
    @Test
    public void testOn_MSVariable2FunctionMessage() {
        System.err.println("on(MSVariable2FunctionMessage)");
        MSOldTaskNode f = new MSOldTaskNode(p1, t);
        MSPlaneNode v = new MSPlaneNode(p1);
        p1.getFunctions().put(t, f);
        MSPlaneNode.MSPlane2Task msg = v.buildOutgoingMessage(t, 0);
        msg.setSender(p2);
        msg.setRecipient(p1);
        MSExecutionBehavior instance = new MSExecutionBehavior(p1);
        instance.on(msg);
        assertSame(msg, f.lastMessages.get(p2));
    }

    /**
     * Test of on method, of class MSExecutionBehavior.
     */
    @Test
    public void testOn_MSFunction2VariableMessage() {
        System.err.println("on(MSFunction2VariableMessage)");

        // Prepare the message
        MSOldTaskNode f = new MSOldTaskNode(p1, t);
        MSOldTaskNode.MSTask2Plane msg = f.buildOutgoingMessage(p1, 0);
        msg.setSender(p2);
        msg.setRecipient(p1);

        // Set the domain of the recipient plane's variable
        Map<Task, MSPlane> domain = new TreeMap<Task, MSPlane>();
        domain.put(t, p2);
        final MSPlaneNode var = p1.getVariable();
        var.update(domain);

        // Dispatch the message
        assertEquals(0, var.lastMessages.size());
        MSExecutionBehavior instance = new MSExecutionBehavior(p1);
        instance.on(msg);

        assertSame(msg, var.lastMessages.get(t));
        assertEquals(1, var.lastMessages.size());
    }

    /**
     * Test of afterMessages method, of class MSExecutionBehavior.
     */
    @Test
    public void testAfterMessages() {
        System.err.println("afterMessages");
        world.run();

        for (Plane p : world.getPlanes()) {
            System.err.println(p + ": " + p.getTasks());
        }
    }
}
