/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Expression application is undefined on line 6, column 57 in Templates/Licenses/license-bsd.txt..
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
 *   Neither the name of Expression application is undefined on line 21, column 41 in Templates/Licenses/license-bsd.txt.
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   Expression application is undefined on line 25, column 21 in Templates/Licenses/license-bsd.txt.
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

import es.csic.iiia.planes.AbstractPlane;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class OmniscientPlane extends AbstractPlane {

    private static HashMap<Task, OmniscientPlane> beingAttended = new HashMap<Task, OmniscientPlane>();

    public OmniscientPlane(Location location) {
        super(location);
    }

    @Override
    protected void taskCompleted(Task t) {
        removeTask(t);
        replan();
    }

    @Override
    public void addTask(Task task) {
        if (getTasks().contains(task)) {
            System.err.println("Double addition? no good!");
            System.exit(0);
        }
        super.addTask(task);
    }



    @Override
    protected void taskAdded(Task t) {
        Task oldt = getNextTask();
        if (oldt != null) {
            setNextTask(null);
            removeTask(oldt);
        }

        if (getTasks().size() > 2) {
            System.err.println("Big time error.");
            System.exit(0);
        }

        beingAttended.put(t, this);
        setNextTask(t);
    }

    private void replan() {
        List<Task> available = new ArrayList<Task>(getWorld().getTasks());

        Task best = getNearest(available);
        while (best != null) {
            if (beingAttended.containsKey(best)) {
                double other_distance = beingAttended.get(best).getLocation().distance(best.getLocation());
                double my_distance    = getLocation().distance(best.getLocation());
                if (my_distance < other_distance) {
                    beingAttended.get(best).removeTask(best);
                    addTask(best);
                    return;
                } else {
                    available.remove(best);
                    best = getNearest(available);
                }
            } else {
                addTask(best);
                return;
            }
        }
    }

    @Override
    protected void taskRemoved(Task t) {
        if (getNextTask() == t) {
            setNextTask(null);
            replan();
        }
        beingAttended.remove(t);
    }

    @Override
    public List<Location> getPlannedLocations() {
        return null;
    }


}
