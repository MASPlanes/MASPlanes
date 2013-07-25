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
package es.csic.iiia.planes;

import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DStation;
import es.csic.iiia.planes.messaging.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base implementation of a World, leaving some details to be implemented by the
 * more specific types.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public abstract class AbstractWorld implements World {
    private static final Logger LOG = Logger.getLogger(AbstractWorld.class.getName());

    private Space space = null;
    private List<Agent> agents = new ArrayList<Agent>();
    private List<Plane> planes = new ArrayList<Plane>();
    private List<Task> tasks = new ArrayList<Task>();
    private List<Station> stations = new ArrayList<Station>();

    /**
     * Statistics collector.
     */
    private StatsCollector stats = new StatsCollector(this);

    /**
     * Operators in charge of supplying tasks to the UAVs.
     */
    private ArrayList<Operator> operators = new ArrayList<Operator>();

    /**
     * Current simulation time.
     */
    private long time = 0;

    /**
     * Duration of this simulation in tenths of second.
     */
    protected long duration;

    /**
     * Factory used to create elements for this simulation.
     */
    private final Factory factory;

    /**
     * Builds a new world.
     *
     * @param f factory used to create elements for this simulation.
     */
    public AbstractWorld(Factory f) {
        this.factory = f;
    }

    @Override
    public Factory getFactory() {
        return factory;
    }

    /**
     * Get the list of operators.
     * @return list of operators.
     */
    @Override
    public List<Operator> getOperators() {
        return operators;
    }

    /**
     * Add an operator to the simulation.
     *
     * @param operator operator to add.
     */
    public void addOperator(Operator operator) {
        operators.add(operator);
        agents.add(operator);
    }

    @Override
    public void addStation(Station station) {
        stations.add(station);
    }

    /**
     * Get the list of charging stations.
     * @return list of charging stations.
     */
    protected List<Station> getStations() {
        return stations;
    }

    @Override
    public void init(DProblem d) {
        space = new Space(d.getWidth(), d.getHeight());
        setDuration(d.getDuration());

        for (DOperator o : d.getOperators()) {
            Location l = new Location(o.getX(), o.getY());
            Operator operator = factory.buildOperator(l, o.getTasks());
            operator.setCommunicationRange(o.getCommunicationRange());
            addOperator(operator);
        }

        for (DPlane pd : d.getPlanes()) {
            Location l = new Location(pd.getX(), pd.getY());
            Plane p = factory.buildPlane(l);
            Battery b = factory.buildBattery(p);
            p.setSpeed(pd.getSpeed());
            b.setEnergy(pd.getInitialBattery());
            b.setCapacity(pd.getBatteryCapacity());
            p.setCommunicationRange(pd.getCommunicationRange());
            p.setColor(pd.getColor());
        }

        for (DStation sd : d.getStations()) {
            Location l = new Location(sd.getX(), sd.getY());
            Station s = factory.buildStation(l);
        }
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Space getSpace() {
        return space;
    }

    @Override
    public void run() {

        for (Agent a : agents) {
            a.initialize();
        }

        for (time=0; time<duration || tasks.size() > 0; time++) {
            LOG.fine("----------     TICK     ----------");
            computeStep();
            displayStep();

            // TODO: Replace this maximum duration factor by something that detects if tasks are
            // being completed or not.
            if (time > duration*10) {
                System.err.println("It looks like some tasks will never be completed: ");
                for (Task t : tasks) {
                    System.err.println("\t" + t);
                }
                break;
            }

        }

        for (Plane p : planes) {
            stats.collect(p);
        }
        stats.display(); 
    }

    /**
     * Computes a single simulation step (tenths of second).
     *
     * This should give all of the simulation's actors the opportunity to
     * perform actions, by calling their {@link Agent#step()} methods.
     */
    protected void computeStep() {

        for (Agent a : agents) {
            a.preStep();
        }
        for (Agent a : agents) {
            a.step();
        }
        for (Agent a : agents) {
            a.postStep();
        }
    }

    /**
     * Displays the progress of the simulation.
     *
     * This method must be implemented by the specific world, and should
     * somehow show the simulation's progress to the user.
     */
    protected abstract void displayStep();

    @Override
    public void addPlane(Plane p) {
        planes.add(p);
        agents.add(p);
    }

    @Override
    public List<Plane> getPlanes() {
        return planes;
    }

    /**
     * Get the list of tasks.
     *
     * @return list of pending tasks in this world.
     */
    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public void addTask(Task task) {
        if (tasks.contains(task)) {
            throw new RuntimeException("This task already exists!");
        }
        tasks.add(task);
    }

    @Override
    public void removeTask(Task t) {
        // Check if it has been removed before tracking the stats. Sometimes two
        // planes may think that they complete a pending task, whereas in
        // reality another plane has already completed it before (split brain).
        while (tasks.remove(t)) {
            stats.collect(t);
        }
    }

    @Override
    public Station getNearestStation(Location location) {
        double mind = Double.MAX_VALUE;
        Station best = null;
        for (Station s : stations) {
            final double d = location.getDistance(s.getLocation());
            if (d < mind) {
                best = s;
                mind = d;
            }
        }
        return best;
    }

    @Override
    public Operator getNearestOperator(Location location) {
        double mind = Double.MAX_VALUE;
        Operator best = null;
        for (Operator o : operators) {
            final double d = location.getDistance(o.getLocation());
            if (d < mind) {
                best = o;
                mind = d;
            }
        }
        return best;
    }

    @Override
    public void sendMessage(Message message) {
        final Location origin = message.getSender().getLocation();
        final double range = message.getSender().getCommunicationRange();

        for (Plane p : planes) {
            if (  origin.distance(p.getLocation()) <= range
               && (p == message.getRecipient() || message.getRecipient() == null))
            {
                p.receive(message);
            }
        }
    }

}