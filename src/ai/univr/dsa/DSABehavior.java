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

    //private Map<Task, List<BidMessage>> bids = new TreeMap<Task, List<BidMessage>>();

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
        if(sender != getAgent()){
        
            //verifico che il Plane vicino resti tale per tutta la durata di DSA        
            if(neighborTracker.isNeighbor(sender, n_of_DSA_iterations)){

                NearPlaneTaskNode newTask;
                for(Task t: pm.getTasks()){
                    newTask = new NearPlaneTaskNode(t,sender);
                    dsa_graph.add(newTask);

                    for(MyPlaneTaskNode my_t : dsa_graph.getMyPlaneTasksNode()){                
                        my_t.addNeighbor(newTask);
                    }
                }            

                for(MyPlaneTaskNode t: dsa_graph.getMyPlaneTasksNode()){
                    t.updateDomain(sender);               
                } 
                
                
                if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+getAgent().getWorld().getTime()+" "+getAgent()+" on PresentationMess grafo:"+dsa_graph);
        }
        }
    }

    public void on(TaskMessage ts ){
        /*
        recupara dal messaggio il riferimerto al Task
        recupara dal messaggio il nuovo valore assunto dal task( il valore è di tipo Plane es P2)
        cerca nel grafo il NearPlaneTaskNode che rappresenta il task e aggiorna il nodo con il nuovo valore
        */
        //try{
            dsa_graph.getTaskNode(ts.getTask()).setValue(ts.getValue());
            
            if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+getAgent().getWorld().getTime()+" "+getAgent()+" on TaskMess("+ts.getTask().getId()+","+ts.getValue()+") grafo:"+dsa_graph);
