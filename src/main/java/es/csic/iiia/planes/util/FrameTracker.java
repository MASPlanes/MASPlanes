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
package es.csic.iiia.planes.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delays execution of a thread to achieve the desired frames per second.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FrameTracker {
    private static final Logger LOG = Logger.getLogger(FrameTracker.class.getName());

    private long lastTime = System.nanoTime();
    public final Object lock = new Object();
    private double min_sleeptime_avg = 16e6;
    private boolean unlocked = false;
    private int speed;
    private double avg = Double.NaN;
    private long lastTick = 0;

    public FrameTracker(int speed) {
        this.speed = speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        avg = Double.NaN;
    }

    private long calibrateIter() {
        long t1 = System.nanoTime();
        try {
            Thread.sleep(0l, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(FrameTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
        long t2 = System.nanoTime();
        return t2 - t1;
    }

    /**
     * Calibrates this frame tracker, computing the minimum number of
     * nanoseconds taken by a <em>sleep()<em> call.
     */
    public void calibrate() {
        min_sleeptime_avg = calibrateIter();
        long min = (long)min_sleeptime_avg;
        long max = (long)min_sleeptime_avg;
        for (int i=1; i<1000; i++) {
            final long x = calibrateIter();
            min_sleeptime_avg += (x - min_sleeptime_avg)/i+1;
            min = Math.min(min, x);
            max = Math.max(x, max);
        }
        LOG.log(Level.SEVERE, "Sleep nanoseconds: {0}/{1}/{2}", new Object[]{min, min_sleeptime_avg, max});
    }

    /**
     * Delays the execution of this thread, trying to achieve the given ratio
     * of executions per second.
     *
     * @param ratio of executions per second that we want to accomplish.
     */
    public void delay() {
        final long time = System.nanoTime();
        final long remainder = (long)Math.max(0,1e9/2* - (time - lastTime));
        boolean local_unlocked = false;

        avg = Double.isNaN(avg) ? remainder*0.1 : remainder*0.1 + avg*0.9;
        avg = remainder;

        final long milis = (long)(remainder/1e6);
        final int  nanos = (int)(remainder%1e6);
        if (milis*1e3 > min_sleeptime_avg) {
            //System.err.println("Sleeping for (" + remainder + ") " + milis + "ms " + nanos);
            local_unlocked = sleep(milis, nanos);
        } else if (milis > 0 || nanos > 0) {
            double dice = Math.random()*min_sleeptime_avg;
            if (dice < avg) {
                //System.err.println("AVGSLeep for (" + remainder + ") " + milis + "ms " + nanos + "ns. AVG: " + avg + ", DICE: " + dice);
                local_unlocked = sleep(milis, nanos);
            } else {
                //System.err.println("NOTSLeep for (" + remainder + ") " + milis + "ms " + nanos + "ns. AVG: " + avg + ", DICE: " + dice);
            }
        }

        if (local_unlocked) {
            avg = 1;
            System.err.println("Unlocked!");
        }
        lastTime = System.nanoTime();
    }

    public boolean sleep(long milis, int nanos) {
        boolean result;
        synchronized(lock) {
            try {
                lock.wait(milis, nanos);
            } catch (InterruptedException ex) {
                Logger.getLogger(FrameTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
            result = unlocked;
            unlocked = false;
        }
        return result;
    }

    public void interrupt() {
        synchronized(lock) {
            unlocked = true;
            lock.notify();
        }
    }

}