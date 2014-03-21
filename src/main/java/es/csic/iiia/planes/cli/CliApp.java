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
package es.csic.iiia.planes.cli;

import es.csic.iiia.planes.DefaultFactory;
import es.csic.iiia.planes.Factory;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.gui.GUIFactory;

/**
 * Actual object in charge of executing the simulation.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class CliApp {

    private final Configuration config;

    /**
     * Default constructor.
     *
     * @param configuration with which the simulator will run.
     */
    public CliApp(Configuration configuration) {
        this.config = configuration;
    }

    /**
     * Runs a simulation.
     * <p/>
     * Specifically, CliApp executes the following steps:
     * <ol>
     *   <li>Create either a {@link DefaultFactory} (no gui option set) or a
     *       {@link GUIFactory} (gui option set).</li>
     *   <li>Create a new {@link World} using that factory.</li>
     *   <li>Initialize the world by calling
     *       {@link World#init(es.csic.iiia.planes.definition.DProblem) }</li>
     *   <li>Launching the world in its own thread</li>
     * </ol>
     */
    public void run() {
        Factory f;
        if (config.isGui()) {
            f = new GUIFactory(config);
        } else {
            f = new DefaultFactory(config);
        }

        World world = f.buildWorld();

        world.init(config.getProblemDefinition());
        new Thread(world).start();
    }

}