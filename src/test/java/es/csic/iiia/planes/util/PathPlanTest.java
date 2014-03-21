/*
 * Copyright (c) 2013, Marc Pujol <mpujol@iiia.csic.es>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.util;

import es.csic.iiia.planes.AbstractPositionedElement;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Positioned;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * Tests for the PathPlan class.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class PathPlanTest {

    private static double DELTA = 1e-5;

    Positioned init = new PositionedMock(new Location(0, 0));
    Positioned p1 = new PositionedMock(new Location(0, 1));
    Positioned p2 = new PositionedMock(new Location(0, 2));
    Positioned p3 = new PositionedMock(new Location(0, 3));
    Positioned p4 = new PositionedMock(new Location(0, 4));

    @Before
    public void setUp() {
        init = new PositionedMock(new Location(0, 0));
        p1 = new PositionedMock(new Location(0, 1));
        p2 = new PositionedMock(new Location(0, 2));
        p3 = new PositionedMock(new Location(0, 3));
        p4 = new PositionedMock(new Location(0, 4));
    }

    /**
     * Test of add method, of class PathPlan.
     */
    @Test
    public void testAdd() {
        PathPlan instance = new PathPlan(init);
        instance.add(p2);
        assertEquals(2f, instance.getCost(), DELTA);
        instance.add(p1);
        assertEquals(2f, instance.getCost(), DELTA);
        instance.add(p4);
        assertEquals(4f, instance.getCost(), DELTA);
        instance.add(p3);
        assertEquals(4f, instance.getCost(), DELTA);
    }

    /**
     * Test of getCostTo method, of class PathPlan.
     */
    @Test
    public void testGetCostTo() {
        PathPlan instance = new PathPlan(init);
        instance.add(p1);
        instance.add(p2);
        instance.add(p3);
        instance.add(p4);
        assertEquals(1f, instance.getCostTo(p1), DELTA);
        assertEquals(2f, instance.getCostTo(p2), DELTA);
        assertEquals(3f, instance.getCostTo(p3), DELTA);
        assertEquals(4f, instance.getCostTo(p4), DELTA);
    }

    /**
     * Test of getPlan method, of class PathPlan.
     */
    @Test
    public void testGetPlan() {
        PathPlan instance = new PathPlan(init);
        instance.add(p2);
        assertArrayEquals(new Object[]{p2}, instance.getPlan().toArray());
        instance.add(p1);
        assertArrayEquals(new Object[]{p1, p2}, instance.getPlan().toArray());
        instance.add(p4);
        assertArrayEquals(new Object[]{p1,p2,p4}, instance.getPlan().toArray());
        instance.add(p3);
        assertArrayEquals(new Object[]{p1,p2,p3,p4}, instance.getPlan().toArray());
    }

    /**
     * Helper mock object implementing the Positioned interface.
     */
    private class PositionedMock extends AbstractPositionedElement {

        public PositionedMock(Location location) {
            super(location);
        }

        @Override
        public void initialize() {}
    }
}