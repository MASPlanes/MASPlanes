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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Plane extends AbstractDrawable implements Agent {
    
    final private static AtomicInteger idGenerator = new AtomicInteger();
    final int id = idGenerator.incrementAndGet();
    
    // Speed in meters per second
    private double speed = 50/3.6d;

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    private List<Task> tasks = null;
    private Task nextTask = null;
    
    public Plane(Location location) {
        super(location);
        tasks = new ArrayList<Task>();
    }
    
    @Override
    public void step() {
        if (nextTask != null) {
            if (location.move(nextTask.getLocation(), speed)) {
                taskCompleted(nextTask);
            }
            updateBattery();
        }
    }
    
    private void taskCompleted(Task t) {
        tasks.remove(t);
        getWorld().removeTask(t);
        nextTask = getNearestTask();
    }
    
    private Task getNearestTask() {
        Task nearestTask = null;
        double mind = Double.MAX_VALUE;
        
        for(Task t : tasks) {
            double d = location.getDistance(t.getLocation());
            if (d < mind) {
                nearestTask = t;
                mind = d;
            }
        }
        
        return nearestTask;
    }

    void addTask(Task task) {
        tasks.add(task);
        nextTask = getNearestTask();
    }

    
    final private static Stroke ownedLines =
            new BasicStroke(1.0f,   // Width
            BasicStroke.CAP_SQUARE, // End cap
            BasicStroke.JOIN_MITER, // Join style
            10.0f,                  // Miter limit
            new float[]{16.0f, 20.0f}, // Dash pattern
            0.0f);                     // Dash phase
    
    @Override
    public void draw(Graphics2D g) {
        int x = location.getXInt();
        int y = location.getYInt();
        Color previous = g.getColor();
        
        // Line to destination (if exists)
        if (nextTask != null) {
            g.setColor(Color.RED);
            
            final Location l = nextTask.getLocation();
            g.drawLine(x, y, l.getXInt(), l.getYInt());
        }
        // Line to other assigned tasks
        Stroke previousStroke = g.getStroke();
        g.setStroke(ownedLines);
        for (Task t : tasks) {
            if (t == nextTask) continue;
            
            final Location l = t.getLocation();
            g.drawLine(x, y, l.getXInt(), l.getYInt());
        }
        g.setStroke(previousStroke);
        
        g.setColor(Color.RED);
        g.fillOval(x-200, y-200, 400, 400);
        
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 160);
        String sid = String.valueOf(id);
        g.setFont(f);
        FontMetrics m = g.getFontMetrics(f);
        int w = m.stringWidth(sid);
        int h = m.getHeight()-2;
        g.setColor(Color.WHITE);
        g.drawString(sid, x-(w/2), y+(h/2));
        
        g.setColor(previous);
    }

    
    private long battery;
    
    public void setBattery(long battery) {
        this.battery = battery;
    }
    public long getBattery() {
        return battery;
    }

    private void updateBattery() {
        battery--;
    }
    
}
