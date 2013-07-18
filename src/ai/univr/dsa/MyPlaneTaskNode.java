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

import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.util.PathPlan;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a TaskNode which is assigned to a Plane.<br>
 * Contains a List of neighbors task, and  a Planes list which represents the 
 * possible domain of this TaskNode.
 * @author Andrea Jeradi, Francesco Donato
 */
public class MyPlaneTaskNode extends AbstractTaskNode {
    /**
     * List of the neighbors of this Task.
     */
    private List<AbstractTaskNode> neighbors;
    /**
     * List which represents the domain.
     */
    private List<Plane> domain;
    /**
     * Builds a MyPlaneTaskNode and assigned owner Plane in the domain. 
     * @param t Task represented by this Node.
     * @param own Task's owner Plane.
     */
    public MyPlaneTaskNode(Task t, Plane own) {
        super(t, own);
        
        this.neighbors = new ArrayList<AbstractTaskNode>();
        this.domain = new ArrayList<Plane>();
        updateDomain(own);
    }
    /**
     * Adds a neighbor Node at this Node.
     * @param n to be added.
     * @return true if and only if adding is succesful, false if n is already in the List of Neighbors.
     */
    public boolean addNeighbor(AbstractTaskNode n) {
       if(!this.neighbors.contains(n)){ 
        return this.neighbors.add(n);
       }
       return false;
    }
    
    /**
     * Gets the List of Neighbors.
     * @return List of Neighbors
     */
    public List<AbstractTaskNode> getNeighbors() {
        
        return new ArrayList<AbstractTaskNode>(this.neighbors);
        
    }
    /**
     * Gets a List which contains values(Planes) of the domain.
     * @return List of Planes
     */
    public List<Plane> getDomain() {
        
        return new ArrayList<Plane>(this.domain);
        
    }
    /**
     * Adds a value(Plane) at the domain List
     * @param p Plane to be added at the domain List.
     * @return true if and only if the domain is correctly updated, false il p is already in the Domain.
     */
    public boolean updateDomain(Plane p) {
        if(!this.domain.contains(p)){
            return this.domain.add(p); 
        }
        
        return false;

    }
    
    public void makeDecision(){
        //controllo se almeno uno dei vicini ha cambiato il proprio valore
        boolean changed = false || this.neighbors.size()==0; //Attenzione in questo modo se c'è solo un task(io) entro sempre nell'if
        int i = 0;
        while(!changed && i < this.neighbors.size())
            changed = this.neighbors.get(i++).isChanged();
        
        //se almeno uno è cambiato ha senso cercare un nuovo valore per me che minimizza il costo totale
        double minCost = Double.MAX_VALUE;
        Plane best = null;
        PathPlan path;
        if(changed){
            for(Plane possibleOwner: this.domain){
                
                //starting from the possibleOwner location
                path = new PathPlan(possibleOwner);
                
                //and add me to the path
                path.add(this.getTask());
                
                //and add the other tasks to the path that they have the same my current value
                for(AbstractTaskNode other: this.neighbors)
                    if(other.getValue() == possibleOwner)
                        path.add(other.getTask());
                
                //what is the total cost of the path???
                if(path.getCost() < minCost){
                    //change
                    minCost = path.getCost();
                    best = possibleOwner;
                            
                }
     
            }
            //the best new value has been found
            this.setValue(best);
            
        }
    }
}