//        }
//        catch(Exception eex){
//            System.out.println("dfsf");
//            
//        }
        
    }

    public void on(ReallocatedTaskMessage rtm) {
        getAgent().addTask(rtm.getTask());

        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "{0} recives task {1}", new Object[]{getAgent(), rtm.getTask().getId()});
        }
    }

    @Override
    public void preStep() { //forse anche beforeMessages
        /*
        considerando le configurazioni su ogni quanto fare DSA e il n. di iterazioni di quest’ultimo
        assegna a toDo lo step da fare, nel seguente modo:
        se è tempo di iniziare dsa e c’è almeno un vicino per 200 step toDo=StartDSA else toDo=nothing
        se toDo nel step precedente era toDo=StartDSA allora toDO=RandomDSA
        se toDo nel step precedente era toDo=RandomDSA allora toDO=ContineDSA
        se toDo nel step precedente era toDo=ContineDSA e il numero di iterazioni fatte è minore del relativo parametro di conf. allora toDo=ContineDSA Nota: da qualche parte bisognerà incrementare il contatore delle iterazioni.
        se toDo nel step precedente era toDo=ContineDSA e il numero di iterazioni fatte è uguale al relativo parametro di conf. allora toDo=EndDSA 
        else toDo=Nothing
        */
        final Plane agent = getAgent();
        
        /*if (agent.getWorld().getTime() % this.DSA_every == 0 &&
            neighborTracker.hasNeighbors(n_of_DSA_iterations) && 
            toDo == DSAStep.Nothing) {
          */  
        if (agent.getWorld().getTime() % this.DSA_every == 0 &&
            toDo == DSAStep.Nothing) {
            
            toDo = DSAStep.StartDSA;
            
        }
        else if(toDo == DSAStep.StartDSA)// && !this.dsa_graph.isEmpty())
            toDo = DSAStep.RandomDSA;
        
        else if(toDo == DSAStep.RandomDSA)
            toDo = DSAStep.ContinueDSA;
        
        else if(toDo == DSAStep.ContinueDSA) 
            if(current_DSA_iteration < n_of_DSA_iterations)
                toDo = DSAStep.ContinueDSA;
            else
                toDo = DSAStep.EndDSA;
        
        else
            toDo = DSAStep.Nothing;
        
        
        
        //final Plane agent = getAgent();

        /* Auction our tasks every minute
        if (agent.getWorld().getTime() % getConfiguration().getAucEvery() == 0) {
            for(Task t : agent.getTasks()) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "{0} auctioning task {1}", new Object[]{agent, t.getId()});
                }

                AskMessage ask = new AskMessage(t);
                agent.send(ask);
            }
        }*/

    }

    @Override
    public void afterMessages() {
        /*
            switch(toDo){
        case StartDSA
        pulisci le strutture dati e azzera il contatore delle iterazioni fatte
        per ogni mio Task crea il relativo PlaneTaskNode indicando il Task che rappresenta e aggiungendo al dominio il riferimento a me (inteso come Plane)
        per ogni mio PlaneTaskNode aggiorna la lista dei suoi vicini aggiungendo tutti gli altri nodi creati nella fase precedente
        invia in broadcast un PresentationMessage {“sono P3 e questa è la mia lista di Task”}
                        
        case RandomDSA
        l’algoritmo prevederebbe una lettura dei PresentationMessage degli aerei vici e un conseguente aggiornamento del grafo ma isiccome siamo in afterMessage tutto ciò è stato già fatto in on(PresentationMessage)
        per ogni mio PlaneTaskNode scelgli casualmente in valore per esso tra i possibili valori appartenenti al dominio, aggiorna il campo value del nodo e crea /invia un TaskMessage per ogni aereo presente nel dominio del nodo(escluso me stesso ovviamente)
        
        * case ContinueDSA
        come prima cosa bisognerebbe leggere i TaskMessage degli aerei vicini e aggiornare il proprio grafo con le nuove info. Questo viene fatto in on(TaskMessage)
        genera un numero random rn compreso [0,1)
        se rn < p puoi ‘giocare’
        per ogni mio task (PlaneTaskNode ) ti
        per ogni possibile valore del dominio assegnabile a ti calcola il costo dell’assegnamento totale  condiderando il valore corrente dei nodi vicini.
        prendi il valore che minimizza il costo totale
        se il valore scelto è diverso da quello attuale crea /invia un TaskMessage per ogni aereo presente nel dominio del nodo(escluso me stesso ovviamente) di ti
        NB : rimane ancora da chiarire come bisogna comportari con i task che vengono gestiti dallo stesso aereo es. se l’ordine pi processamento dei task per l’aereo è t1,t2 se il valore di t1=P1 e dopo l’iterazione di DSA diventa P2, t2 quale dei due valori deve usare per la sua iterazione? probabilmente P1 perchè anche se i due task sono gestiti dallo stesso aereo l’unico modo di comunicare che dovrebbero avere è quello via messaggi e non attraverso condivisione di dati.

        NB: bisogna rianalizzare la questione della condizione di terminazione cercando di far terminare dsa se converge in un minino locale

        case EndDSA
        per ogni mio nodo se terminato DSA il valore(Plane che deve farlo) è diverso da te stesso invia un  ReallocatedTaskMessage al nuovo gestore del task.
        rimuovi il Task dalla tua lista.

        */
        final Plane agent = getAgent();
        Random rnd = new Random();
        
        switch(toDo){
            case StartDSA:
                int count = 0;
                for(MessagingAgent a: neighborTracker.getNeighbors(n_of_DSA_iterations)){                    
                    count++;                    
                }

                if( count > 1) {//neighborTracker.hasNeighbors(n_of_DSA_iterations) &&
                    
                    initializeNewDSAExec();

                    /*per ogni mio Task crea il relativo MyPlaneTaskNode indicando il Task che rappresenta e aggiungendo al dominio il riferimento a me (inteso come Plane)
            per ogni mio PlaneTaskNode aggiorna la lista dei suoi vicini aggiungendo tutti gli altri nodi creati nella fase precedente
            invia in broadcast un PresentationMessage {“sono P3 e questa è la mia lista di Task”}*/

                    for(Task t: agent.getTasks()){
                        dsa_graph.add(new MyPlaneTaskNode(t,agent));
                    }

                    for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
                        for(MyPlaneTaskNode other_tNode : dsa_graph.getMyPlaneTasksNode()){
                            if(tNode != other_tNode)
                                tNode.addNeighbor(other_tNode);
                        }                                        
                    }
if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+agent.getWorld().getTime()+" "+agent+" start dsa grafo:"+dsa_graph);
                    agent.send(new PresentationMessage(agent.getTasks()));
                }
                else{
                    if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+agent.getWorld().getTime()+" "+agent+" NON inizio dsa ho il colore: "+agent.getColor());
                    toDo = DSAStep.Nothing;
                }
                break;
                
            case RandomDSA:
                /*
        l’algoritmo prevederebbe una lettura dei PresentationMessage degli aerei vici e un conseguente aggiornamento del grafo ma isiccome siamo in afterMessage tutto ciò è stato già fatto in on(PresentationMessage)
        per ogni mio PlaneTaskNode scelgli casualmente in valore per esso tra i possibili valori appartenenti al dominio, 
        *aggiorna il campo value del nodo e crea /invia un TaskMessage per ogni aereo presente nel dominio del nodo(escluso me stesso ovviamente)
        
                 */
                
                int rnd_index;
                List<Plane> domain;
                
                for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
                    domain = tNode.getDomain();
                    rnd_index = rnd.nextInt(domain.size());
                    tNode.setValue(domain.get(rnd_index));
                    //tNode.setValue(tNode.getOwner());
                    for(Plane p: domain){
                        if(p != agent)
                            agent.send(new TaskMessage(tNode.getTask(),tNode.getValue(),p)); 
                    }
                }
                
                if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+getAgent().getWorld().getTime()+" "+getAgent()+" random dsa grafo:"+dsa_graph);
                
                break;
                
            case ContinueDSA:
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
               
                for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
                    if(rnd.nextDouble() < this.DSA_p){
                        tNode.makeDecision();
                        if(tNode.getLastChangedTime() == agent.getWorld().getTime()){
                            for(Plane p: tNode.getDomain()){
                                if(p != agent){
                                    agent.send(new TaskMessage(tNode.getTask(),tNode.getValue(),p)); 
                                }
                            }
                            
                        }
                    }
                    
                }
                
                current_DSA_iteration++;
                break;
                
            case EndDSA:
                if(ai.univr.dsa.DSAPlane.DEBUG)System.out.println("t="+agent.getWorld().getTime()+" "+agent+" dsa_iter:"+current_DSA_iteration+" fine dsa");
                        
                        
                for(MyPlaneTaskNode tNode : dsa_graph.getMyPlaneTasksNode()){
                    if(tNode.getValue() != tNode.getOwner()){
                        agent.send(new ReallocatedTaskMessage(tNode.getTask(),tNode.getValue()));
                        agent.removeTask(tNode.getTask());
                    }
                }
                break;
                
                
            case Nothing:
                break;
                
                
                
        }

    
    }
    
   
    
    private void initializeNewDSAExec(){
        dsa_graph.clear();
        current_DSA_iteration = 2;
    }


}
