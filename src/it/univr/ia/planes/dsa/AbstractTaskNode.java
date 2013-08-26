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
import es.csic.iiia.planes.Task;

/**
 * This Class represents a generic Node which contains a Task, its owner Plane,
 * the current value, which is represented by the current plane that has been 
 * assigned in the i-th step at that task and the Plane that was been assigned 
 * in the i-1-th step.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public abstract class AbstractTaskNode {
    
    /**
     * Task that is represented from this Node.
     */
    private final Task t;
    /**
     * Plane owner of this Node.
     */
    private final Plane owner;
    /**
     * Plane which is currently assigned at this Node.
     */
    private Plane value;
    
    /**
     * Builds a Node contained a Task and a Plane owner.
     * @param t the Task.
     * @param own Plane owner.
     */
    public AbstractTaskNode(Task t, Plane own){
        this.t = t;
        this.owner = own;
        this.value = null;
    }
    
    /**
     * Get the Task represented by this Node. 
     * @return the Task.
     */
    public Task getTask(){
        return this.t;   
    }
    
    /**
     * Get a Plane which represents the owner of this Node.
     * @return the owner
     */
    public Plane getOwner(){
        return this.owner;
    }
    
    /**
     * Get a Plane which represents the current assignment for this Node.
     * @return the plane that is the current value of the Node.
     */
    public Plane getValue(){
        return this.value;
    }
    
    /**
     * Set the current value of this Node and tracks the old value.
     * @param p the new value of this Node.
     */
    public void setValue(Plane p){
            this.value = p;
    }
    
    @Override
    public String toString(){
        return t.getId()+"="+value;
        //return t.getId() +" value:"+value+" last_changed_time:"+last_changed_time+" own:" + this.owner ;
    }
}
