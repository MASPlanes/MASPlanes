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
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FrameTracker {
    
    private long lastTime = System.nanoTime();
    private int counter = 0;
    private String identifier;
    public final Object lock = new Object();
    
    public FrameTracker(String identifier) {
        this.identifier = identifier;
    }
    
    public void tick() {
        counter += 1;
        final long time = System.nanoTime();
        if (time-lastTime > 10e9) {
            Logger.getLogger(FrameTracker.class.getName()).log(Level.INFO, "{0} rate: {1}fps.", new Object[]{identifier, counter});
            counter = 0;
            lastTime = time;
        }
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
    
    public void calibrate() {
        double avg = calibrateIter();
        long min = (long)avg;
        long max = (long)avg;
        for (int i=1; i<1000; i++) {
            final long x = calibrateIter();
            avg += (x - avg)/i+1;
            min = Math.min(min, x);
            max = Math.max(x, max);
        }
        System.err.println("Sleep nanoseconds: " + min + "/" + avg + "/" + max);
    }
    
    private double avg = 1;
    public void delay(int ratio) {
        final long time = System.nanoTime();
        final long remainder = (long)Math.max(0,1e9/ratio - (time - lastTime));
        
        avg = remainder*0.1 + avg*0.9;
        counter += 1;
        
        try {
            final long milis = (long)(remainder/1e6);
            final int  nanos = (int)(remainder%1e6);
            if (milis > 16) {
                //System.err.println("Sleeping for (" + remainder + ") " + milis + "ms " + nanos);
                synchronized(lock) {
                    lock.wait(milis, nanos);
                }
            } else if (milis > 0 || nanos > 0) {
                double dice = Math.random()*16e6;
                if (dice < avg) {
                    //System.err.println("AVGSLeep for (" + remainder + ") " + milis + "ms " + nanos + "ns. AVG: " + avg + ", DICE: " + dice);
                    synchronized(lock) {
                        lock.wait(milis, nanos);
                    }
                } else {
                    //System.err.println("NOTSLeep for (" + remainder + ") " + milis + "ms " + nanos + "ns. AVG: " + avg + ", DICE: " + dice);
                }
            }
            //Logger.getLogger(FrameTracker.class.getName()).log(Level.SEVERE, "Error when trying to sleep for " + remainder + "ns:");
        } catch (InterruptedException ex) {
            Logger.getLogger(FrameTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        lastTime = System.nanoTime();
    }
    
}
