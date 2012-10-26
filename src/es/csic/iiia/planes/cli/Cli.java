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

import es.csic.iiia.planes.Configuration;
import es.csic.iiia.planes.DefaultPlane;
import es.csic.iiia.planes.auctions.AuctionPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.operator_behavior.Nearest;
import es.csic.iiia.planes.operator_behavior.Random;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.cli.*;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Main class for the CLI interface.
 * 
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Cli {
    
    /**
     * List of available cli options.
     */
    private static Options options = new Options();

    /**
     * Cli's entry point.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        initializeLogging();
        
        options.addOption("g", "gui", false, "graphically display the simulation.");
        options.addOption("h", "help", false, "show this help message.");
        options.addOption(OptionBuilder.withArgName("operator")
                .hasArg()
                .withDescription("set the strategy used by the operator to submit tasks to UAVs.")
                .withArgName("random|nearest")
                .withLongOpt("operator")
                .create('o'));
        options.addOption(OptionBuilder.withArgName("planestype")
                .hasArg()
                .withDescription("set the type of planes in this simulation.")
                .withArgName("default|auction")
                .withLongOpt("planes-type")
                .create('p'));
        
        Configuration config = parseOptions(args);
        CliApp app = new CliApp(config);
        app.run();
    }
    
    private static void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("planes [OPTIONS] <PROBLEM>", options);
        System.exit(1);
    }
    
    /**
     * Parse the provided list of arguments according to the program's options.
     * 
     * @param in_args list of input arguments.
     * @return a configuration object set according to the input options.
     */
    private static Configuration parseOptions(String[] in_args) {
        Configuration config = new Configuration();
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, in_args);
        } catch (ParseException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        if (line.hasOption('g')) {
            config.gui = true;
        }
        if (line.hasOption('h')) {
            showHelp();
        }
        if (line.hasOption('o')) {
            String value = line.getOptionValue('o');
            if (value.equalsIgnoreCase("nearest")) {
                config.operatorStrategy = new Nearest();
            } else if (value.equalsIgnoreCase("random")) {
                config.operatorStrategy = new Random();
            } else {
                throw new IllegalArgumentException("Illegal operator strategy \"" + value + "\".");
            }
        }
        if (line.hasOption('p')) {
            String value = line.getOptionValue('p');
            if (value.equalsIgnoreCase("auction")) {
                config.planesClass = AuctionPlane.class;
            } else if (value.equalsIgnoreCase("default")) {
                config.planesClass = DefaultPlane.class;
            } else {
                throw new IllegalArgumentException("Illegal plane strategy \"" + value + "\".");
            }
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
            System.err.println("Error reading file: " + ex.getLocalizedMessage());
            System.exit(1);
        }
        config.problemDefinition = d;
        
        return config;
    }

    /**
     * Initializes the logging system.
     */
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
