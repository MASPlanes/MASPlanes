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
package es.csic.iiia.planes.maxsum.centralized;

import es.csic.iiia.bms.Factor;
import es.csic.iiia.bms.factors.CardinalityFactor;
import es.csic.iiia.bms.factors.WeightingFactor;
import es.csic.iiia.bms.factors.CardinalityFactor.CardinalityFunction;

/**
 * Maxsum workload factor with integrated independent costs (\beta in the paper)
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class WorkloadFactor<T> extends WeightingFactor<T> implements CostFactor<T> {

    public WorkloadFactor() {
        super(new CardinalityFactor<T>());
    }

    @Override
    public CardinalityFactor<T> getInnerFactor() {
        return (CardinalityFactor<T>)super.getInnerFactor();
    }

    public void setFunction(CardinalityFunction function) {
        getInnerFactor().setFunction(function);
    }

    @Override
    public String toString() {
        // The centralized solver uses the object itself as identity, so we need
        // this check to avoid an infinite recursion.
        final T identity = getIdentity();
        if (identity == this) {
            return super.toString();
        }

        return "Workload" + getIdentity();
    }

}
