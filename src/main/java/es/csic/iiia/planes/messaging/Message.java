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

import es.csic.iiia.planes.MessagingAgent;

/**
 * Base type for any messages exchanged by {@link MessagingAgent}s.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface Message {

    /**
     * Get the sender of this message.
     * @return sender of this message.
     */
    public MessagingAgent getSender();

    /**
     * Set the sender of this message.
     * @param sender of this message.
     */
    public void setSender(MessagingAgent sender);

    /**
     * Get the intended recipient of this message.
     * <p/>
     * A broadcast message should have an intended recipient of <em>null</em>.
     * The intended recipient is just informational: any agent within the
     * sender's {@link MessagingAgent#getCommunicationRange()} will receive it.
     *
     * @return intented recipient of this message.
     */
    public MessagingAgent getRecipient();

    /**
     * Set the intended recipient of this message.
     * @see #getRecipient()
     * @param recipient of this message.
     */
    public void setRecipient(MessagingAgent recipient);

}