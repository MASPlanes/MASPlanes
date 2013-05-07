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
package es.csic.iiia.planes.omniscient;

import es.csic.iiia.planes.DefaultWorld;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.World;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
public class SSIAllocationTest {

    public SSIAllocationTest() {
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

    /**
     * Test of allocate method, of class SSIAllocation.
     */
    @Test
    public void testAllocate() {

        World w = new DefaultWorld(null);
        OmniscientPlane p1 = new OmniscientPlane(new Location(0,0));
        OmniscientPlane p2 = new OmniscientPlane(new Location(1,1));
        w.addPlane(p1);
        w.addPlane(p2);
        OmniscientPlane[] planes = new OmniscientPlane[]{p1, p2};

        Task t1 = new Task(new Location(0,1));
        w.addTask(t1);
        Task t2 = new Task(new Location(1,0));
        w.addTask(t2);

        // Instantiate the visibility map
        TreeMap<MessagingAgent, Set<Task>> visibilityMap = new TreeMap<MessagingAgent, Set<Task>>();
        visibilityMap.put(p1, new TreeSet<Task>(Arrays.asList(new Task[]{t1,t2})));
        visibilityMap.put(p2, new TreeSet<Task>(Arrays.asList(new Task[]{t1,t2})));

        TreeMap<OmniscientPlane, Task> assignmentMap = new TreeMap<OmniscientPlane, Task>();
        TreeMap<Task, OmniscientPlane> reverseMap = new TreeMap<Task, OmniscientPlane>();

        SSIAllocation instance = new SSIAllocation();
        instance.allocate(w, planes, visibilityMap, assignmentMap, reverseMap);

        assertEquals(assignmentMap.get(p1), t1);
        assertEquals(assignmentMap.get(p2), t2);
    }

    /**
     * Test of allocate method, of class SSIAllocation.
     */
    @Test
    public void testAllocate2() {
        System.err.println("testAllocate2");
        World w = new DefaultWorld(null);
        OmniscientPlane p1 = new OmniscientPlane(new Location(0,0));
        OmniscientPlane p2 = new OmniscientPlane(new Location(1,1));
        w.addPlane(p1);
        w.addPlane(p2);
        OmniscientPlane[] planes = new OmniscientPlane[]{p1, p2};

        Task t1 = new Task(new Location(0,1));
        w.addTask(t1);
        Task t2 = new Task(new Location(0,2));
        w.addTask(t2);
        Task t3 = new Task(new Location(-1,1));
        w.addTask(t3);
        Task t4 = new Task(new Location(-1,1.9));
        w.addTask(t4);

        // Instantiate the visibility map
        TreeMap<MessagingAgent, Set<Task>> visibilityMap = new TreeMap<MessagingAgent, Set<Task>>();
        visibilityMap.put(p1, new TreeSet<Task>(Arrays.asList(new Task[]{t1,t2,t3,t4})));
        visibilityMap.put(p2, new TreeSet<Task>(Arrays.asList(new Task[]{t1,t2,t3,t4})));

        TreeMap<OmniscientPlane, Task> assignmentMap = new TreeMap<OmniscientPlane, Task>();
        TreeMap<Task, OmniscientPlane> reverseMap = new TreeMap<Task, OmniscientPlane>();

        SSIAllocation instance = new SSIAllocation();
        instance.allocate(w, planes, visibilityMap, assignmentMap, reverseMap);

        assertEquals(instance.getPlannedLocations(p1),
                Arrays.asList(new Location[]{t1.getLocation(), t3.getLocation()}));
        assertEquals(instance.getPlannedLocations(p2),
                Arrays.asList(new Location[]{t2.getLocation(), t4.getLocation()}));
    }

}
