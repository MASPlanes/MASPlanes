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

/**
 * Implements the DSAWorkload evaluation function.
 * 
 * @author Andrea Jeradi, Francesco Donato
 */
public class DSAWorkload implements EvaluationFunction{
    /**
     * K value used for the workload function.
     */
    private final double k;
    /**
     * Alpha value used for the workload function.
     */
    private final double alpha;
    
    /**
     * Build a DSAWorkload function.
     * @param k value of the function.
     * @param alpha value of the function.
     */
    public DSAWorkload(double k, double alpha) {
        this.k = k;
        this.alpha = alpha;
    }
    
    /**
     * Gets the k value of DSAWorkload evaluation function.
     * 
     * @return double represents the k value. 
     */
    public double getK() {
        return k;
    }
    
    /**
     * Gets the alpha value of DSAWorkload evaluation function.
     * 
     * @return double represents the alpha value. 
     */
    public double getAlpha() {
        return alpha;
    }
    
    @Override
    public String getName() {
        return "workload";
    }
    
    /**
     * Gets the workload results.
     * 
     * @param nTasks number of tasks that has the plane.
     * @return double represents the workload.
     */
    public double getWorkload(int nTasks){
        return k * Math.pow(nTasks, alpha);
    }
    
}
