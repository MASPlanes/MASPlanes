/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute 
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.planes.messaging;

import es.csic.iiia.planes.behaviors.Behavior;
import es.csic.iiia.planes.AbstractPositionedElement;
import es.csic.iiia.planes.Location;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Skeletal implementation of a messaging agent.
 * <p/>
 * Agents using this implementation must react to received messages by using
 * {@link Behavior}s.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractMessagingAgent extends AbstractPositionedElement
    implements MessagingAgent
{
    private static final Logger LOG = Logger.getLogger(AbstractMessagingAgent.class.getName());
    
    /**
     * Cache of handling methods by message type.
     */
    private HashMap<Class<? extends Message>, Map<Behavior, Method>> handlers = 
            new HashMap<Class<? extends Message>, Map<Behavior, Method>>();
    
    /**
     * The communication radius.
     */
    private double communicationRange;
    
    /**
     * Messages received in the previous iteration, available at the current
     * time.
     */
    private List<Message> currentMessages;
    
    /**
     * Messages received in this iteration, that will not be available until
     * the next one.
     */
    private List<Message> futureMessages;
    
    /**
     * The list of behaviors of this agent.
     */
    private List<Behavior> behaviors;
    
    public AbstractMessagingAgent(Location location) {
        super(location);
        currentMessages = new ArrayList<Message>();
        futureMessages = new ArrayList<Message>();
        behaviors = new ArrayList<Behavior>();
    }
    
    /**
     * Add a new behavior to the agent.
     * 
     * Every time a message is being received (which always happens during the
     * {@link #step() } execution), any behavior that implements a handler
     * of the specific type of the message will be triggered.
     * 
     * @param behavior to be added.
     */
    protected void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
        handlers.clear();
    }
    
    /**
     * Get the list of behaviors.
     * @return list of behaviors of this agent.
     */
    protected List<Behavior> getBehaviors() {
        return Collections.unmodifiableList(behaviors);
    }

    @Override
    public double getCommunicationRange() {
        return communicationRange;
    }
    
    @Override
    public void setCommunicationRange(double range) {
        this.communicationRange = range;
    }

    @Override
    public void receive(Message message) {
        futureMessages.add(message);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * In this case, the step initialization is to turn the "future" messages
     * of the previous iteration into the "current" ones for this iteration.
     */
    @Override
    public void preStep() {
        List<Message> tmp = currentMessages;
        tmp.clear();
        currentMessages = futureMessages;
        futureMessages = tmp;
    }
    
    /**
     * Call the message handler for each message received in the previous
     * iteration.
     */
    @Override
    public void step() {
        
        for (Behavior b : behaviors) {
            b.beforeMessages();
        }
        
        for (Message m : currentMessages) {
            handle(m);
        }
        
        for (Behavior b : behaviors) {
            b.afterMessages();
        }
        
    }
    
    @Override
    public void send(Message message) {
        message.setSender(this);
        getWorld().sendMessage(message);
    }
    
    
    private void handle(Message m) {
        final Class<? extends Message> mclass = m.getClass();
        
        // Populate the handlers cache if needed.
        if (!handlers.containsKey(mclass)) {
            computeHandlers(mclass);
        }
        
        for (Entry<Behavior,Method> e : handlers.get(mclass).entrySet()) {
                
            try {
                e.getValue().invoke(e.getKey(), m);
            } catch (IllegalAccessException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Throwable throwable = ex.getTargetException();
                if (throwable instanceof Error) { 
                    throw (Error) throwable; 
                } else if(throwable instanceof RuntimeException) { 
                    throw (RuntimeException) throwable; 
                }
                LOG.log(Level.SEVERE, null, throwable);
            }
                
        }
        
    }
    
    private void computeHandlers(Class<? extends Message> mclass) {
        Map<Behavior, Method> mhandlers = new TreeMap<Behavior, Method>();
        
        for (Behavior behavior : behaviors) {
            Method handler = getMethod(behavior.getClass(), mclass);
            if (handler != null) {
                mhandlers.put(behavior, handler);
            }
        }
        
        this.handlers.put(mclass, mhandlers);
    }
    
    private static Method getMethod(Class<? extends Behavior> bclass, 
            Class<? extends Message> mclass) 
    {
        Method m = null;
        try {
            m = bclass.getMethod("on", mclass);
        } catch (NoSuchMethodException ex) {
            Class c = bclass.getSuperclass();
            if (Message.class.isAssignableFrom(c)) {
                m = getMethod(bclass, c);
            }
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return m;
    }
}