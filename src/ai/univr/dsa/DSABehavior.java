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

    public void on(PresentationMessage pm){
        /*
        recupera il Plane sender
        recupera i task da lui conosciuti
        se resterà nel range di comunicazione per tutta la durata di dsa allora aggiungi al grafo dei task, per ogni task del sender, un nodo di tipo NearPlaneTaskNode rappresentante del task
        aggiorna il dominio dei nodi(quindi task) da me gestiti  MyPlaneTaskNode inserendo il Plane sender  
        aggiorna i vicini dei nodi(quindi task) da me gestiti  MyPlaneTaskNode inserendo i nuovi Task	
        */
        final Plane sender =(Plane) pm.getSender();
        
        //if you send a broadcast message, also the sender receive the message
        if(sender != getAgent() && toDo != DSAStep.Nothing){
        
            //verifico che il Plane vicino resti tale per tutta la durata di DSA        
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

    public void on(TaskMessage ts ){
        /*
        recupara dal messaggio il riferimerto al Task
        recupara dal messaggio il nuovo valore assunto dal task( il valore è di tipo Plane es P2)
        cerca nel grafo il NearPlaneTaskNode che rappresenta il task e aggiorna il nodo con il nuovo valore
        */
     
        dsa_graph.getTaskNode(ts.getTask()).setValue(ts.getValue());
            
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} recive TaskMessage({2},{3}) updated graph:{4}", 
                    new Object[]{getAgent().getWorld().getTime(), getAgent(), ts.getTask().getId(),ts.getValue(), dsa_graph});
        }
    }

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
    
    private int getNumberOfNeighbors(){
        
        int count = 0;
        for(MessagingAgent a: neighborTracker.getNeighbors(n_of_DSA_iterations))                    
            count++;
        
        return count - 1;
        
    }
    
    
    /*per ogni mio Task crea il relativo MyPlaneTaskNode indicando il Task che rappresenta e aggiungendo al dominio il riferimento a me (inteso come Plane)
        per ogni mio PlaneTaskNode aggiorna la lista dei suoi vicini aggiungendo tutti gli altri nodi creati nella fase precedente
        invia in broadcast un PresentationMessage {“sono P3 e questa è la mia lista di Task”}*/

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
    
    
    
    /*
        l’algoritmo prevederebbe una lettura dei PresentationMessage degli aerei vici e un conseguente aggiornamento del grafo ma isiccome siamo in afterMessage tutto ciò è stato già fatto in on(PresentationMessage)
        per ogni mio PlaneTaskNode scelgli casualmente in valore per esso tra i possibili valori appartenenti al dominio, 
        *aggiorna il campo value del nodo e crea /invia un TaskMessage per ogni aereo presente nel dominio del nodo(escluso me stesso ovviamente)
        
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
    
    private void doDSAStep(){
        /* case ContinueDSA
        come prima cosa bisognerebbe leggere i TaskMessage degli aerei vicini e aggiornare il proprio grafo con le nuove info. Questo viene fatto in on(TaskMessage)
        genera un numero random rn compreso [0,1)
        se rn < p puoi ‘giocare’
        per ogni mio task (PlaneTaskNode ) ti
        per ogni possibile valore del dominio assegnabile a ti calcola il costo dell’assegnamento totale  condiderando il valore corrente dei nodi vicini.
        prendi il valore che minimizza il costo totale
        se il valore scelto è diverso da quello attuale crea /invia un TaskMessage per ogni aereo presente nel dominio del nodo(escluso me stesso ovviamente) di ti
        NB : rimane ancora da chiarire come bisogna comportari con i task che vengono gestiti dallo stesso aereo es. se l’ordine pi processamento dei task per l’aereo è t1,t2 se il valore di t1=P1 e dopo l’iterazione di DSA diventa P2, t2 quale dei due valori deve usare per la sua iterazione? probabilmente P1 perchè anche se i due task sono gestiti dallo stesso aereo l’unico modo di comunicare che dovrebbero avere è quello via messaggi e non attraverso condivisione di dati.

        NB: bisogna rianalizzare la questione della condizione di terminazione cercando di far terminare dsa se converge in un minino locale

                 */
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
    
    private void endDSA(){
        final Plane agent = getAgent();
             
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "t={0} agent:{1} END DSA.", 
                    new Object[]{getAgent().getWorld().getTime(), agent});
        }                
                        
        for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
            if(tNode.getValue() != tNode.getOwner()){
                agent.send(new ReallocatedTaskMessage(tNode.getTask(),tNode.getValue()));
                agent.removeTask(tNode.getTask());
            }
        }
    }


}
