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

import es.csic.iiia.planes.messaging.Message;
import es.csic.iiia.planes.MessagingAgent;

/**
 * Defines some behavior for {@link MessagingAgent}s, by implementing arbitrary
 * reactions to specific {@link Message}s.
 * <p/>
 * Unfortunately, java is very stupid when trying to implement a messaging
 * system. Therefore, we use reflection here and there is no static checking
 * of any type.
 * <p/>
 * In order to define the reaction to a specific type of event, a class
 * implementing this interface must implement an method<br/>
 * <code>on(MessageType message)</code><br/>
 * for each type of message that it reacts to.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface Behavior<T extends MessagingAgent> {

    /**
     * Get the agent that exhibits this behavior.
     *
     * @return agent that exhibits this behavior.
     */
    public T getAgent();

    /**
     * Initialize this behavior.
     */
    public void initialize();

    /**
     * Get the dependencies of this behavior.
     *
     * @return list of the classes on which this behavior depends.
     */
    public Class[] getDependencies();

    /**
     * Implements actions to be performed by this behavior *before* any other
     * agent has started processing messages.
     */
    public void preStep();

    /**
     * Implements actions to be performed by this behavior *before* this agent
     * has processed any messages from the current iteration.
     */
    public void beforeMessages();

    /**
     * Implements action to be performed *after* this agent has processed all
     * the messages from the current iteration.
     */
    public void afterMessages();

    /**
     * Implements actions to be performed by this behavior *after* all other
     * agents have finished processing messages.
     */
    public void postStep();

}