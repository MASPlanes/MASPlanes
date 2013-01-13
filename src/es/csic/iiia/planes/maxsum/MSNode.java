/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.planes.maxsum;

import java.util.Set;

/**
 * A node (function) in the max-sum graph.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface MSNode<Domain extends Object, IncomingMessage extends MSMessage> {

    /**
     * Get the plane where this (logical) node is running.
     * 
     * @return plane where this node is running.
     */
    MSPlane getPlane();

    /**
     * Get the set of possible domain values (objects) of this node.
     * <p/>
     * This is akin to the variables involved in this node.
     * 
     * @return the domain of this node.
     */
    public Set<Domain> getDomain();

    /**
     * Get the potential (cost) of activating the given domain value (variable).
     * 
     * @param domainValue variable that would activate.
     * @return cost incurred when activating the given domain value.
     * @TODO this is strange and should be changed. The concept of potential
     * given a single domain value is wrong. The potential is a function that
     * receives assignments (true/false) for each of the binary variables
     * involved in the node and returns a cost associated to them.
     */
    public double getPotential(Domain domainValue);

    /**
     * Receive an incoming message.
     * 
     * @param message message to receive.
     */
    public void receive(IncomingMessage message);

    /**
     * Perform a single iteration of the max-sum algorithm.
     */
    public void iter();
    
    /**
     * Chose the best domain value according to the node's belief.
     * 
     * @return best domain value according to the node's belief.
     * @TODO this is strange and should be changed. It should be possible to
     * activate multiple domain values at this point.
     */
    public Domain makeDecision();

}