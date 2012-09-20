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

import es.csic.iiia.planes.definition.DPlane;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DStation;
import es.csic.iiia.planes.util.FrameTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class World implements Runnable {
    private Space space = null;
    private List<Plane> planes = new ArrayList<Plane>();
    private List<Task> tasks = new ArrayList<Task>();
    private List<Station> stations = new ArrayList<Station>();
    private StatsCollector stats = new StatsCollector(this);
    private Operator operator;
    
    private long time = 0;
    private long duration;
    
    private Display display;
    
    private FrameTracker ftracker = new FrameTracker("world");
    private final Factory factory;

    public Factory getFactory() {
        return factory;
    }
    
    public World(Factory f) {
        this.factory = f;
    }
    
    public void init(DProblem d) {
        this.reset();
        space = new Space(d.getWidth(), d.getHeight());
        duration = d.getDuration();
        
        operator = factory.buildOperator(d.getTasks());
        
        for (DPlane pd : d.getPlanes()) {
            Location l = new Location(pd.getX(), pd.getY());
            Plane p = factory.buildPlane(l);
            p.setSpeed(pd.getSpeed());
            p.setBattery(pd.getBattery());
            p.setMaxBattery(pd.getBattery());
            planes.add(p);
        }
        
        for (DStation sd : d.getStations()) {
            Location l = new Location(sd.getX(), sd.getY());
            Station s = factory.buildStation(l);
            stations.add(s);
        }
    }
    
    public long getTime() {
        return time;
    }
    
    public Space getSpace() {
        return space;
    }
    
    @Override
    public void run() {

        for (time=0; time<duration || tasks.size() > 0; time++) {
            
            computeStep();
            displayStep();
            
        }
        
        stats.display();
    }
    
    public void computeStep() {
        for (Plane p : planes) {
            p.step();
        }
        operator.step();
    }
    
    
    public void displayStep() {
        if (display == null) return;
        
        //if (time % 10 != 0) return;
        
        BufferStrategy strategy = display.getBufferStrategy();
        
        do {
            do {
                Graphics2D surface = (Graphics2D)(strategy.getDrawGraphics());
                surface.setColor(Color.WHITE);
                surface.fillRect(0, 0, display.getWidth(), display.getHeight());
                surface.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                surface.setColor(Color.BLACK);
                surface.drawString("Time: " + time, 100, 100);

                
                Dimension wd = space.getDimension();
                Dimension dd = display.getSize();
                AffineTransform tr = new AffineTransform();
                tr.scale(dd.width/wd.getWidth(), dd.height/wd.getHeight());
                surface.setTransform(tr);
                
                for (Plane p : planes) {
                    p.draw(surface);
                }
                for (Task t : tasks) {
                    t.draw(surface);
                }
                for (Station s : stations) {
                    s.draw(surface);
                }
                
                surface.dispose();
                
            } while (strategy.contentsRestored());
            
            strategy.show();
            
        } while (strategy.contentsLost());
    }
    
    public List<Plane> getPlanes() {
        return planes;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }
    
    public void reset() {
        planes.clear();
        tasks.clear();
    }
    
    public void addTask(Task task) {
        tasks.add(task);
    }

    public void setDisplay(Display d) {
        display = d;
    }

    public void removeTask(Task t) {
        tasks.remove(t);
        stats.collect(t);
    }

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
    
}
