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
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.util.Map;
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
public class MSMoreTasksThanPlanesTest {
    private static final Logger LOG = Logger.getLogger(MSMoreTasksThanPlanesTest.class.getName());

    private static Configuration c;

    private Factory factory;
    private World world;

    public MSMoreTasksThanPlanesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        c = new Configuration();
        c.quiet = true;
        c.planesClass = MSPlane.class;
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        // Create a small problem:
        factory = new DefaultFactory(c);
        world = factory.buildWorld();
        world.addStation(factory.buildStation(new Location(0,0)));
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
     * Test of afterMessages method, of class MSExecutionBehavior.
     */
    @Test
    public void testAfterMessages() {
        System.err.println("afterMessages");

        MSPlane p1 = buildPlane(0, 0);
        MSPlane p2 = buildPlane(2, 0);
        Task t1 = factory.buildTask(new Location(0.75,0));
        Task t2 = factory.buildTask(new Location(2,0));
        Task t3 = factory.buildTask(new Location(3,0));
        p1.addTask(t1);
        p1.addTask(t2);
        p2.addTask(t3);

        for (int i=0; i<100; i++) {
            LOG.log(Level.FINE, "======== Iter {0}", i);
            p1.preStep();
            p2.preStep();
            p1.step();
            p2.step();
            if (i == 5) {
                p2.removeTask(t3);
            }
        }


        try {
            LOG.severe("Done.");
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(MSMoreTasksThanPlanesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
