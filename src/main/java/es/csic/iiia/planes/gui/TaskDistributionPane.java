/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
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

import es.csic.iiia.planes.definition.DOperator;
import es.csic.iiia.planes.definition.DProblem;
import es.csic.iiia.planes.definition.DTask;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class TaskDistributionPane extends JPanel {
    private static final int DEFAULT_WIDTH  = 1000;
    private static final int DEFAULT_HEIGHT = 1000;

    BufferedImage image;
    final DProblem problem;
    BufferedImage buffer;
    private boolean showTasks = false;
    private final Display display;

    public TaskDistributionPane(Display display, DProblem p) {
        this.display = display;
        problem = p;
        buildImage(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    @Override
    public void setBounds(int i, int i1, int i2, int i3) {
        super.setBounds(i, i1, i2, i3);
        changeSize(i2, i3);
    }

    @Override
    public void setBounds(Rectangle rctngl) {
        super.setBounds(rctngl);
        changeSize(rctngl.width, rctngl.height);
    }

    public void changeSize(int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }

        double xscale = ((double)width)  / DEFAULT_WIDTH;
        double yscale = ((double)height) / DEFAULT_HEIGHT;
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)buffer.getGraphics();
        g.setTransform(AffineTransform.getScaleInstance(xscale, yscale));
        g.drawImage(image, 0, 0, null);
        this.repaint();
    }

    private void buildImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double xscale = ((double)width) / problem.getWidth();
        double yscale = ((double)height) / problem.getHeight();
        Graphics2D g = (Graphics2D) image.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (DOperator o : problem.getOperators()) {
            for (DTask t : o.getTasks()) {
                g.setColor(display.getColor(t.getnCrisis()));
                g.fillOval((int) (t.getX() * xscale), (int) (t.getY() * yscale), 10, 10);
            }
        }

    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        if (showTasks) {
            grphcs.drawImage(buffer, 0, 0, null);
        }
    }

    void toggle() {
        showTasks = !showTasks;
    }

}
