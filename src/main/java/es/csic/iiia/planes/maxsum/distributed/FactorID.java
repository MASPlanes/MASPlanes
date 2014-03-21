/*
 * Copyright (c) 2014, Marc Pujol <mpujol@iiia.csic.es>
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
package es.csic.iiia.planes.maxsum.distributed;

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;

/**
 * Uniquely identifies a factor.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FactorID implements Comparable<FactorID> {

    /**
     * Plane where this node is running.
     */
    public final Plane plane;

    /**
     * Task that this factor represents (<em>null</em> if this is a plane's
     * factor).
     */
    public final Task task;

    /**
     * Build a new task factor id.
     *
     * @param plane current owner of the task (where the task node runs)
     * @param task
     */
    public FactorID(Plane plane, Task task) {
        this.plane = plane;
        this.task = task;
    }

    /**
     * Build a new plane factor id.
     *
     * @param plane plane represented by the identified factor.
     */
    public FactorID(Plane plane) {
        this.plane = plane;
        this.task = null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.plane != null ? this.plane.hashCode() : 0);
        hash = 23 * hash + (this.task != null ? this.task.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FactorID other = (FactorID) obj;
        if (this.plane != other.plane && (this.plane == null || !this.plane.equals(other.plane))) {
            return false;
        }
        if (this.task != other.task && (this.task == null || !this.task.equals(other.task))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(FactorID o) {
        final int h1 = hashCode();
        final int h2 = o.hashCode();
        return h1 == h2 ? 0 : (h1 > h2 ? 1 : -1);
    }

    @Override
    public String toString() {
        return "[" + plane + "," + task + "]";
    }

}
