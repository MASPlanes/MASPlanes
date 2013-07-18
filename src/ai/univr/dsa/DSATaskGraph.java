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
package ai.univr.dsa;

import es.csic.iiia.planes.Task;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrea Jeradi, Francesco Donato
 */
public class DSATaskGraph {

    private Map<Task,MyPlaneTaskNode> myTasks;
    private Map<Task,NearPlaneTaskNode> otherTasks;

    public DSATaskGraph() {
       
      this.myTasks = new HashMap<Task,MyPlaneTaskNode>();
      this.otherTasks = new HashMap<Task,NearPlaneTaskNode>();
      
    }
    
    public void add(AbstractTaskNode n) {
       
        if(n instanceof MyPlaneTaskNode) {
            
            this.myTasks.put(n.getTask(),(MyPlaneTaskNode)n);
        }
        else if(n instanceof NearPlaneTaskNode) {
            
            this.otherTasks.put(n.getTask(), (NearPlaneTaskNode)n);
        }
        else {
            
            throw new ClassCastException();
            
        }   
    }
    
    public AbstractTaskNode getTaskNode(Task t) {
        
        if(this.myTasks.containsKey(t)) {
            return this.myTasks.get(t);
        }
        else if(this.otherTasks.containsKey(t)) {
            return this.otherTasks.get(t);
        }
        else {
            return null;
        }
        
    }
   
    public Collection<MyPlaneTaskNode> getMyPlaneTasksNode() {
        
        return this.myTasks.values();
        
    }
    
    public Collection<NearPlaneTaskNode> getNearPlaneTaskNode() {
        
        return this.otherTasks.values();
        
    }
    
    public void clear() {
        
        this.myTasks.clear();
        this.otherTasks.clear();
    }
}
