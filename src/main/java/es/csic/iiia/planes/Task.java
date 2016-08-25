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

import es.csic.iiia.planes.gui.Drawable;
import es.csic.iiia.planes.gui.graphics.TaskGraphic;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a location that must be checked by some plane.
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class Task extends AbstractPositionedElement implements Drawable, Comparable {

    /**
     * Generator of unique identifiers.
     */
    private final static AtomicInteger idGenerator = new AtomicInteger();

    /**
     * Identifier of this task.
     */
    private final int id = idGenerator.incrementAndGet();

    /**
     * Time at which this task has been submitted.
     */
    private long submissionTime;

    /**
     * Time at which this task expires.
     */
    private long expireTime;

    public long getExpireTime() { return expireTime; }

    /**
     * Status of survivor.
     */
    private boolean alive;

    public boolean isAlive() { return alive; }

    public void expire() { this.alive = false; }

    /**
     * Builds a new task.
     *
     * @param location that must be checked by some plane.
     */
    public Task(Location location) {
        super(location);
    }

    @Override
    public void initialize() {
        submissionTime = getWorld().getTime();
        //TODO: Make this dependent on settings
        expireTime = (long)(Math.random()*(2592000-360000))+360000;
        alive = true;
    }

    /**
     * Get the time at which this task was submitted by the operator (created).
     * @return submission time.
     */
    public long getSubmissionTime() {
        return submissionTime;
    }

    /**
     * Get the identifier of this task.
     *
     * Identifiers are sequential (in order of submission) and guaranteed to be
     * unique.
     *
     * @return the identifier of this Task.
     */
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
//        buf.append("Task[").append(id).append("](").append(getLocation().getX())
//                .append(",").append(getLocation().getY()).append(")");
        buf.append("Task[").append(id).append("]");
        return buf.toString();
    }


    private static TaskGraphic tg = new TaskGraphic();
    @Override
    public void draw(Graphics2D g) {
        int x = getLocation().getXInt();
        int y = getLocation().getYInt();

        Color previous = g.getColor();
        g.setColor(Color.BLUE);
        /*int dim = (int)(30 * getWorld().getSpace().getWidth() / 10000f);
        g.fillOval(x-dim/2, y-dim/2, dim, dim);*/

        int dim = (int)(200 * getWorld().getSpace().getWidth() / 10000f);
        tg.setDimension(new Dimension(dim,dim));
        tg.paint(g, x-dim/2, y-dim/2);

        /*
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 8);
        String sid = String.valueOf(id);
        g.setFont(f);
        FontMetrics m = g.getFontMetrics(f);
        int w = m.stringWidth(sid);
        int h = m.getHeight()-2;
        g.setColor(Color.WHITE);
        g.drawString(sid, x-(w/2), y+(h/2));*/
        g.setColor(previous);
    }

    @Override
    public int compareTo(Object t) {
        if (!(t instanceof Task)) {
            throw new ClassCastException();
        }

        final Task other = (Task)t;
        return id - other.id;
    }

}