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
package es.csic.iiia.planes.behaviors;

import es.csic.iiia.planes.AbstractMessagingAgent;
import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.messaging.Message;
import es.csic.iiia.planes.util.DependencyResolver;
import org.apache.commons.collections.map.MultiKeyMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public abstract class AbstractBehaviorAgent extends AbstractMessagingAgent {
    private static final Logger LOG = Logger.getLogger(AbstractBehaviorAgent.class.getName());

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

    /**
     * Flag to prevent nodes from adding behaviors after being initialized.
     */
    private boolean initialized = false;

    public AbstractBehaviorAgent(Location location) {
        super(location);
        currentMessages = new ArrayList<Message>();
        futureMessages = new ArrayList<Message>();
        behaviors = new ArrayList<Behavior>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        // Compute the behavior ordering from the declared dependencies
        DependencyResolver d = new DependencyResolver();
        for (Behavior v : behaviors) {
            Class[] dependencies = v.getDependencies();
            d.add(v.getClass(), dependencies);
        }

        // Get an ordered list of behavior classes, and construct a new
        // (ordered) list of behavior objects.
        List<Behavior> newBehaviors = new ArrayList<Behavior>(behaviors.size());
        for (Class c : d.getOrderedList()) {
            Behavior b = getBehavior(c);
            b.initialize();
            newBehaviors.add(b);
        }

        behaviors = newBehaviors;
        initialized = true;
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
        if (initialized) {
            throw new UnsupportedOperationException("You can only add behaviors to an agent inside its constructor, not here.");
        }
        behaviors.add(behavior);
    }

    /**
     * Get the list of behaviors.
     * @return list of behaviors of this agent.
     */
    protected List<Behavior> getBehaviors() {
        return Collections.unmodifiableList(behaviors);
    }

    /**
     * Get the behavior that implements a specific class.
     *
     * @return behavior that implements the given class.
     */
    @SuppressWarnings("unchecked")
    public <T extends Behavior> T getBehavior(Class<T> behaviorClass) {
        for (Behavior b : behaviors) {
            if (b.getClass() == behaviorClass) {
                return (T)b;
            }
        }
        return null;
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
     * <p/>
     * Additionally, it gives the opportunity for behaviors to initialize
     * themselves.
     */
    @Override
    public void preStep() {
        List<Message> tmp = currentMessages;
        tmp.clear();
        currentMessages = futureMessages;
        futureMessages = tmp;

        for (Behavior b : behaviors) {
            b.preStep();
        }
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

        dispatchMessages();

        for (Behavior b : behaviors) {
            b.afterMessages();
        }

    }

    /**
     * {@inheritDoc}
     * <p/>
     * In this case, it gives the opportunity for behaviors to finalize the
     * current iteration.
     */
    @Override
    public void postStep() {
        for (Behavior b : behaviors) {
            b.postStep();
        }
    }

    @Override
    public void send(Message message) {
        message.setSender(this);
        getWorld().sendMessage(message);
    }

    private void dispatchMessages() {
        LOG.log(Level.FINER, "{0} dispatching {1} messages.",
                new Object[]{this, currentMessages.size()});

        for (Behavior b : behaviors) {
            for (Message m : currentMessages) {
                handle(b, m);
            }
        }
    }

    private MultiKeyMap cache = new MultiKeyMap();

    private void handle(Behavior b, Message m) {
        final Class<? extends Behavior> bClass = b.getClass();
        final Class<? extends Message> mClass = m.getClass();

        // Memoize the method
        Method method;
        if (cache.containsKey(bClass, mClass)) {
            method = (Method)cache.get(bClass, mClass);
        } else {
            method = getMethod(bClass, mClass);
            if (method != null) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Dispatching {0} to {1}",
                            new Object[]{mClass.getSimpleName(), method.toGenericString()});
                }
            }
            cache.put(bClass, mClass, method);
        }

        if (method == null) {
            return;
        }

        // Invoke it
        try {
            method.invoke(b, m);
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Error invoking " + method.toGenericString()
                    + " on " + b + " with argument " + m, ex);
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

    @SuppressWarnings("unchecked")
    private static Method getMethod(Class<? extends Behavior> bClass,
            Class<? extends Message> mClass)
    {
        Method m = null;
        try {
            m = bClass.getMethod("on", mClass);
        } catch (NoSuchMethodException ex) {
            Class c = mClass.getSuperclass();
            if (Message.class.isAssignableFrom(c)) {
                m = getMethod(bClass, (Class<? extends Message>)c);
            }
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return m;
    }

}