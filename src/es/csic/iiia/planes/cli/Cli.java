/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2012, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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

import es.csic.iiia.planes.Display;
import es.csic.iiia.planes.World;
import es.csic.iiia.planes.generator.Generator;
import es.csic.iiia.planes.io.DProblem;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Cli {
    
    private static Options options = new Options();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        initializeLogging();
        
        options.addOption("g", "gui", false, "graphically display the simulation." );
        options.addOption("h", "help", false, "show this help message.");
        options.addOption("s", "seed", true, "set the random seed for this simulation (simulation number)." );
        
        CliApp app = new CliApp();
        parseOptions(args, app);
        app.run();
    }
    
    private static void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("planes [OPTIONS] <PROBLEM>", options);
        System.exit(1);
    }
    
    private static void parseOptions(String[] in_args, CliApp app) {
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, in_args);
        } catch (ParseException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        if (line.hasOption('g')) {
            app.gui = true;
        }
        if (line.hasOption('h')) {
            showHelp();
        }
        if (line.hasOption('s')) {
            app.seed = Long.valueOf(line.getOptionValue('s'));
        }

        String[] args = line.getArgs();
        if (args.length < 1) {
            showHelp();
        }
        
        DProblem d = new DProblem();
        ObjectMapper mapper = new ObjectMapper();
        try {
            d = mapper.readValue(new File(args[0]), DProblem.class);
        } catch (IOException ex) {
            Logger.getLogger(Generator.class.getName()).log(Level.SEVERE, null, ex);
        }
        app.problemDefinition = d;
    }

    private static void initializeLogging() {
        try {
            // Load logging configuration
            LogManager.getLogManager().readConfiguration(
                    Cli.class.getResourceAsStream("/logging.properties"));
        } catch (IOException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
