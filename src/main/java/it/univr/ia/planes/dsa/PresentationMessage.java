/*
 * Copyright (c) 2013, Andrea Jeradi, Francesco Donato
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
package it.univr.ia.planes.dsa;

import es.csic.iiia.planes.messaging.AbstractMessage;
import es.csic.iiia.planes.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * Message used to start DSA.<br>
 * 
 * All Planes send this message to comunicate to the other planes in the comunication range the list of the known tasks.
 * In this way, a plane can update its knowledge of the world
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class PresentationMessage extends AbstractMessage{
    /**
     * Tasks list that the sender plane know.
     */    
    private final List<Task> tasks;
    
    /**
     * Build a PresentationMessage containing the task list.
     * @param tasks list of all task known by sender plane, in this moment.
     */
    public PresentationMessage(List<Task> tasks){
        this.tasks = new ArrayList<Task>(tasks);
    }
    
    /**
     * Get a list of task.
     * @return a list of task.
     */
    public List<Task> getTasks(){
        return this.tasks;
    }
}
