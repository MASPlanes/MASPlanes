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

import es.csic.iiia.planes.MessagingAgent;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.behaviors.neighbors.NeighborTracking;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the DSA and replying behavior for DSAPlanes.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class DSABehavior extends AbstractBehavior {
    
    private static final Logger LOG = Logger.getLogger(DSABehavior.class.getName());

    private NeighborTracking neighborTracker;
    
    private DSATaskGraph dsa_graph;
    
    private int current_DSA_iteration;
    
    final int n_of_DSA_iterations;
    
    final int DSA_every;
    
    final double DSA_p;

    private enum DSAStep {
        StartDSA, RandomDSA, ContinueDSA, EndDSA, Nothing;
    }
    
    private DSAStep toDo;
    /**
     * Builds a DSA Behavior for the agent passed as parameter.
     * 
     * @param agent that will display this behavior.
     */
    public DSABehavior(DSAPlane agent) {
        super(agent);
        toDo = DSAStep.Nothing;
        dsa_graph = new DSATaskGraph();
        
        //load the settings
        n_of_DSA_iterations = getConfiguration().getDsaIterations();
        DSA_every = getConfiguration().getDsaEvery();
        DSA_p = getConfiguration().getDsaP();
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{NeighborTracking.class};
    }

    @Override
    public void initialize() {
        super.initialize();
        neighborTracker = getAgent().getBehavior(NeighborTracking.class);
    }

    @Override
    public DSAPlane getAgent() {
            return (DSAPlane)super.getAgent();
    }
    /**
     * Takes the neighbor Tasks and inserts them into the graph and updating the
     * near node.
     * 
     * @param pm message contaning the tasks list of a near Plane.
     */
    public void on(PresentationMessage pm){
        
        final Plane sender =(Plane) pm.getSender();
        
        //if you send a broadcast message, also the sender receive the message
        if(sender != getAgent() && toDo != DSAStep.Nothing){
        
            //check if the sender plane is a my neighbor for all time of dsa
            if(neighborTracker.isNeighbor(sender, n_of_DSA_iterations)){
                
                NearPlaneTaskNode newTask;
                for(Task t: pm.getTasks()){
                    newTask = new NearPlaneTaskNode(t,sender);
                    dsa_graph.add(newTask);

                    for(MyPlaneTaskNode my_t : dsa_graph.getMyPlaneTasksNode())
                        my_t.addNeighbor(newTask);
                }            

                for(MyPlaneTaskNode t: dsa_graph.getMyPlaneTasksNode())
                    t.updateDomain(sender);               
                
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "t={0} agent:{1} recive PresentationMessage from {2} updated graph:{3}", 
                            new Object[]{getAgent().getWorld().getTime(), getAgent(), pm.getSender(), dsa_graph});
                }
            }
        }
    }
    /**
     * Manages a message send by a task of another plane in the range, and updates
     * the value of the correspondent {@link NearPlaneTaskNode} in the graph.
     * 
     * @param ts message containg the sender of the task and its new value.
     */
    public void on(TaskMessage ts ){
     
        dsa_graph.getTaskNode(ts.getTask()).setValue(ts.getValue());
            
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} recive TaskMessage({2},{3}) updated graph:{4}", 
                    new Object[]{getAgent().getWorld().getTime(), getAgent(), ts.getTask().getId(),ts.getValue(), dsa_graph});
        }
    }
    /**
     * Assigns the Task at this agent which before it belonged to the sender.
     * 
     * @param rtm message contains the new task for this agent.
     */
    public void on(ReallocatedTaskMessage rtm) {
        getAgent().addTask(rtm.getTask());
        
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} has recived the task {2}", 
                    new Object[]{getAgent().getWorld().getTime(), getAgent(), rtm.getTask().getId()});
        }
    }



    @Override
    public void afterMessages() {

        final Plane agent = getAgent();
        
        if (agent.getWorld().getTime() % this.DSA_every == 0 && toDo == DSAStep.Nothing){
            initializeNewDSAExec();
            
            if( getNumberOfNeighbors() > 0)
                toDo = DSAStep.StartDSA;
            else
                toDo = DSAStep.Nothing;
            
        }
                
        switch(toDo){
            case StartDSA:
              
                beginDSA();
                toDo = DSAStep.RandomDSA;
                break;

            case RandomDSA:
                
                doRandomDSAStep();
                toDo = DSAStep.ContinueDSA;
                break;
                
            case ContinueDSA:
                
                doDSAStep();
                if(current_DSA_iteration < n_of_DSA_iterations)
                    toDo = DSAStep.ContinueDSA;
                else
                    toDo = DSAStep.EndDSA;
                
                break;
                
            case EndDSA:
                
                endDSA();
                toDo = DSAStep.Nothing;                
                break;
                                
            case Nothing:
                
                break;
                
        }
    }
    
   
    
    private void initializeNewDSAExec(){
        dsa_graph.clear();
        current_DSA_iteration = 3;
    }

    /**
     * 
     * @return number of Neighbors Agent without myself
     */
    private int getNumberOfNeighbors(){
        
        int count = 0;
        for(MessagingAgent a: neighborTracker.getNeighbors(n_of_DSA_iterations))                    
            count++;
        
        return count - 1;
        
    }
    
    
    /**
     * Starts a new dsa execution, builds the graph with the tasks of this agent
     * and sends the {@link PresentationMessage}.
     */
    private void beginDSA(){
        final Plane agent = getAgent();

        
        for(Task t: agent.getTasks()){
            dsa_graph.add(new MyPlaneTaskNode(t,agent));
        }

        for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
            for(MyPlaneTaskNode other_tNode : dsa_graph.getMyPlaneTasksNode()){
                if(tNode != other_tNode)
                    tNode.addNeighbor(other_tNode);
            }                                        
        }

        agent.send(new PresentationMessage(agent.getTasks()));   
        
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} START DSA. current graph:{2}", 
                    new Object[]{getAgent().getWorld().getTime(), agent, dsa_graph});
        }
    }
    
    
    /**
     * Executes the random step of DSA and for all tasks chooses 
     * an initial random value.
     */
    private void doRandomDSAStep(){
        final Plane agent = getAgent();
        Random rnd = new Random();
        
        int rnd_index;
        List<Plane> domain;

        for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
            domain = tNode.getDomain();
            rnd_index = rnd.nextInt(domain.size());
            tNode.setValue(domain.get(rnd_index));
            for(Plane p: domain){
                if(p != agent)
                    agent.send(new TaskMessage(tNode.getTask(),tNode.getValue(),p)); 
            }
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} RANDOM DSA. current graph:{2}", 
                    new Object[]{getAgent().getWorld().getTime(), agent, dsa_graph});
        }
    }
    
    /**
     * Tries to improve the correspondent value of all tasks of this agent 
     * by calling the makeDecision method, only if the dsa-p configuration is respected.
     */
    private void doDSAStep(){

        final Plane agent = getAgent();
        Random rnd = new Random();
               
        for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
            if(rnd.nextDouble() < this.DSA_p){
                tNode.makeDecision();
                if(tNode.getLastChangedTime() == agent.getWorld().getTime()){
                    for(Plane p: tNode.getDomain()){
                        if(p != agent){
                            agent.send(new TaskMessage(tNode.getTask(),tNode.getValue(),p)); 
                        }
                    }
                    
                    if (LOG.isLoggable(Level.FINER)){
                        LOG.log(Level.FINER, "t={0} task:{1} changed its value. new value:{2}", 
                        new Object[]{getAgent().getWorld().getTime(), tNode.getTask(),tNode.getValue()});
                    }

                }
            }

        }
        current_DSA_iteration++;
    }
    /**
     * Reallocates tasks if the value is different from the owner.
     */
    private void endDSA(){
        final Plane agent = getAgent();
             
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} END DSA.", 
                    new Object[]{getAgent().getWorld().getTime(), agent});
        }                
                        
        for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
            if(tNode.getValue() != tNode.getOwner()){
                
                agent.removeTask(tNode.getTask());
                agent.send(new ReallocatedTaskMessage(tNode.getTask(),tNode.getValue()));
            }
        }
    }
    
}
