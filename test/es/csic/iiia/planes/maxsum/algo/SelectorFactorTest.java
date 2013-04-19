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
package es.csic.iiia.planes.maxsum.algo;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SelectorFactorTest {

    private final double DELTA = 0.0001d;

    @Test
    public void testRun1() {
        double[] values  = new double[]{0, 1, 2};
        double[] results = new double[]{-1, 0, 0};
        run(values, results);
    }

    @Test
    public void testRun2() {
        double[] values  = new double[]{0, 0, 2};
        double[] results = new double[]{0, 0, 0};
        run(values, results);
    }

    @Test
    public void testRun3() {
        double[] values  = new double[]{-1, 2};
        double[] results = new double[]{-2, 1};
        run(values, results);
    }

    private void run(double[] values, double[] results) {

        // Setup incoming messages
        WorkloadFactor[] cfs = new WorkloadFactor[values.length];
        SelectorFactor s = new SelectorFactor();
        for (int i=0; i<cfs.length; i++) {
            cfs[i] = new WorkloadFactor();
            s.addNeighbor(cfs[i]);
            s.receive(new Message(cfs[i], values[i]));
        }

        // The tick is to make the messages current
        s.tick();
        s.gather();

        for (int i=0; i<cfs.length; i++) {
            cfs[i].tick();
            assertEquals(cfs[i].getMessage(s).value, results[i], DELTA);
        }
    }
}