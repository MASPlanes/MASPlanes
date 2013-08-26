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
 * Message to notify an agent that it has a new task to do.
 * 
 * Message sends by the previous owner plane of the task.
 * After the execution of DSA, the task has been delegated by the sender agent to receiver.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class ReallocatedTaskMessage extends AbstractMessage{
    /**
     * Task to do.
     */
    private final Task task;

    /**
     * Builds a new message to inform the intended recipient that it has a new task to do.
     * 
     * @param task that has changed the owner.
     * @param  recipient Plane who receive the message.
     */
    public ReallocatedTaskMessage(Task task, Plane recipient) {
        this.task = task;
        super.setRecipient(recipient);
    }
    
    /**
     * Get the task that this message refers to.
     *
     * @return task that this message refers to.
     */
    public Task getTask() {        
        return task;
    }   
}
