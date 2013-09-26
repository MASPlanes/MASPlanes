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

import es.csic.iiia.planes.Task;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the DSA graph of a Plane.<br>
 * It's builded by a Plane during the DSA with its TaskNodes and with the received TaskNodes from Planes near.<br>
 * It is formed by AbstractTaskNode.
 * Nodes are dived into two set, MyPlaneTaskNode, and NearPlaneTaskNode.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class DSATaskGraph {
    /**
     * Map that represents the link between a myTask and a Node of the Graph.
     */
    private Map<Task,MyPlaneTaskNode> myTasks;
    /**
     * Map that represents the link between a Task owned by a near Plane and a Node of the Graph.
     */
    private Map<Task,NearPlaneTaskNode> otherTasks;
    
    /**
     * Builds an empty DSATaskGraph
     */
    public DSATaskGraph() {
       
      this.myTasks = new HashMap<Task,MyPlaneTaskNode>();
      this.otherTasks = new HashMap<Task,NearPlaneTaskNode>();
      
    }
    
    /**
     * Adds an AbstractTaskNode to the Graph.
     * @param n Node to be added to the Graph.
     * @exception IllegalArgumentException the node n as parameter is already in the graph.
     */
    public void add(AbstractTaskNode n) {
        
        if(this.myTasks.containsKey(n.getTask()) || this.otherTasks.containsKey(n.getTask())) {
            throw new IllegalArgumentException(" Try to add a node of a task that is already in the graph ");    
        }
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
    
    /**
     * Gets the Node that represents the Task passed as parameter.
     * @param t Task represented by the searched Node.
     * @return the Node looked for or null if the Node linked by the Task doesn't exists.
     */
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
    
    /**
     * Gets the Set of MyPlaneTaskNode.
     * @return a Collection that represents the set.
     */
    public Collection<MyPlaneTaskNode> getMyPlaneTasksNode() {
        return this.myTasks.values();
    }
    
    /**
     * Gets the Set of NearPlaneTaskNode.
     * @return a Collection that represents the set.
     */
    public Collection<NearPlaneTaskNode> getNearPlaneTaskNode() {
        return this.otherTasks.values(); 
    }
    
    /**
     * Clears the Graph from all Nodes.
     */
    public void clear() {
        this.myTasks.clear();
        this.otherTasks.clear();
    }
    
    /**
     * Checks if the graph is empty.
     * @return true if and only if the graph i empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.myTasks.isEmpty() && this.otherTasks.isEmpty();
    }
    
    @Override
    public String toString(){
        String s="{";
        for(MyPlaneTaskNode t: this.getMyPlaneTasksNode())
            s+="["+t+"],";
        s+="}{";
        for(NearPlaneTaskNode t: this.getNearPlaneTaskNode())
            s+="["+t+"],";
        s+="}";
        return s;        
    }
}
