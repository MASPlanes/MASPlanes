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
package es.csic.iiia.planes.gui;

import es.csic.iiia.planes.Location;
import es.csic.iiia.planes.Plane;
import es.csic.iiia.planes.Task;
import es.csic.iiia.planes.gui.graphics.PlaneGraphic;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.text.MessageFormat;
import java.util.List;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class PlaneDrawer implements Drawable {

    private Plane plane;

    /**
     * Stroke used to paint stuff
     */
    private static final Stroke normalStroke = new BasicStroke(10f);

    public PlaneDrawer(Plane p) {
        this.plane = p;
    }

    @Override
    public void draw(Graphics2D g) {
        GUIWorld w = (GUIWorld)plane.getWorld();

        Plane selectedPlane = w.getSelectedPlane();
        if (selectedPlane == plane) {
            drawSelected(g);
        } else if (selectedPlane == null) {
            drawNormal(g);
        } else {
            drawUnselected(g);
        }
    }

    @Override
    public Location getLocation() {
        return plane.getLocation();
    }

    @Override
    public void setLocation(Location l) {
        plane.setLocation(l);
    }

    private void drawFutureLocations(Graphics2D g) {
        List<Location> plannedLocations = plane.getPlannedLocations();
        if (plannedLocations == null || plannedLocations.isEmpty()) {
            return;
        }

        /*** Future locations */
        GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, plannedLocations.size()+1);
        p.moveTo(getLocation().getXInt(), getLocation().getYInt());

        // Add all the tasks to a list
        for (Location nextLocation : plannedLocations) {
            p.lineTo(nextLocation.getX(), nextLocation.getY());
        }

        g.setStroke(normalStroke);
        g.setColor(plane.getColor());
        g.draw(p);
    }

    private void drawPastLocations(Graphics2D g) {
        List<Location> completedLocations = plane.getCompletedLocations();
        if (completedLocations == null || completedLocations.isEmpty()) {
            return;
        }

        /*** Past locations */
        GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD, completedLocations.size());
        boolean first = true;
        for (Location l : completedLocations) {
            if (first) {
                p.moveTo(l.getX(), l.getY());
                first = false;
            } else {
                p.lineTo(l.getX(), l.getY());
            }
        }
        p.lineTo(plane.getLocation().getX(), plane.getLocation().getY());
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(10f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, new float[]{100f,100f}, 0.0f));
        g.draw(p);
        g.setStroke(normalStroke);
    }

    private void drawSelected(Graphics2D g) {
        Color previous = g.getColor();

        // Line to destination (if exists)
        final int x = getLocation().getXInt();
        final int y = getLocation().getYInt();

        drawFutureLocations(g);
        drawPastLocations(g);

        drawTasks(g, plane.getColor());
        drawPlane(g, Color.DARK_GRAY, plane.getColor());
        drawBattery(g);

        g.setColor(previous);
    }

    private void drawUnselected(Graphics2D g) {
        Color previous = g.getColor();

        // Line to destination (if exists)
        int x = getLocation().getXInt();
        int y = getLocation().getYInt();
        Task nextTask = plane.getNextTask();
        if (nextTask != null) {
            g.setColor(Color.LIGHT_GRAY);
            final Location l = nextTask.getLocation();
            g.drawLine(x, y, l.getXInt(), l.getYInt());
        }

        drawTasks(g, Color.LIGHT_GRAY);
        drawPlane(g, Color.DARK_GRAY, Color.LIGHT_GRAY);
        drawBattery(g);

        g.setColor(previous);

    }

    private void drawTasks(Graphics2D g, Color c) {
        g.setColor(c);
        for (Task t : plane.getTasks()) {
            final Location l = t.getLocation();
            g.fillOval(l.getXInt()-50, l.getYInt()-50, 100, 100);
        }
    }

    private void drawPlane(Graphics2D g, Color lineColor, Color planeColor) {
        int x = getLocation().getXInt();
        int y = getLocation().getYInt();

        AffineTransform oldt = g.getTransform();
        AffineTransform newt = new AffineTransform(oldt);
        newt.translate(x-175, y-175);
        //g.setColor(Color.RED);
        //g.draw(PlaneGraphic.getImage(400, 400));
        newt.rotate(plane.getAngle(), 175, 175);
        newt.rotate(3*Math.PI/2., 175, 175);
        newt.scale(350,350);
        g.setTransform(newt);
        g.setColor(lineColor);
        Stroke olds = g.getStroke();
        Stroke news = new BasicStroke(.15f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
        g.setStroke(news);
        g.draw(PlaneGraphic.getImage());
        g.setColor(planeColor);
        g.fill(PlaneGraphic.getImage());

        // Draw the communication circle
        g.setTransform(oldt);
        final int r = (int)plane.getCommunicationRange();
        g.drawOval(x-r, y-r, r*2, r*2);

        g.setStroke(olds);
    }

    private void drawBattery(Graphics2D g) {
        int x = getLocation().getXInt();
        int y = getLocation().getYInt();

        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 160);
        g.setFont(f);
        String sid = String.valueOf(plane.getId());
        FontMetrics m = g.getFontMetrics(f);
        int w = m.stringWidth(sid);
        int h = m.getHeight();
        double percent = plane.getBattery().getEnergy()/(double)plane.getBattery().getCapacity();
        String bat =  MessageFormat.format("{0,number,#.##%}", percent);
        g.setColor(new Color(240,240,240));
        g.fillRect(x-(w/2), y+200, (int)(w*percent), h);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x-(w/2), y+200, w, h);
        g.drawString(bat, x-(w/2), y+200+h-25);
    }

    private void drawNormal(Graphics2D g) {
        Color previous = g.getColor();

        // Line to destination (if exists)
        int x = getLocation().getXInt();
        int y = getLocation().getYInt();
        Task nextTask = plane.getNextTask();
        if (nextTask != null) {
            g.setColor(Color.RED);
            final Location l = nextTask.getLocation();
            g.drawLine(x, y, l.getXInt(), l.getYInt());
        }

        drawTasks(g, plane.getColor());
        drawPlane(g, Color.DARK_GRAY, plane.getColor());
        drawBattery(g);
        drawFutureLocations(g);

        g.setColor(previous);
    }

}