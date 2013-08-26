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

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.messaging.AbstractMessage;
import es.csic.iiia.planes.Task;

/**
 * Message sends during DSA.
 * A Task informs its neighbors tasks that has changed its value.
 * The message is sent by the actual ownrer Plane to the actual ownrer Plane of an other Task.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class TaskMessage extends AbstractMessage{
    /**
     * Task sender of the message.
     */
    private final Task task;
    /*
     * Value assumed by the task. Value represents the Plane that should manage the task according to DSA step.
     */
    private final Plane value;
    
    /**
     * Builds a new TaskMessage.
     * 
     * @param task The task that has changed its value.
     * @param value The new value of the task to comunicate to the message's recipient.
     * @param recipient Plane who received the message.
     */
    public TaskMessage(Task task, Plane value, Plane recipient){
        this.task = task;
        this.value = value;
        super.setRecipient(recipient);
        
    }
    
    /**
     * Get the task that this message refers to. 
     * 
     * @return task that this message refers to.
     */
    public Task getTask(){
        return task;
    }
    
    /**
     * Get the value assumed by the task.
     * 
     * @return the Plane that should manage the task according to DSA step.
     */
    public Plane getValue(){
        return value;
    }
    
}
