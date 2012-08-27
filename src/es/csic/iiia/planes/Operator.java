/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2012, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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
package es.csic.iiia.planes;

import es.csic.iiia.planes.io.DTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Operator extends AbstractElement implements Agent {
    
    private ArrayList<DTask> tasks;
    private int nextTask = 0;
    private Random r = new Random(0);
    private boolean done = false;
    
    public Operator(ArrayList<DTask> tasks) {
        this.tasks = tasks;
        Collections.sort(this.tasks, new TaskSorter());
    }

    @Override
    public void step() {
        while (!done && tasks.get(nextTask).getTime() == getWorld().getTime()) {
            Task t = createTask(tasks.get(nextTask));
            submitTaskToPlane(t);
            
            tasks.set(nextTask, null);
            nextTask++;
            
            if (nextTask == tasks.size()) {
                tasks = null;
                done = true;
            }
        }
    }

    private Task createTask(DTask nt) {
        Location l = new Location(nt.getX(), nt.getY());
        Task t = Factory.buildTask(l);
        getWorld().addTask(t);
        return t;
    }

//    private void submitTaskToPlane(Task t) {
//        final List<Plane> planes = getWorld().getPlanes();
//        int pnum = r.nextInt(planes.size());
//        planes.get(pnum).addTask(t);
//    }
    
    private void submitTaskToPlane(Task t) {
        final List<Plane> planes = getWorld().getPlanes();
        Location l = t.getLocation();
        
        double mind = Double.MAX_VALUE;
        Plane nearest = null;
        for (Plane p : planes) {
            final double d = p.getLocation().getDistance(l);
            if (d < mind) {
                mind = d;
                nearest = p;
            }
        }
        nearest.addTask(t);
    }
    
    private class TaskSorter implements Comparator<DTask> {
        @Override
        public int compare(DTask t, DTask t1) {
            return Long.valueOf(t.getTime()).compareTo(t1.getTime());
        }
    }
    
}
