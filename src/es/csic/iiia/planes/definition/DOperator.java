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
package es.csic.iiia.planes.definition;

import java.util.ArrayList;

/**
 * Definition of an operator.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class DOperator extends DLocation {
    private double communicationRange;
    private ArrayList<DTask> tasks = new ArrayList<DTask>();

    /**
     * Get the list of tasks in this scenario.
     *
     * @return list of tasks in this scenario.
     */
    public ArrayList<DTask> getTasks() {
        return tasks;
    }

    /**
     * Set the list of tasks in this scenario.
     *
     * @param tasks list of tasks to set.
     */
    public void setTasks(ArrayList<DTask> tasks) {
        this.tasks = tasks;
    }

    /**
     * Get the communication range of this operator.
     *
     * @return the communication range of this operator.
     */
    public double getCommunicationRange() {
        return communicationRange;
    }

    /**
     * Set the communication range of this operator.
     *
     * @param communicationRange new communication range.
     */
    public void setCommunicationRange(double communicationRange) {
        this.communicationRange = communicationRange;
    }

}
