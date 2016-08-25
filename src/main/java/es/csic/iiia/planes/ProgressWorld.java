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
package es.csic.iiia.planes;

import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.util.FrameTracker;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default implementation of a World, to be used when running on a command
 * line (interactive) interface.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ProgressWorld extends AbstractWorld {

    /**
     * Queue used to hold percentages of completion until they are ready
     * to be displayed.
     */
    private ConcurrentLinkedQueue<Double> progressQueue =
            new ConcurrentLinkedQueue<Double>();

    private ConcurrentLinkedQueue<Integer> progressQueue2 =
            new ConcurrentLinkedQueue<Integer>();

    /**
     * Runnable that will keep printing the progress
     */
    private ShowProgress progress = new ShowProgress();

    private final FrameTracker ftracker = new FrameTracker(24);

    /**
     * Builds a new world, whose elements will be created by the given factory.
     *
     * @param factory
     */
    public ProgressWorld(Factory factory) {
        super(factory);
    }

    @Override
    public void init(DProblem d) {
        super.init(d);

        new Thread(progress).start();
    }

    @Override
    public void run() {
        try {
            super.run();
        } catch (Exception e) {
            System.err.println();
            e.printStackTrace();
        } finally {
            progressQueue.clear();
            progressQueue2.clear();
            progress.stop();
        }
    }

    /**
     * Shows the simulation progress.
     * <p/>
     * In this case, a percentage of completion is updated in stderr unless
     * quiet mode is specified in the command line.
     */
    @Override
    public void displayStep() {

        if (progressQueue.isEmpty()) {
            final Double percent = getTime()*100 / (double)duration;
            progressQueue.add(percent);
        }
        /**
         * @author Guillermo Bautista
         * Secondary queue added to track number of survivors that remain
         * alive in the simulation world.
         */
        if (progressQueue2.isEmpty()) {
            final Integer survivorsRemaining = getTasks().size();
            progressQueue2.add(survivorsRemaining);
        }

    }

    private class ShowProgress implements Runnable {

        private boolean stop = false;

        private synchronized void stop() {
            this.stop = true;
        }

        private synchronized boolean isStop() {
            return stop;
        }

        @Override
        public void run() {
            while (!isStop()) {
                final Double percent = progressQueue.poll();
                /**
                 * @author Guillermo Bautista
                 * Also prints out number of survivors still alive in the
                 * simulation, and whether all the blocks have been
                 * assigned to be searched by an agent.
                 */
                final Integer survivors = progressQueue2.poll();
                if (percent != null && survivors != null) {
                    System.err.print(String.format("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" +
                            "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" +
                            "Completed: %6.2f%%\tAll blocks assigned: %b" +
                            "\tRemaining Survivors: %d   ", percent, getUnassignedBlocks().isEmpty(), survivors));
                }
                ftracker.delay();
            }
        }

    }

}